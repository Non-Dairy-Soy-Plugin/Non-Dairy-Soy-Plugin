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

package net.venaglia.nondairy.soylang.lexer;

import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.cupparser.SoyParserSymbols;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;

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

    private static Set<SoyToken> INPUT_TOKENS_TO_SKIP;

    static {
        SoyToken.importParserValues(SoyParserSymbols.class);
        Set<SoyToken> inputTokensToSkip = new HashSet<SoyToken>();
        inputTokensToSkip.add(SoyToken.WHITESPACE);
        inputTokensToSkip.add(SoyToken.COMMENT);
        inputTokensToSkip.add(SoyToken.IGNORED_TEXT);
        INPUT_TOKENS_TO_SKIP = Collections.unmodifiableSet(inputTokensToSkip);
    }

    private final SoyToken dummyTokenForNonSoyTokens;

    private Runnable doReset = null;

    public TestableSoyScanner() {
        this(null);
    }

    public TestableSoyScanner(SoyToken dummyTokenForNonSoyTokens) {
        this.dummyTokenForNonSoyTokens = dummyTokenForNonSoyTokens;
    }

    public Symbol next_token() throws IOException {
        do {
            lastSymbol = null;
            advance();
        } while (lastSymbol != null && INPUT_TOKENS_TO_SKIP.contains(lastSymbol.getToken()));
        return lastSymbol;
    }

    public void reset() {
        if (doReset != null) doReset.run();
    }

    public void reset(CharSequence buffer) {
        reset(buffer, YYINITIAL);
    }

    @Override
    public void reset(final CharSequence buffer, final int initialState) {
        doReset = new Runnable() {
            public void run() {
                _reset(buffer, initialState);
            }
        };
        doReset.run();
    }

    @Override
    public void reset(final CharSequence buffer, final int start, final int end, final int initialState) {
        doReset = new Runnable() {
            public void run() {
                _reset(buffer, start, end, initialState);
            }
        };
        doReset.run();
    }

    private void _reset(CharSequence buffer, int initialState) {
        super.reset(buffer, initialState);
    }

    private void _reset(CharSequence buffer, int start, int end, int initialState) {
        super.reset(buffer, start, end, initialState);
    }

    private SoySymbol lastSymbol = null;

    IElementType symbol(IElementType type) {
        IElementType symbolType = type instanceof SoyToken || dummyTokenForNonSoyTokens == null ? type : dummyTokenForNonSoyTokens;
        lastSymbol = symbolType instanceof SoyToken
                     ? new SoySymbol((SoyToken)symbolType, yystate(), yycolumn + 1, yyline + 1, yychar, yylength())
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
