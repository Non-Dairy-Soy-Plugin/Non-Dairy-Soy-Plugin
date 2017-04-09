/*
 * Copyright 2010 - 2013 Ed Venaglia
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

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import net.venaglia.nondairy.soylang.lexer.cupparser.SoyParserSymbols;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 11, 2010
 * Time: 8:44:33 AM
 */
public class TestableSoyScanner extends SoyScanner implements Iterable<SoySymbol>, Scanner {

    private static Set<IElementType> INPUT_TOKENS_TO_SKIP;

    static {
        SoyToken.importParserValues(SoyParserSymbols.class);
        Set<IElementType> inputTokensToSkip = new HashSet<IElementType>();
        inputTokensToSkip.add(SoyToken.WHITESPACE);
        inputTokensToSkip.add(SoyToken.DOC_COMMENT_WHITESPACE);
        inputTokensToSkip.add(SoyToken.COMMENT);
        inputTokensToSkip.add(SoyToken.LINE_COMMENT);
        inputTokensToSkip.add(SoyToken.IGNORED_TEXT);
        inputTokensToSkip.add(XmlTokenType.TAG_WHITE_SPACE);
        inputTokensToSkip.add(XmlTokenType.XML_WHITE_SPACE);
        INPUT_TOKENS_TO_SKIP = Collections.unmodifiableSet(inputTokensToSkip);
    }

    private final SoyToken dummyTokenForNonSoyTokens;

    private Runnable doReset = null;

    public TestableSoyScanner() {
        this(null);
    }

    public TestableSoyScanner(@Nullable SoyToken dummyTokenForNonSoyTokens) {
        this.dummyTokenForNonSoyTokens = dummyTokenForNonSoyTokens;
    }

    public Symbol next_token() throws IOException {
        //noinspection SuspiciousMethodCalls
        do {
            lastSymbol = null;
            advance();
            testToEnsureLexerAdvances(lastSymbol);
        } while (lastSymbol != null && INPUT_TOKENS_TO_SKIP.contains(lastSymbol.getToken()));
        return lastSymbol;
    }

    private SoySymbol previousSymbol = null;
    private int symbolRepeatCount = 0;

    private void testToEnsureLexerAdvances(SoySymbol current) {
        if (previousSymbol == null || current == null ||
            previousSymbol.getToken() != current.getToken() ||
            previousSymbol.getPosition() != current.position) {
            previousSymbol = current;
            symbolRepeatCount = 0;
        } else {
            if (++symbolRepeatCount >= 50) {
                Assert.fail("Lexer is not advancing, the same symbol has been generated 50 times: " + previousSymbol); //NON-NLS
            }
        }
    }

    public void reset() {
        if (doReset != null) doReset.run();
    }

    public void reset(CharSequence buffer) {
        reset(buffer, 0, buffer.length(), YYINITIAL);
    }

    @Override
    public void reset(final CharSequence buffer, final int start, final int end, final int initialState) {
        doReset = new Runnable() {
            public void run() {
                _reset(buffer, start, end, initialState);
                yychar = 0;
                yycolumn = 0;
                yyline = 0;
            }
        };
        doReset.run();
    }

    private void _reset(CharSequence buffer, int start, int end, int initialState) {
        super.reset(buffer, start, end, initialState);
    }

    private SoySymbol lastSymbol = null;

    IElementType symbol(IElementType type) {
        IElementType symbolType = type instanceof SoyToken || dummyTokenForNonSoyTokens == null ? type : dummyTokenForNonSoyTokens;
        lastSymbol = symbolType instanceof SoyToken
                     ? new SoySymbol((SoyToken)symbolType, yystate(), yyline + 1, yycolumn + 1, yychar, yylength())
                     : new SoySymbol(symbolType, yystate(), yyline + 1, yycolumn + 1, yychar, yylength());
        return symbolType;
    }

    IElementType symbol(IElementType type, Object value) {
        IElementType symbolType = type instanceof SoyToken || dummyTokenForNonSoyTokens == null ? type : dummyTokenForNonSoyTokens;
        lastSymbol = symbolType instanceof SoyToken
                     ? new SoySymbol((SoyToken)symbolType, yystate(), yyline + 1, yycolumn + 1, yychar, yylength(), value)
                     : new SoySymbol(symbolType, yystate(), yyline + 1, yycolumn + 1, yychar, yylength(), value);
        return symbolType;
    }

    public Iterator<SoySymbol> iterator() {
        return new Iterator<SoySymbol>() {
            SoySymbol n = null;

            public boolean hasNext() {
                if (n == null) {
                    try {
                        n = (SoySymbol)next_token();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return n != null && n.getToken() != SoyToken.EOF;
            }

            public SoySymbol next() {
                if (!hasNext()) throw new NoSuchElementException();
                SoySymbol t = n;
                n = null;
                return t;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
