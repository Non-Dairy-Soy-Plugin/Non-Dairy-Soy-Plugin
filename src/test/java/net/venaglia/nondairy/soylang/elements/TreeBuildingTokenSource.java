/*
 * Copyright 2010 - 2013 Ed Venaglia
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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.mocks.MockTreeNode;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyLanguage;
import net.venaglia.nondairy.soylang.SoyParserDefinition;
import net.venaglia.nondairy.soylang.elements.factory.PsiElementFactory;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.TokenSource;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: ed
 * Date: 2/8/12
 * Time: 6:35 PM
 */
public class TreeBuildingTokenSource extends TokenSource {

    public static final ThreadLocal<Boolean> TRACK_WHERE_MARKERS_ARE_CREATED =
            new ThreadLocal<Boolean>() {
                @Override
                protected Boolean initialValue() {
                    return Boolean.FALSE;
                }
            };

    static {
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(SoyLanguage.INSTANCE, new SoyParserDefinition());
    }

    private final CharSequence source;
    private final List<SoySymbol> symbols;
    private final int symbolCount;
    private final boolean trackWhereCreated;

    private int symbolIndex = 0;
    private int readTokenCallsSinceLastAdvance = 0;

    private TempNode head;
    private TempNode tail;

    public TreeBuildingTokenSource(CharSequence source, Iterator<SoySymbol> iterator) {
        this.source = source;
        this.symbols = new ArrayList<SoySymbol>();
        while (iterator.hasNext()) {
            symbols.add(iterator.next());
        }
        symbolCount = symbols.size();
        this.trackWhereCreated = TRACK_WHERE_MARKERS_ARE_CREATED.get();
    }

    @Override
    public PsiBuilder.Marker mark(@NotNull @NonNls Object name) {
        TempNode child = createNext(tail, String.valueOf(name));
        if (head == null) {
            head = child;
        }
        tail = child;
        return child;
    }

    @Override
    public IElementType token() {
//        if (symbolIndex < 0) {
//            throw new IllegalStateException("must call advance() before calling token()");
//        }
        if (symbolIndex >= symbolCount) {
            throw new NoSuchElementException();
        }
        if (++readTokenCallsSinceLastAdvance > 1000) {
            throw new IllegalStateException("Parser is not advancing, too many successive calls to token()");
        }
        return symbols.get(symbolIndex).getToken();
    }

    @Override
    public IElementType previous() {
        if (symbolIndex < 1 || symbolIndex > symbolCount) {
            return null;
        }
        return symbols.get(symbolIndex - 1).getToken();
    }

    @Override
    public String text() {
//        if (symbolIndex < 0) {
//            throw new IllegalStateException("must call advance() before calling text()");
//        }
        if (symbolIndex >= symbolCount) {
            throw new NoSuchElementException();
        }
        SoySymbol symbol = symbols.get(symbolIndex);
        return source.subSequence(symbol.getPosition(),
                                  symbol.getPosition() + symbol.getLength())
                     .toString();
    }

    @Override
    public boolean eof() {
        return symbolIndex >= symbolCount;
    }

    @Override
    public void advance() {
        if (eof()) {
            throw new NoSuchElementException();
        }
        symbolIndex++;
        readTokenCallsSinceLastAdvance = 0;
    }

    @Override
    public int index() {
        return symbolIndex;
    }

    @Override
    public void error(String message) {
        mark("errorMessage");
        tail.error(message);
        readTokenCallsSinceLastAdvance = 0;
    }

    public PsiElement buildNode(@NotNull PsiFile fileNode, @NotNull final PsiElementFactory factory) {
        MockTreeNode headNode = new MockTreeNode.Builder(head.builder(), fileNode).build(factory);
        List<MockTreeNode> rootNodes = headNode.getMockChildren();
        if (rootNodes.size() != 1) {
            throw new IllegalStateException("should have exactly one root node, found " + rootNodes.size());
        }
        MockTreeNode rootNode = rootNodes.get(0);
        return rootNode.getPsi();
    }

    private class TempNode implements PsiBuilder.Marker {

        private final String nodeName;
        private final StackTraceElement[] createdAt;
        private final int createdAtMore;

        private int startSymbolIndex = symbolIndex;
        private int endSymbolIndex = -1;

        private TempNode prev;
        private TempNode next;

        private TempNode parent;
        private List<TempNode> children;

        private boolean done;
        private IElementType type;
        private String errorMessage;
        
        private TempNode(@NotNull @NonNls String nodeName) {
            this.nodeName = nodeName;
            int createdAtMore = 0;
            if (trackWhereCreated) {
                List<StackTraceElement> createdAt = new ArrayList<StackTraceElement>(5);
                for (StackTraceElement ste : new Exception().getStackTrace()) {
                    if (!createdAt.isEmpty() || !ste.toString().contains(TreeBuildingTokenSource.class.getSimpleName())) {
                        if (createdAt.size() < 5) {
                            createdAt.add(ste);
                        } else {
                            createdAtMore++;
                        }
                    }
                }
                this.createdAt = createdAt.toArray(new StackTraceElement[createdAt.size()]);
                this.createdAtMore = createdAtMore;
            } else {
                this.createdAt = null;
                this.createdAtMore = -1;
            }
        }

