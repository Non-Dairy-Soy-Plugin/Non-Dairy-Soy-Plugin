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

package net.venaglia.nondairy.soylang;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.xml.XmlTokenType.*;

/**
 * 
 */
public class SoyBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(SoyToken.TAG_END_LBRACE, SoyToken.TAG_RBRACE, true),
            new BracePair(SoyToken.TAG_LBRACE, SoyToken.TAG_END_RBRACE, true),
//            new BracePair(TAG_LBRACE, TAG_RBRACE, false),
            new BracePair(SoyToken.LBRACE, SoyToken.RBRACE, false),
            new BracePair(SoyToken.LBRACK, SoyToken.RBRACK, false),
            new BracePair(SoyToken.LPAREN, SoyToken.RPAREN, false),
            new BracePair(XML_START_TAG_START, XML_TAG_END, false),
            new BracePair(XML_COMMENT_START, XML_COMMENT_END, false)
    };

    public BracePair[] getPairs() {
        return PAIRS;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType tokenType) {
        return SoyToken.WHITESPACE_TOKENS.contains(tokenType) ||
                (!(tokenType instanceof SoyToken));
    }

    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
