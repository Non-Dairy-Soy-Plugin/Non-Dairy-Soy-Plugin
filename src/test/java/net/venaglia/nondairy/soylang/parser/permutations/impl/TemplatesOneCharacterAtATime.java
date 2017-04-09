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
 * Date: 8/9/11
 * Time: 5:32 PM
 */
public class TemplatesOneCharacterAtATime extends CharacterOmittingPermutator {

    private static final IElementType[] KEY_TOKENS = { TEMPLATE };

    @Override
    protected IElementType[] getKeyTokens() {
        return KEY_TOKENS;
    }

    @Override
    protected TokenPair[] seekTemplateTokens(List<SoySymbol> allSymbols, int startTemplate) {
        int endTemplate = findNextOf(allSymbols, startTemplate + 1, 1, TEMPLATE);
        endTemplate = findLastOf(allSymbols, endTemplate, 1, TEMPLATE, TAG_RBRACE, TAG_END_RBRACE);
        startTemplate = findLastOf(allSymbols, startTemplate, -1, TEMPLATE, TAG_LBRACE);
        endTemplate = findLastOf(allSymbols, endTemplate, 1, TEMPLATE, TAG_RBRACE, TAG_END_RBRACE);
        int beginDoc = findNextOf(allSymbols, startTemplate, -1, DOC_COMMENT_BEGIN, DOC_COMMENT);
        if (beginDoc >= 0) {
            int endDoc = findLastOf(allSymbols, beginDoc, 1, DOC_COMMENT_TOKENS.getTypes()) - 1;
            beginDoc = findLastOf(allSymbols, beginDoc, -1, DOC_COMMENT_TOKENS.getTypes());
            return new TokenPair[]{
                    new TokenPair(beginDoc, endDoc),
                    new TokenPair(startTemplate, endTemplate)
            };
        } else {
            return new TokenPair[]{
                    new TokenPair(startTemplate, endTemplate)
            };
        }
    }

}
