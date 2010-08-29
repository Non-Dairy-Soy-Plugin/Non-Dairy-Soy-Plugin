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

import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.lexer.TestableSoyScanner;
import org.jetbrains.annotations.NonNls;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 21, 2010
 * Time: 12:24:23 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class ParserDeliveryAcceptanceTest extends BaseParserTest {

    private static final Pattern MATCH_DELETED_TEXT = Pattern.compile("\u007F+");

    private enum FailState { PASS, FAIL, RETRY_VERBOSE }

    private FailState failState = FailState.PASS;

    @Override
    protected void println(Object o) {
        if (failState == FailState.RETRY_VERBOSE) super.println(o);
    }

    @Override
    protected void println() {
        if (failState == FailState.RETRY_VERBOSE) super.println();
    }

    @Override
    protected void printTestHeader(String header) {
        System.out.println(header.substring(0, 78));
        System.out.println("This test should run quietly, unless there is a problem");
    }

    private List<List<SoySymbol>> groupSymbols(Iterator<SoySymbol> source) {
        List<List<SoySymbol>> outer = new LinkedList<List<SoySymbol>>();
        List<SoySymbol> inner = Collections.emptyList(); // value will be discarded
        String currentState = null;
        while (source.hasNext()) {
            SoySymbol symbol = source.next();
            String state = symbol.getState();
            if (!state.equals(currentState)) {
                currentState = state;
                inner = new LinkedList<SoySymbol>();
                outer.add(inner);
            }
            inner.add(symbol);
        }
        return outer;
    }

    private int[] countPermutations(List<List<SoySymbol>> groupedSymbols) {
        int[] count = {1,0};
        int totalSymbolCount = 0;
        for (List<SoySymbol> symbolGroup : groupedSymbols) {
            if (symbolGroup.isEmpty()) continue;
            count[0] += symbolGroup.size() * 3 - 1;
            totalSymbolCount += symbolGroup.size();
        }
        count[1] = totalSymbolCount * 3 - 1;
        return count;
    }

    List<SoySymbol> permuteSubListToDelete(List<SoySymbol> symbolGroup, int sequence) {
        int size = symbolGroup.size();
        int permutationsThisGroup = size * 3 - 1;
        int removeLeft;
        int removeRight;
        if (sequence == permutationsThisGroup) {
            // missing all elements
            removeLeft = 0;
            removeRight = size;
        } else if (sequence <= size) {
            removeLeft = sequence - 1;
            removeRight = removeLeft + 1;
            // missing one element
        } else {
            // omit a leading or trailing subset
            int base = sequence - size * 2;
            removeLeft = Math.max(base, 0);
            removeRight = Math.min(base + size - 1, size);
        }
        return symbolGroup.subList(removeLeft, removeRight);
    }

    private CharSequence permuteSourceBuffer(CharSequence sourceBuffer,
                                             List<List<SoySymbol>> groupedSymbols,
                                             int permutationSequence) {
        if (permutationSequence <= 0) return sourceBuffer;
        StringBuilder permuteBuffer = new StringBuilder(sourceBuffer);
        for (List<SoySymbol> symbolGroup : groupedSymbols) {
            int permutationsThisGroup = symbolGroup.size() * 3 - 1;
            if (permutationSequence <= permutationsThisGroup) {
                List<SoySymbol> toDelete = permuteSubListToDelete(symbolGroup, permutationSequence);
                for (SoySymbol symbol : toDelete) {
                    for (int i = symbol.getPosition(), j = i + symbol.getLength(); i < j; ++i) {
                        permuteBuffer.setCharAt(i, '\u007F');
                    }
                }
                return permuteBuffer;
            }
            permutationSequence -= permutationsThisGroup;
        }
        return sourceBuffer;
    }

    private Iterator<SoySymbol> permute(List<List<SoySymbol>> groupedSymbols,
                                        final int permutationSequence) {
        final Iterator<List<SoySymbol>> outer = groupedSymbols.iterator();
        return new Iterator<SoySymbol>() {

            private int sequence = permutationSequence;
            private Iterator<SoySymbol> inner = null;
            private SoySymbol next = null;

            private void prepareNext() {
                if (next != null) return;
                while (inner == null || !inner.hasNext()) {
                    if (!outer.hasNext()) return; // no more tokens
                    List<SoySymbol> symbolGroup = outer.next();
                    if (symbolGroup.isEmpty()) continue;
                    int permutationsThisGroup = symbolGroup.size() * 3 - 1;
                    if (sequence > 0) {
                        if (sequence <= permutationsThisGroup) {
                            List<SoySymbol> buffer = new ArrayList<SoySymbol>(symbolGroup);
                            List<SoySymbol> toDelete = permuteSubListToDelete(buffer, sequence);
                            toDelete.clear();
                            sequence = 0;
                        } else {
                            inner = symbolGroup.iterator();
                            sequence -= permutationsThisGroup;
                        }
                    }
                }
                next = inner.next();
            }

            public boolean hasNext() {
                prepareNext();
                return next != null;
            }

            public SoySymbol next() {
                if (!hasNext()) throw new NoSuchElementException();
                SoySymbol symbol = next;
                next = null;
                return symbol;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    protected MockTokenSource buildTestSource(CharSequence source,
                                              @NonNls String initialState,
                                              Deque<Object> expectedSequence) throws Exception {
        Assert.assertNull(expectedSequence);
        TestableSoyScanner scanner = SoyScannerTest.buildScanner(source, initialState);
        List<List<SoySymbol>> groupedSymbols = groupSymbols(scanner.iterator());
        int symbolCount = 0;
        for (List<SoySymbol> symbolGroup : groupedSymbols) symbolCount += symbolGroup.size();
        int[] permutationCount = countPermutations(groupedSymbols);
        int permutationCountTotal = 0;
        for (int c : permutationCount) {
            permutationCountTotal += c;
        }
        permutationCountTotal *= 2;
        System.out.printf("Preparing permutable source from %d symbols [%d chars] with %d permutations.%n",
                          symbolCount,
                          source.length(),
                          permutationCountTotal);
        return buildPermutableMockTokenSource(source, groupedSymbols, 0, 0, permutationCount, permutationCountTotal);
    }

    @Override
    protected void parseImpl(TokenSource tokenSource) {
        new SoyStructureParser(tokenSource).parse();
    }

    private void testPermutatedParse(String resourceName) throws Exception {
        try {
            testParseSequence(SoyTestUtil.getTestSourceBuffer(resourceName), "YYINITIAL");
        } catch (AssertionError e) {
            if (failState == FailState.RETRY_VERBOSE) throw e;
            failState = FailState.FAIL;
        }
        System.out.println();
    }

    @Test
    public void testMinimal() throws Exception {
        testPermutatedParse("minimal.soy");
    }

    @Test
    public void testExample() throws Exception {
        testPermutatedParse("example.soy");
    }

    @Test
    public void testFeatures() throws Exception {
        testPermutatedParse("features.soy");
    }

    @Test
    public void testEdgeCases() throws Exception {
        testPermutatedParse("edge-cases.soy");
    }

    @Test
    public void testErrorCases() throws Exception {
        testPermutatedParse("error-cases.soy");
    }

    private PermutableMockTokenSource buildPermutableMockTokenSource(CharSequence originalSource,
                                                                     List<List<SoySymbol>> groupedSymbols,
                                                                     int permutationSequence,
                                                                     int permutationCountIndex,
                                                                     int[] permutationCount,
                                                                     int permutationCountTotal) {
        CharSequence source;
        Iterator<SoySymbol> symbolIterator;
        if (permutationSequence % 2 == 0) {
            source = permuteSourceBuffer(originalSource, groupedSymbols, permutationSequence >> 1);
            symbolIterator = permute(groupedSymbols, permutationSequence);
        } else {
            source = MATCH_DELETED_TEXT.matcher(permuteSourceBuffer(originalSource, groupedSymbols, permutationSequence >> 1)).replaceAll("");
            try {
                symbolIterator = SoyScannerTest.buildScanner(source, "YYINITIAL").iterator();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new PermutableMockTokenSource(source,
                                             symbolIterator,
                                             originalSource,
                                             groupedSymbols,
                                             permutationSequence,
                                             permutationCountIndex,
                                             permutationCount,
                                             permutationCountTotal);
    }

    private class PermutableMockTokenSource extends MockTokenSource {

        private final CharSequence originalSource;
        private final List<List<SoySymbol>> groupedSymbols;
        private final int permutationSequence;
        private final int permutationCountIndex;
        private final int[] permutationCount;
        private final int permutationCountTotal;

        private PermutableMockTokenSource(CharSequence source,
                                          Iterator<SoySymbol> symbolIterator,
                                          CharSequence originalSource,
                                          List<List<SoySymbol>> groupedSymbols,
                                          int permutationSequence,
                                          int permutationCountIndex,
                                          int[] permutationCount,
                                          int permutationCountTotal) {
            super(source, symbolIterator);
            this.originalSource = originalSource;
            this.groupedSymbols = groupedSymbols;
            this.permutationSequence = permutationSequence;
            this.permutationCountIndex = permutationCountIndex;
            this.permutationCount = permutationCount;
            this.permutationCountTotal = permutationCountTotal;
        }

        @Override
        public int getPermutationSequence() {
            return permutationSequence + 1;
        }

        @Override
        public MockTokenSource getNextTokenSourcePermutation() {
            switch (failState) {
                case FAIL:
                    failState = FailState.RETRY_VERBOSE;
                    System.out.printf("Failed ParserDeliveryAcceptanceTest on permutation %d of %d%n",
                                      permutationSequence,
                                      permutationCountTotal);
                    return buildPermutableMockTokenSource(originalSource,
                                                          groupedSymbols,
                                                          permutationSequence,
                                                          permutationCountIndex,
                                                          permutationCount,
                                                          permutationCountTotal);
                case PASS:
                    if (permutationSequence + 1 < permutationCount[permutationCountIndex]) {
                        return buildPermutableMockTokenSource(originalSource,
                                                              groupedSymbols,
                                                              permutationSequence + 1,
                                                              permutationCountIndex,
                                                              permutationCount,
                                                              permutationCountTotal);
                    }
                    if (permutationCountIndex + 1 < permutationCount.length) {
                        List<List<SoySymbol>> nextSymbolPermutationSet;
                        nextSymbolPermutationSet = buildNextSymbolPermutationSet(permutationCountIndex + 1);
                        if (nextSymbolPermutationSet == null) return null; // all done
                        return buildPermutableMockTokenSource(originalSource,
                                                              nextSymbolPermutationSet,
                                                              1,
                                                              permutationCountIndex + 1,
                                                              permutationCount,
                                                              permutationCountTotal);
                    }
            }
            return null;
        }

        private List<List<SoySymbol>> buildNextSymbolPermutationSet(int i) {
            if (permutationCount[i] == 0) return null;
            switch (i) {
                case 1:
                    List<SoySymbol> flatSymbols = new LinkedList<SoySymbol>();
                    for (List<SoySymbol> symbolGroup : groupedSymbols) {
                        flatSymbols.addAll(symbolGroup);
                    }
                    return Arrays.asList(flatSymbols);
            }
            return null;
        }
    }
}