        private void addChild(TempNode child) {
            if (children == null) {
                children = new ArrayList<TempNode>(4);
            }
            children.add(child);
        }

        private void assertNotDone() {
            if (done) {
                if (type != null) {
                    throw new IllegalStateException("marker is already done" + createdAtTrace());
                } else {
                    throw new IllegalStateException("marker is dropped" + createdAtTrace());
                }
            }
        }

        private void assertDone() {
            if (!done) {
                throw new IllegalStateException("unclosed marker: " + nodeName + createdAtTrace());
            }
        }

        private void assertAllChildrenDone() {
            for (TempNode tn = next; tn != null; tn = tn.next) {
                tn.assertDone();
            }
        }

        private void assertTokenWasAdvanced() {
            if (symbolIndex == startSymbolIndex) {
                throw new IllegalStateException("marker was created and closed without advancing" + createdAtTrace());
            }
        }

        @Override
        public PsiBuilder.Marker precede() {
            TempNode tn = new TempNode(nodeName);
            tn.startSymbolIndex = startSymbolIndex;
            if (done && parent != null) {
                int l = parent.children.indexOf(this);
                int r = parent.children.size();
                tn.parent = parent;
                if (parent == tail) {
                    tail = tn;
                }
                List<TempNode> slice = parent.children.subList(l, r);
                for (TempNode child : slice) {
                    child.parent = tn;
                    tn.addChild(child);
                }
                slice.clear();
                slice.add(tn);
            } else {
                tn.prev = this.prev;
                tn.next = this;
                this.prev = tn;
                if (parent != null) {
                    parent.children.add(parent.children.indexOf(this), tn);
                }
            }
            if (this == head) {
                head = tn;
            }
            return tn;
        }

        private void close(boolean addAsChildOfPrev) {
            assertNotDone();
            endSymbolIndex = symbolIndex;
            TempNode parent = prev;
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            }
            if (addAsChildOfPrev && parent != null) {
                parent.addChild(this);
                this.parent = parent;
            }
            done = true;
            endSymbolIndex = symbolIndex;
            if (tail == this) {
                while (tail != null && tail.done) {
                    if (tail.next != null) {
                        tail = tail.next;
                    } else if (tail == this && tail.parent == null) {
                        tail = parent;
                    } else {
                        tail = tail.parent;
                    }
                }
            }
            next = null;
            prev = null;
        }

        @Override
        public void drop() {
            close(false);
        }

        @Override
        public void error(String message) {
            assertNotDone();
            assertAllChildrenDone();
            errorMessage = message;
            close(true);
        }

        @Override
        public void errorBefore(String message, PsiBuilder.Marker before) {
            error(message);
        }

        @Override
        public void done(IElementType type) {
            assertNotDone();
            assertAllChildrenDone();
            if (type != SoyElement.soy_file) { // could be an empty file
                assertTokenWasAdvanced();
            }
            this.type = type;
            close(true);
        }

        @Override
        public void doneBefore(IElementType type, PsiBuilder.Marker before) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void doneBefore(IElementType type, PsiBuilder.Marker before, String errorMessage) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rollbackTo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collapse(IElementType type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder left,
                                              @Nullable WhitespacesAndCommentsBinder right) {
            throw new UnsupportedOperationException();
        }

        public MockTreeNode.Builder builder() {
            assertDone();
            MockTreeNode.Builder builder = new MockTreeNode.Builder();
            builder.setType(type);
            builder.setErrorMessage(errorMessage);
            if (symbolCount == 0 || startSymbolIndex == endSymbolIndex) {
                builder.setText("");
                builder.setStartOffset(0);
            } else {
                SoySymbol startSymbol = symbols.get(startSymbolIndex);
                SoySymbol endSymbol = symbols.get(endSymbolIndex - 1);
                int startPostiion = startSymbol.getPosition();
                int endPosition = endSymbol.getPosition() + endSymbol.getLength();
                CharSequence text = source.subSequence(startPostiion,
                                                       Math.max(startPostiion, endPosition));
                builder.setText(text);
                builder.setStartOffset(startPostiion);
            }
            if (children != null) {
                for (TempNode child : children) {
                    builder.addChild(child.builder());
                }
            }
            return builder;
        }

        private String createdAtTrace() {
            if (createdAt == null || createdAt.length == 0) {
                return "";
            } else {
                @NonNls StringBuilder buf = new StringBuilder();
                buf.append("\n\tMarker '").append(nodeName).append("' created:");
                for (StackTraceElement ste : createdAt) {
                    buf.append("\n\t\tat ");
                    buf.append(ste);
                }
                if (createdAtMore > 0) {
                    buf.append("\n\t\t").append(createdAtMore).append(" more...");
                }
                return buf.toString();
            }
        }
    }

    TempNode createNext(@Nullable TempNode node, @NotNull @NonNls String name) {
        if (node != null && node.next != null) {
            throw new IllegalStateException("Passed node already has another node after it.");
        }
        TempNode tn = new TempNode(name);
        tn.prev = node;
        if (node != null) {
            node.next = tn;
        } else {
            tail = tn;
        }
        return tn;
    }
}
