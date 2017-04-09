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

package net.venaglia.nondairy.soylang.parser.permutations.impl;

import net.venaglia.nondairy.soylang.lexer.SoyScannerTest;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationConsumer;
import net.venaglia.nondairy.soylang.parser.permutations.Permutator;
import org.jetbrains.annotations.NonNls;

import java.util.Iterator;

/**
 * Permutator that parses the source text and creates a single permutation
 */
public class SingletonPermutator implements Permutator {

    @Override
    public void permutate(CharSequence source,
                          @NonNls String initialState,
                          PermutationConsumer consumer)
            throws Exception {
        Iterator<SoySymbol> symbolIterator = SoyScannerTest.buildScanner(source, initialState).iterator();
        consumer.add(symbolIterator, source);
    }
}
