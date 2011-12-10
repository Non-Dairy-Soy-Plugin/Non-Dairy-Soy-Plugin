/*
 * Copyright 2010 Ed Venaglia
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

import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.soylang.lexer.TestableSoyScanner;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.util.Deque;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 15, 2010
 * Time: 5:05:06 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public abstract class BaseParserTest {

    @Rule
    public TestName name = new TestName();

    private static final String TEST_BEGIN_HEADER =
            "=====[ %s.%s ]=====================================================================";

    private static final String TEST_INTERMEDIATE_HEADER =
            "-----[ %s ]------------------------------------------------------------------------";

    private SoyToken nextSoyToken(Iterator<?> iterator) {
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof SoyToken) return (SoyToken)obj;
        }
        return null;
    }

    private void validateLexerAgainstExpectedSequence(TestableSoyScanner testableSoyScanner,
                                                      Deque<Object> expectedSequence) {
        String msg = "Token from expected sequence does not follow token from source.";
        Iterator<Object> iterator = expectedSequence.iterator();
        for (SoySymbol symbol : testableSoyScanner) {
            IElementType source = symbol.getToken();
            if (source instanceof SoyToken) {
                IElementType expect = nextSoyToken(iterator);
                Assert.assertEquals(msg, expect, source);
            }
        }
        IElementType token = nextSoyToken(iterator);
        if (token != null) {
            Assert.fail("Expected sequence contains more tokens than provided from the source: " + token);
        }
    }

    protected MockTokenSource buildTestSource(CharSequence source,
                                              @NonNls String initialState,
                                              final Deque<Object> expectedSequence,
                                              String resourceName) throws Exception {
        TestableSoyScanner scanner = SoyScannerTest.buildScanner(source, initialState);
        if (expectedSequence != null) {
            validateLexerAgainstExpectedSequence(scanner, expectedSequence);
            scanner.reset();
        }
        EventDelegate eventDelegate = new EventDelegate() {
            public void event(Object value, MockTokenSource mockTokenSource) {
                println(value);
                if (expectedSequence != null) {
                    Assert.assertFalse("An unexpected event was recorded:" + value + " at " + mockTokenSource, expectedSequence.isEmpty());
                    Assert.assertEquals("An event was recorded that did not match the expectation at " + mockTokenSource, expectedSequence.peekFirst(), value);
                    expectedSequence.poll();
                }
            }
        };
        return new MockTokenSource(source, scanner.iterator()).setEventDelegate(eventDelegate);
    }

    protected abstract void parseImpl(TokenSource tokenSource);

    protected void println(Object o) {
        System.out.println(o);
    }

    protected void println() {
        System.out.println();
    }

    protected void printTestHeader(String header) {
        println(header.substring(0, 78));
    }

    protected void testParseSequence(CharSequence source, CharSequence expectSource, @NonNls String initialState, @NonNls @Nullable String resourceName) throws Exception {
        String header = String.format(TEST_BEGIN_HEADER, getClass().getSimpleName(), name.getMethodName());
        printTestHeader(header);
        println();
        println(source);
        println();
        Deque<Object> expectedSequence = ExpectedExpression.getExpectedSequence(expectSource);
        MockTokenSource tokenSource = buildTestSource(source, initialState, expectedSequence, resourceName);
        while (tokenSource != null) {
            int permutationSequence = tokenSource.getPermutationSequence();
            parseImpl(tokenSource);
            header = String.format(TEST_INTERMEDIATE_HEADER, "Running permutation " + permutationSequence);
            println(header.substring(0, 78));
            if (!tokenSource.eof()) {
                Deque<IElementType> todo = tokenSource.purge();
                if (todo.peekLast() == null) todo.removeLast();
                Assert.assertTrue("Parser exited before reading all tokens (todo: " + todo + ")", todo.isEmpty());
            }
            if (!expectedSequence.isEmpty()) {
                Object nextExpected = expectedSequence.peekFirst();
                if (nextExpected instanceof MockParseMetaToken &&
                    !"end".equals(((MockParseMetaToken)nextExpected).getName())) {
                    Assert.fail("Parser exited before meeting all expectations (todo: " + expectedSequence + ")");
                }
            }
            tokenSource.assertAllMarkersAreClosed();
            tokenSource = tokenSource.getNextTokenSourcePermutation();
        }
    }

    protected void testParseSequence(CharSequence source, @NonNls String initialState, @NonNls @Nullable String resourceName) throws Exception {
        testParseSequence(source, initialState, (EventDelegate)null, resourceName);
    }

    protected void testParseSequence(CharSequence source,
                                     @NonNls String initialState,
                                     final EventDelegate eventDelegate,
                                     @Nullable String resourceName) throws Exception {
        String header = String.format(TEST_BEGIN_HEADER, getClass().getSimpleName(), name.getMethodName());
        printTestHeader(header);
        EventDelegate superDelegate = new EventDelegate() {
            public void event(Object value, MockTokenSource mockTokenSource) {
                println(value);
                if (eventDelegate != null) eventDelegate.event(value, mockTokenSource);
            }
        };
        MockTokenSource tokenSource = buildTestSource(source, initialState, null, resourceName).setEventDelegate(superDelegate);
        while (tokenSource != null) {
            int permutationSequence = tokenSource.getPermutationSequence();
            header = String.format(TEST_INTERMEDIATE_HEADER, "Running permutation " + permutationSequence);
            println(header.substring(0, 78));
            parseImpl(tokenSource);
            if (!tokenSource.eof()) {
                Deque<IElementType> todo = tokenSource.purge();
                if (todo.peekLast() == null) todo.removeLast();
                while (todo.size() > 32) todo.removeLast();
                Assert.assertTrue("Parser exited before reading all tokens (todo: " + todo + ")", todo.isEmpty());
            }
            tokenSource.assertAllMarkersAreClosed();
            tokenSource = tokenSource.getNextTokenSourcePermutation();
        }
    }
}
