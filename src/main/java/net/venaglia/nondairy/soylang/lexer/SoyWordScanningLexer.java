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

package net.venaglia.nondairy.soylang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.soylang.SoyNamesValidator;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 2:02:20 PM
 *
 * This is a specialized form of the soy lexer that breaks up dotted tokens,
 * such as namespace identifiers and absolute token identifiers. It also trims
 * the "$" that precedes parameter references.
 */
public class SoyWordScanningLexer extends FlexAdapter {

    private static final TokenSet TOKENS_TO_FRAGMENT = TokenSet.create(
            SoyToken.NAMESPACE_IDENTIFIER,
            SoyToken.TEMPLATE_IDENTIFIER,
            SoyToken.DELTEMPLATE_IDENTIFIER,
            SoyToken.PARAMETER_REF,
            SoyToken.STRING_PARAMETER_REF
    );
    
    private Deque<TokenFragment> fragmentBuffer = new LinkedList<TokenFragment>();
    
    public SoyWordScanningLexer() {
        super(new _SoyLexer());
    }

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, startOffset, endOffset, initialState);
        fragmentBuffer.clear();
    }

    @Override
    public IElementType getTokenType() {
        if (!fragmentBuffer.isEmpty()) {
            return fragmentBuffer.getFirst().type;
        }
        return super.getTokenType();
    }

    @Override
    public int getTokenStart() {
        if (!fragmentBuffer.isEmpty()) {
            return super.getTokenStart() + fragmentBuffer.getFirst().startOffset;
        }
        return super.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        if (!fragmentBuffer.isEmpty()) {
            return super.getTokenStart() + fragmentBuffer.getFirst().endOffset;
        }
        return super.getTokenEnd();
    }

    @Override
    public void advance() {
        if (!fragmentBuffer.isEmpty()) {
            fragmentBuffer.removeFirst();
        }
        if (fragmentBuffer.isEmpty()) {
            super.advance();
            IElementType type = getTokenType();
            if (TOKENS_TO_FRAGMENT.contains(type)) {
                buildFragment();
            }
        }
    }

    @Override
    public void restore(LexerPosition position) {
        super.restore(position);
        fragmentBuffer.clear();
    }

    @Override
    public CharSequence getTokenSequence() {
        if (!fragmentBuffer.isEmpty()) {
            return super.getTokenSequence().subSequence(fragmentBuffer.getFirst().startOffset,
                                                        fragmentBuffer.getFirst().endOffset);
        }
        return super.getTokenSequence();
    }

    @Override
    public String getTokenText() {
        return getTokenSequence().toString();
    }

    private void buildFragment() {
        String text = getTokenText();
        IElementType tokenType = getTokenType();
        int lastEnd = 0;
        Matcher matcher = SoyNamesValidator.MATCH_VALID_IDENTIFIER.matcher(text);
        while (matcher.find()) {
            int s = matcher.start();
            int e = matcher.end();
            if (s > lastEnd) {
                String part = text.substring(lastEnd, s);
                fragmentBuffer.add(new TokenFragment(lastEnd, s, SoyToken.IGNORED_TEXT, part));
            }
            fragmentBuffer.add(new TokenFragment(s, e, tokenType, matcher.group()));
            lastEnd = e;
        }
        if (lastEnd < text.length()) {
            fragmentBuffer.add(new TokenFragment(lastEnd, text.length(), SoyToken.IGNORED_TEXT, text.substring(lastEnd)));
        }
    }

    private class TokenFragment {
        final int startOffset;
        final int endOffset;
        final IElementType type;
        final String part;

        private TokenFragment(int startOffset, int endOffset, IElementType type, String part) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.type = type;
            this.part = part;
        }
    }
}
