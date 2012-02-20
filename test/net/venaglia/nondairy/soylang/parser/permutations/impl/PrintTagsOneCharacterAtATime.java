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

import static net.venaglia.nondairy.soylang.lexer.SoyToken.*;

import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoySymbol;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 8/14/11
 * Time: 1:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrintTagsOneCharacterAtATime extends CharacterOmittingPermutator {

    private static final IElementType[] KEY_TOKENS = { PRINT, PRINT_IMPLICIT };

    @Override
    protected IElementType[] getKeyTokens() {
        return KEY_TOKENS;
    }

    @Override
    protected TokenPair[] seekTemplateTokens(List<SoySymbol> allSymbols,
                                             int startTemplate) {
        int index = findNextOf(allSymbols, startTemplate, 1, TAG_END_RBRACE, TAG_RBRACE);
        return new TokenPair[]{ new TokenPair(startTemplate, index) };
    }
}
