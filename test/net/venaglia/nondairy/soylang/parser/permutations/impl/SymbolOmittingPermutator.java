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

import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationConsumer;
import net.venaglia.nondairy.soylang.parser.permutations.Permutator;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Permutator implementation that will delete symbols from the source.
 */
public abstract class SymbolOmittingPermutator implements Permutator {

    private static final Pattern MATCH_DELETED_TEXT = Pattern.compile("\u007F+");

    @Override
    public void permutate(CharSequence source,
                          @NonNls String initialState,
                          PermutationConsumer consumer)
            throws Exception {
        if (!"YYINITIAL".equals(initialState)) {
            return;
        }

        Iterator<SoySymbol> symbolIterator = SoyScannerTest.buildScanner(source, initialState).iterator();
        List<List<SoySymbol>> allSymbols = groupSymbols(symbolIterator);
        for (int index = 0, groupedSymbolsSize = allSymbols.size(); index < groupedSymbolsSize; index++) {
            int size = allSymbols.get(index).size();
            int permutationsThisGroup = getPermutationCount(size);
            for (int seq = 0; seq < permutationsThisGroup; seq++) {
                List<List<SoySymbol>> groupedSymbols = new ArrayList<List<SoySymbol>>(allSymbols);
                List<SoySymbol> symbolGroup = new ArrayList<SoySymbol>(groupedSymbols.get(index));
                groupedSymbols.set(index, symbolGroup);
                StringBuilder modifiedSource = new StringBuilder(source);
                List<SoySymbol> toDelete = permuteSubListToDelete(symbolGroup, seq);
                for (SoySymbol symbol : toDelete) {
                    for (int j = symbol.getPosition(), k = j + symbol.getLength(); j < k; ++j) {
                        modifiedSource.setCharAt(j, '\u007F');
                    }
                }
                toDelete.clear();
                consumer.add(new GroupedSymbolIterator(groupedSymbols), modifiedSource);
            }
        }
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
        for (ListIterator<List<SoySymbol>> iter = outer.listIterator(); iter.hasNext(); ) {
            iter.set(Collections.unmodifiableList(iter.next()));
        }
        return Collections.unmodifiableList(outer);
    }

    protected abstract <T> List<T> permuteSubListToDelete(List<T> symbolGroup, int sequence);

    protected abstract int getPermutationCount(int size);

    private static class GroupedSymbolIterator implements Iterator<SoySymbol> {

        private final Iterator<List<SoySymbol>> outer;

        private Iterator<SoySymbol> inner = null;
        private SoySymbol next = null;

        GroupedSymbolIterator(List<List<SoySymbol>> groupedSymbols) {
            outer = groupedSymbols.iterator();
        }

        private void prepareNext() {
            if (next != null) return;
            while (inner == null || !inner.hasNext()) {
                if (!outer.hasNext()) return; // no more tokens
                List<SoySymbol> symbolGroup = outer.next();
                if (!symbolGroup.isEmpty()) {
                    inner = symbolGroup.iterator();
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
    }
}
