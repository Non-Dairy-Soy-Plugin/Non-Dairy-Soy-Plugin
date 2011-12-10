/*
 * Copyright 2011 Ed Venaglia
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

package net.venaglia.nondairy.soylang.parser.permutations.impl;

import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationConsumer;
import net.venaglia.nondairy.soylang.parser.permutations.Permutator;
import net.venaglia.nondairy.util.RangeBasedIntegerSet;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Permutator implementation that will delete symbols from the source.
 */
public abstract class CharacterOmittingPermutator implements Permutator {

    private static final Pattern MATCH_DELETED_TEXT = Pattern.compile("\u007F+");

    @Override
    public void permutate(final CharSequence source,
                          final @NonNls String initialState,
                          final PermutationConsumer consumer)
            throws Exception {
        List<SoySymbol> allSymbols = new LinkedList<SoySymbol>();
        for (SoySymbol symbol : SoyScannerTest.buildScanner(source, initialState)) {
            allSymbols.add(symbol);
        }
        allSymbols = Collections.unmodifiableList(new ArrayList<SoySymbol>(allSymbols));

        CharacterOmittingConsumer characterOmittingConsumer = new CharacterOmittingConsumer() {
            @Override
            public void add(RangeBasedIntegerSet charsToDelete) {
                StringBuilder modifiedSource = new StringBuilder(source);
                charsToDelete.add(modifiedSource.length()); // so we find our stopping place
                for (int i = charsToDelete.getNext(0), l = modifiedSource.length(); i < l; i = charsToDelete.getNext(i + 1)) {
                    modifiedSource.setCharAt(i, '\u007F');
                }
                try {
                    Iterator<SoySymbol> iterator = SoyScannerTest.buildScanner(modifiedSource, initialState).iterator();
                    consumer.add(iterator, modifiedSource);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Set<IElementType> keyTokens = new HashSet<IElementType>(Arrays.asList(getKeyTokens()));
        for (int i = 1, l = allSymbols.size(); i < l; i++) {
            SoySymbol symbol = allSymbols.get(i);
            if (keyTokens.contains(symbol.getToken())) {
                TokenPair[] templateTokens = seekTemplateTokens(allSymbols, i);
                for (TokenPair pair : templateTokens) {
                    i = Math.max(i, pair.getEnd());
                    generateRanges(allSymbols, source.length(), characterOmittingConsumer, pair);
                }
            }
        }
    }

    protected int findNextOf(List<SoySymbol> allSymbols, int tagIndex, int dir, IElementType... tokens) {
        Set<IElementType> stop = new HashSet<IElementType>(Arrays.asList(tokens));
        return scan(allSymbols, tagIndex, dir, dir < 0 ? -1 : allSymbols.size(), false, stop);
    }

    protected int findLastOf(List<SoySymbol> allSymbols, int tagIndex, int dir, IElementType... tokens) {
        Set<IElementType> stop = new HashSet<IElementType>(Arrays.asList(tokens));
        if (!stop.contains(allSymbols.get(tagIndex).getToken())) {
            return tagIndex;
        }
        int shift = dir < 0 ? 1 : -2;
        return scan(allSymbols, tagIndex, dir, tagIndex, true, stop) + shift;
    }

    private int scan(List<SoySymbol> allSymbols,
                     int tagIndex,
                     int dir,
                     int notFoundValue,
                     boolean not,
                     Collection<? extends IElementType> tokens) {
        if (dir == 0) {
            throw new IllegalArgumentException();
        }
        for (int i = tagIndex, l = allSymbols.size(); i > 0 && i < l; i += dir) {
            if (not ^ tokens.contains(allSymbols.get(i).getToken())) {
                return dir < 0 ? i : i + 1;
            }
        }
        return notFoundValue;
    }

    protected abstract IElementType[] getKeyTokens();

    private void generateRanges(List<SoySymbol> allSymbols,
                                int maxLength,
                                CharacterOmittingConsumer consumer,
                                TokenPair pair) {
        SoySymbol left = allSymbols.get(pair.getBegin());
        SoySymbol right = allSymbols.get(pair.getEnd());
        for (int i = left.getPosition(), j = Math.min(maxLength, right.getPosition() + right.getLength()); i < j; i++) {
            RangeBasedIntegerSet charsToDelete = new RangeBasedIntegerSet();
            charsToDelete.addAll(i, j - 1);
            consumer.add(charsToDelete);
        }
    }

    protected abstract TokenPair[] seekTemplateTokens(List<SoySymbol> allSymbols, int startTemplate);

    protected interface CharacterOmittingConsumer {
        void add(RangeBasedIntegerSet charsToDelete);
    }

    protected static class TokenPair {

        private final int begin;
        private final int end;

        public TokenPair(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public int getBegin() {
            return begin;
        }

        public int getEnd() {
            return end;
        }
    }
}
