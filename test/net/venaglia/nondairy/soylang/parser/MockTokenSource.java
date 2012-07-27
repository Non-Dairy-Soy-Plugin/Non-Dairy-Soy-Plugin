/*
 * Copyright 2010 - 2012 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 15, 2010
 * Time: 7:59:14 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class MockTokenSource extends TokenSource {

    private static final String ENDLESS_LOOP_FAIL_MESSAGE =
            "Parser appears to be caught in an endless loop at %s; generating repetitive calls without advancing: %s at %s";
    
    private final CharSequence source;
    private final Iterator<SoySymbol> iterator;
    private final Set<Object> distinctEventsSinceLastAdvance = new LinkedHashSet<Object>();

    private SoySymbol current;
    private int seq = -1;
    private int eventCountSinceLastAdvance = 0;
    private int eofCountSinceLastAdvance = 0;
    private int tokenReadCountSinceLastAdvance = 0;
    private int textReadSinceLastAdvance = 0;
    private boolean finished = false;
    private EventDelegate eventDelegate = null;
    private MockMarker firstProducedMarker;
    private MockMarker lastProducedMarker;

    public MockTokenSource(CharSequence source, Iterator<SoySymbol> symbolIterator) {
        this.source = source;
        this.iterator = symbolIterator;
        advance();
    }

    public int getPermutationSequence() {
        return 1;
    }

    public MockTokenSource getNextTokenSourcePermutation() {
        return null;
    }

    private void testForEndlessLoop () {
        int distinctCount = distinctEventsSinceLastAdvance.size();
        int totalCount = eventCountSinceLastAdvance +
                         eofCountSinceLastAdvance +
                         textReadSinceLastAdvance +
                         tokenReadCountSinceLastAdvance;
        if (totalCount > distinctCount * 32) {
            Assert.fail(String.format(ENDLESS_LOOP_FAIL_MESSAGE, current, distinctEventsSinceLastAdvance, this));
        }
    }

    public MockTokenSource setEventDelegate(EventDelegate eventDelegate) {
        this.eventDelegate = eventDelegate;
        return this;
    }

    public void event(Object value) {
        distinctEventsSinceLastAdvance.add(value);
        eventCountSinceLastAdvance++;
        if (eventDelegate != null) eventDelegate.event(value, this);
    }

    @Override
    public PsiBuilder.Marker mark(@NonNls Object name) {
        return new MockMarker(seq, current);
    }

    @Override
    public IElementType token() {
        tokenReadCountSinceLastAdvance++;
        distinctEventsSinceLastAdvance.add(new MockParseMetaToken("token()", this));
        testForEndlessLoop();
        return current == null ? null : current.getToken();
    }

    @Override
    public String text() {
        textReadSinceLastAdvance++;
        MockParseMetaToken TEXT_INVOKED = new MockParseMetaToken("text()", this);
        distinctEventsSinceLastAdvance.add(TEXT_INVOKED);
        testForEndlessLoop();
        if (current == null) return null;
        return source.subSequence(current.getPosition(), current.getPosition() + current.getLength()).toString();
    }

    @Override
    public boolean eof() {
        eofCountSinceLastAdvance++;
        distinctEventsSinceLastAdvance.add(new MockParseMetaToken("eof()", this));
        testForEndlessLoop();
        return current == null && !iterator.hasNext();
    }

    private void countAdvance() {
        eventCountSinceLastAdvance = 0;
        eofCountSinceLastAdvance = 0;
        tokenReadCountSinceLastAdvance = 0;
        textReadSinceLastAdvance = 0;
        distinctEventsSinceLastAdvance.clear();
        seq++;
    }

    @Override
    public void advance() {
        if (current != null) event(current.getToken());
        if (iterator.hasNext()) {
            current = iterator.next();
            countAdvance();
        } else {
            if (!finished) {
                finished = true;
                countAdvance();
            }
            current = null;
        }
    }

    @Override
    public int index() {
        return seq;
    }

    @Override
    public void error(String message) {
        event(new MockParseMetaToken("error", message, this));
    }

    public Deque<IElementType> purge() {
        Deque<IElementType> buffer = new LinkedList<IElementType>();
        while (!finished) {
            current = null; // suppress the event
            advance();
            buffer.add(token());
        }
        return buffer;
    }

    public void assertAllMarkersAreClosed() {
        StringBuilder buffer = new StringBuilder();
        int unclosedMarkers = 0;
        for (MockMarker m = firstProducedMarker; m != null; m = m.next) {
            if (m.open) {
                buffer.append("\n\t* Unclosed marker created at ").append(m.caller);
                unclosedMarkers++;
            }
        }
        Assert.assertTrue("Done parsing, but markers were left open:" + buffer, unclosedMarkers == 0);
    }

    private static StackTraceElement get2Back() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return stackTrace[5];
    }

    @Override
    public String toString() {
        return "TokenSource @ " + (current == null ? "[EOF]" : current.toString());
    }

    protected class MockMarker implements PsiBuilder.Marker {

        private final StackTraceElement caller;
        private final int startSeq;
        private final SoySymbol symbol;

        private boolean open = true;
        private MockMarker prev;
        private MockMarker next;

        public MockMarker(int startSeq, SoySymbol symbol) {
            caller = get2Back();
            this.startSeq = startSeq;
            this.symbol = symbol;
            if (firstProducedMarker == null) {
                firstProducedMarker = this;
                lastProducedMarker = this;
            } else {
                prev = lastProducedMarker;
                prev.next = this;
                lastProducedMarker = this;
            }
        }

        private MockMarker(@NotNull MockMarker precede) {
            caller = get2Back();
            startSeq = precede.startSeq;
            symbol = precede.symbol;
            next = precede;
            prev = precede.prev;
            precede.prev = this;
            if (prev != null) {
                prev.next = this;
            }
        }

        public int getTokenCount() {
            return seq - startSeq;
        }

        public PsiBuilder.Marker precede() {
//            Assert.assertTrue(open);
            return new MockMarker(this);
        }

        public void drop() {
            Assert.assertTrue(open);
            open = false;
            if (prev != null && next != null) {
                prev.next = next;
                next.prev = prev;
            } else if (prev != null) {
                lastProducedMarker = prev;
                prev.next = null;
            } else if (next != null) {
                firstProducedMarker = next;
                next.prev = null;
            } else {
                firstProducedMarker = null;
                lastProducedMarker = null;
            }
            next = null;
            prev = null;
        }

        public void rollbackTo() {
            Assert.fail();
        }

        public void done(IElementType type) {
            closeImpl("Closing a marker", type.toString());
            event(type);
            event(getTokenCount());
        }

        private void closeImpl(String action, String type) {
            StringBuilder buffer = new StringBuilder();
            int unclosedMarkers = 0;
            for (MockMarker m = this.next; m != null; m = m.next) {
                if (m.open) {
                    buffer.append("\n\tUnclosed marker created at ").append(m.caller).append(" : ").append(symbol);
                    unclosedMarkers++;
                }
            }
            String message = action + " while markers within are still open: " + type + "[" + getTokenCount() + "] at " + MockTokenSource.this + buffer;
            Assert.assertTrue(message, unclosedMarkers == 0);

            Assert.assertTrue(action + " that was already closed or discarded at " + MockTokenSource.this, open);
//            Assert.assertTrue("Closing a marker that contains no tokens at " + MockTokenSource.this, getTokenCount() > 0);
            open = false;
        }

        public void doneBefore(IElementType type, PsiBuilder.Marker before) {
            Assert.fail();
        }

        public void doneBefore(IElementType type, PsiBuilder.Marker before, String errorMessage) {
            Assert.fail();
        }

        public void error(String message) {
            closeImpl("Applying an error to a marker", "");
            MockTokenSource.this.error(message);
        }

        @Override
        public void collapse(IElementType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void errorBefore(String message, PsiBuilder.Marker before) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder left,
                                              @Nullable WhitespacesAndCommentsBinder right) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "TokenSource @ " + (symbol == null ? "[EOF]" : symbol.toString());
        }
    }
}
