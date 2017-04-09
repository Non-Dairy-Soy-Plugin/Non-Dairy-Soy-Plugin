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

import static org.junit.Assert.assertEquals;

import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.soylang.elements.TreeBuildingTokenSource;
import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationProducer;
import net.venaglia.nondairy.soylang.parser.permutations.Permutator;
import net.venaglia.nondairy.soylang.parser.permutations.impl.ASyncPermutator;
import net.venaglia.nondairy.soylang.parser.permutations.impl.TemplatesOneCharacterAtATime;
import net.venaglia.nondairy.util.SourceTuple;
import org.jetbrains.annotations.NonNls;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 21, 2010
 * Time: 12:24:23 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
@Ignore("TODO: understand why it fails")
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
                                              Deque<Object> expectedSequence,
                                              String resourceName) throws Exception {
        Assert.assertNull(expectedSequence);
//        TestableSoyScanner scanner = SoyScannerTest.buildScanner(source, initialState);
//        List<List<SoySymbol>> groupedSymbols = groupSymbols(scanner.iterator());
//        int symbolCount = 0;
//        for (List<SoySymbol> symbolGroup : groupedSymbols) symbolCount += symbolGroup.size();
//        int[] permutationCount = countPermutations(groupedSymbols);
//        int permutationCountTotal = 0;
//        for (int c : permutationCount) {
//            permutationCountTotal += c;
//        }
//        permutationCountTotal *= 2;
//        System.out.printf("Preparing permutable source from %d symbols [%d chars] with %d permutations.%n",
//                          symbolCount,
//                          source.length(),
//                          permutationCountTotal);
//        return buildPermutableMockTokenSource(source, groupedSymbols, 0, 0, permutationCount, permutationCountTotal);
        PermutationProducer producer = permutate(source, initialState, resourceName);
        producer.next();
        return buildPermutableMockTokenSource(source, producer);
    }

    private PermutationProducer permutate(CharSequence source, @NonNls String initialState, String resourceName) {
        Collection<Permutator> permutators = new LinkedList<Permutator>();
//        permutators.add(new AllSymbolOmittingPermutator());
//        permutators.add(new HeadSymbolOmittingPermutator());
//        permutators.add(new TailSymbolOmittingPermutator());
//        permutators.add(new SingleSymbolOmittingPermutator());
        permutators.add(new TemplatesOneCharacterAtATime());
//        permutators.add(new PrintTagsOneCharacterAtATime());
        ASyncPermutator permutator = new ASyncPermutator(permutators, resourceName);
        permutator.permutate(source, initialState);
        return permutator;
    }

    public static void _main(String[] args) {
        String source =
                "{namespace example.soy}\n" +
                "\n" +
                "/**\n" +
                " * An example template doc comment\n" +
                " * @param required Need this parameter\n" +
                " * @param? optional Sometimes need this one\n" +
                " */\n" +
                "{template .nondairy private=\"true\"}\n" +
                "    {if length($required) >= 100}\n" +
                "        Last Item: {$required[length($required) - 1]}<br>\n" +
                "    {/if}\n" +
                "\n" +
                "    {foreach $i in $required}\n" +
                "        Hello {$i|escapeHtml}\n" +
                "    {ifempty}\n" +
                "        {$optional|insertWordBreaks:8}\n" +
                "    {/foreach}\n" +
                "{/template}" +
                "/** eof */";
        ParserDeliveryAcceptanceTest test = new ParserDeliveryAcceptanceTest();
        PermutationProducer producer = test.permutate(source, "YYINITIAL",null);
        int count = 0;
        while (producer.hasNext()) {
            producer.next();
            count++;
            System.out.println(producer.getPermutatorName() + " - " + producer.getSeq());
            System.out.println(producer.getModifiedSource());
            System.out.println("--------------------------------------------------------------------");
        }
        System.out.printf("Produced %d iterations%n", count);
    }

    @Override
    protected void parseImpl(TokenSource tokenSource) {
        new SoyStructureParser(tokenSource).parse();
    }

    private static final Pattern MATCH_HOMOGENEOUS_ELEMENT = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*|[1-9]\\d+|.");

    private String mutateRemove(SourceTuple source, int left, int right) throws Exception {
        StringBuilder buffer = new StringBuilder(source.document.getText());
        buffer.replace(left, right, "");
        return buffer.toString();
    }

    private void testPermutatedParse(SourceTuple source, PsiElement permuteElement, Count count) throws Exception {
        String t = permuteElement.getText();
        if (t.length() == 0) {
            return;
        }
        for (PsiElement child : permuteElement.getChildren()) {
            testPermutatedParse(source, child, count);
        }
        if (permuteElement.getParent() == null || !permuteElement.getParent().getTextRange().equals(permuteElement.getTextRange())) {
            int left = permuteElement.getParent() == null ? 0 : permuteElement.getTextOffset();
            int right = left + permuteElement.getTextLength();
            quietParseVerboseIfFail(source, left, right);
            count.increment();
            if (t.length() > 1) {
                Matcher matcher = MATCH_HOMOGENEOUS_ELEMENT.matcher(t);
                if (!matcher.matches()) {
                    if (matcher.find()) {
                        quietParseVerboseIfFail(source, left + matcher.start(), left + matcher.end());
                        count.increment();
                        if (matcher.start() > 0) {
                            quietParseVerboseIfFail(source, left, left + matcher.start());
                            count.increment();
                        }
                        if (matcher.end() < t.length()) {
                            quietParseVerboseIfFail(source, left + matcher.end(), right);
                            count.increment();
                        }
                        int last = -1;
                        while (matcher.find()) {
                            last = matcher.start();
                        }
                        if (last > 0) {
                            matcher.find(last);
                            quietParseVerboseIfFail(source, left + matcher.start(), left + matcher.end());
                            count.increment();
                            if (matcher.start() > 0) {
                                quietParseVerboseIfFail(source, left, left + matcher.start());
                                count.increment();
                            }
                            if (matcher.end() < t.length()) {
                                quietParseVerboseIfFail(source, left + matcher.end(), right);
                                count.increment();
                            }
                        }
                    }
                }
            }
        }
    }

    private void quietParseVerboseIfFail(SourceTuple source, int left, int right) throws Exception {
        boolean track = TreeBuildingTokenSource.TRACK_WHERE_MARKERS_ARE_CREATED.get();
        String mutatedSource = mutateRemove(source, left, right);
        try {
            failState = FailState.PASS;
            TreeBuildingTokenSource.TRACK_WHERE_MARKERS_ARE_CREATED.set(false);
            new SourceTuple(source.name, mutatedSource);
        } catch (Exception e) {
            try {
                System.out.println(mutatedSource);
                TreeBuildingTokenSource.TRACK_WHERE_MARKERS_ARE_CREATED.set(true);
                failState = FailState.RETRY_VERBOSE;
                new SourceTuple(source.name, mutatedSource);
            } finally {
                failState = FailState.FAIL;
            }
        } finally {
            TreeBuildingTokenSource.TRACK_WHERE_MARKERS_ARE_CREATED.set(track);
        }
    }

    private int countPermutatedParse(PsiElement permuteElement) throws Exception {
        int count = 0;
        String t = permuteElement.getText();
        if (t.length() == 0) {
            return count;
        }
        if (permuteElement.getParent() == null || !permuteElement.getParent().getTextRange().equals(permuteElement.getTextRange())) {
            count++;
            if (t.length() > 1) {
                Matcher matcher = MATCH_HOMOGENEOUS_ELEMENT.matcher(t);
                if (!matcher.matches()) {
                    if (matcher.find()) {
                        count++;
                        if (matcher.start() > 0) {
                            count++;
                        }
                        if (matcher.end() < t.length()) {
                            count++;
                        }
                        int last = -1;
                        while (matcher.find()) {
                            last = matcher.start();
                        }
                        if (last > 0)  {
                            matcher.find(last);
                            count++;
                            if (matcher.start() > 0) {
                                count++;
                            }
                            if (matcher.end() < t.length()) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        for (PsiElement child : permuteElement.getChildren()) {
            count += countPermutatedParse(child);
        }
        return count;
    }

    private void testPermutatedParse(String resourceName) throws Exception {
        SourceTuple tuple = new SourceTuple(resourceName);
        Count count = new Count(resourceName, countPermutatedParse(tuple.psi));
        System.out.println("expecting " + count.getExpected() + " permutations of " + resourceName);
        testPermutatedParse(tuple, tuple.psi, count);
        System.out.println("tested " + count.getCurrent() + " permutations of " + resourceName);
        assertEquals("Expected count is not equal to the actual count", count.getExpected(), count.getCurrent());
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
//    @Ignore("This test takes several minutes")
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

    private PermutableMockTokenSource2 buildPermutableMockTokenSource(CharSequence originalSource,
                                                                      PermutationProducer producer) {
        return new PermutableMockTokenSource2(originalSource, producer);
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

    private class PermutableMockTokenSource2 extends MockTokenSource {

        private final PermutationProducer producer;
        private final CharSequence originalSource;

        PermutableMockTokenSource2(CharSequence originalSource,
                                   PermutationProducer producer) {
            super(producer.getModifiedSource(), producer.getIterator());
            this.producer = producer;
            this.originalSource = originalSource;
        }

        @Override
        public int getPermutationSequence() {
            return producer.getSeq();
        }

        @Override
        public MockTokenSource getNextTokenSourcePermutation() {
            switch (failState) {
                case FAIL:
                    failState = FailState.RETRY_VERBOSE;
                    System.out.printf("Failed ParserDeliveryAcceptanceTest on permutation %d%n",
                                      getPermutationSequence());
                    return new PermutableMockTokenSource2(originalSource, producer);
                case PASS:
                    if (producer.hasNext()) {
                        producer.next();
                        return new PermutableMockTokenSource2(originalSource, producer);
                    }
                    break;
            }
            return null;
        }
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
                    List<List<SoySymbol>> nextSet = new ArrayList<List<SoySymbol>>(1);
                    nextSet.add(flatSymbols);
                    return nextSet;
            }
            return null;
        }
    }

    private static class Count {

        private final long start;
        private final String name;
        private final int expected;

        private int current;

        private Count(String name, int expected) {
            this.start = System.currentTimeMillis();
            this.name = name;
            this.expected = expected;
        }

        public void increment() {
            current++;
            if (current % 250 == 0) {
                System.out.printf("Permutation testing on %s: %s\n", name, this);
            }
            if (current == expected + 1) {
                System.err.printf("Permutation testing on %s has overrun the number of expected permutations: n > %d\n",
                                  name,
                                  expected);
            }
        }

        public String getName() {
            return name;
        }

        public int getExpected() {
            return expected;
        }

        public int getCurrent() {
            return current;
        }

        @Override
        public String toString() {
            long elapsed = System.currentTimeMillis() - start;
            double pct = (current * 100.0) / expected;
            if (elapsed > 10000 && pct > 0.0 && pct < 100.0) {
                double remaining = (100.0 - pct) * elapsed / pct / 1000.0;
                if (remaining >= 60) {
                    double mins = Math.floor(remaining / 60.0);
                    remaining -= mins * 60.0;
                    if (mins >= 60 ) {
                        double hrs = Math.floor(mins / 60.0);
                        mins -= hrs * 60.0;
                        return String.format("%d/%d (%.2f%%) %d:%02d:%04.1f remaining", current, expected, pct, Math.round(hrs), Math.round(mins), remaining);
                    } else {
                        return String.format("%d/%d (%.2f%%) %d:%04.1f remaining", current, expected, pct, Math.round(mins), remaining);
                    }
                } else {
                    return String.format("%d/%d (%.2f%%) 0:%04.1f remaining", current, expected, pct, remaining);
                }
            } else {
                return String.format("%d/%d (%.2f%%)", current, expected, pct);
            }
        }
    }
}
