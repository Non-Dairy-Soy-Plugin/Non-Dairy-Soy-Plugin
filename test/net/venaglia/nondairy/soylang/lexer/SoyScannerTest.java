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

import static junit.framework.Assert.*;

import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.soylang.lexer.cupparser.SoyParser;
import net.venaglia.nondairy.soylang.lexer.cupparser.SoyParserSymbols;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import org.jetbrains.annotations.NonNls;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 17, 2010
 * Time: 12:10:31 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class SoyScannerTest {

    private static final Pattern MATCH_BAD_TOKEN_NAME = Pattern.compile("BAD|UNTERMINATED|UNEXPECTED|ILLEGAL|INVALID|ERROR");

    private static void printf(String s, Object... args) {
        println(String.format(s, args));
    }

    private static void println(Object o) {
        System.out.println(o);
    }

    private static TestableSoyScanner buildScanner(String name) throws Exception {
        String testSource = SoyTestUtil.getTestSourceBuffer(name);
        TestableSoyScanner testableSoyScanner = buildScanner(testSource, SoyScanner.YYINITIAL);
        int start;
        if (testSource.startsWith("/*") && (start = testSource.indexOf("*/", 2)) > 0) {
            testSource = testSource.substring(start + 2);
        }
        printf("Preparing test source: [%d chars] %s%n", testSource.length(), testSource);
        return testableSoyScanner;
    }

    public static TestableSoyScanner buildScanner(CharSequence testSource, @NonNls String initialState) throws Exception {
        return buildScanner(testSource, (Integer)SoyScanner.class.getField(initialState).get(null));
    }

    private static TestableSoyScanner buildScanner(CharSequence testSource, int initialState) throws Exception {
        TestableSoyScanner scanner = new TestableSoyScanner();
        scanner.reset(testSource, initialState);
        return scanner;
    }

    @Test
    public void testMinimal() throws Exception {
        TestableSoyScanner scanner = buildScanner("minimal.soy");
        tallyTokens(scanner, false);
    }

    @Test
    public void testExample() throws Exception {
        TestableSoyScanner scanner = buildScanner("example.soy");
        tallyTokens(scanner, false);
    }

    @Test
    public void testEdgeCases() throws Exception {
        TestableSoyScanner scanner = buildScanner("edge-cases.soy");
        tallyTokens(scanner, false);
    }

    @Test
    public void testErrorCases() throws Exception {
        TestableSoyScanner scanner = buildScanner("error-cases.soy");
        tallyTokens(scanner, true);
    }

    @Test
    public void testFeatures() throws Exception {
        TestableSoyScanner scanner = buildScanner("features.soy");
        Map<SoyToken, Integer> tokenCounts = tallyTokens(scanner, false);
        StringBuffer hashBuffer = new StringBuffer();
        boolean first = true;
        for (Map.Entry<SoyToken,Integer> entry : tokenCounts.entrySet()) {
            if (first) first = false; else hashBuffer.append("\n");
            hashBuffer.append(entry.getKey());
            hashBuffer.append("=");
            hashBuffer.append(entry.getValue());
        }
        // todo: assert token results are consistent
    }

    private Map<SoyToken,Integer> tallyTokens(TestableSoyScanner scanner, boolean expectErrors) {
        List<SoySymbol> tokens = new LinkedList<SoySymbol>();
        Map<SoyToken,Integer> tokenCounts = new HashMap<SoyToken,Integer>();
        Set<SoyToken> badTokens = new HashSet<SoyToken>();
        int good = 0, bad = 0;
        for (SoySymbol token : scanner) {
            tokens.add(token);
            if (!(token.getToken() instanceof SoyToken)) continue;
            SoyToken soyToken = (SoyToken)token.getToken();
            if (MATCH_BAD_TOKEN_NAME.matcher(soyToken.toString()).find()) {
                badTokens.add(soyToken);
                bad++;
            } else {
                good++;
            }
            if (badTokens.contains(soyToken)) {
                System.err.println(toString(token));
            }
            tokenCounts.put(soyToken, tokenCounts.containsKey(soyToken) ? tokenCounts.get(soyToken) + 1 : 1);
        }
        printf("Found %d tokens - %d good, %d bad%n", (good + bad), good, bad);
        for (SoyToken soyToken : badTokens) {
            println(soyToken.toString() + " count: " + tokenCounts.get(soyToken));
        }
        for (SoySymbol token : tokens) {
//            if (!badTokens.contains(token.getToken())) continue;
            println(toString(token));
        }
        if (expectErrors) {
            assertTrue(bad > 0);
        } else {
            assertEquals(0, bad);
        }
        return tokenCounts;
    }

    private String toString(SoySymbol token) {
        if (token == null) {
            return "\t[null]";
        } else if (token.getPayload() instanceof String) {
            return String.format("\t%s %s", token, SoyTestUtil.toSource((String)token.getPayload()));
        } else if (token.getPayload() instanceof SoyDocCommentBuffer) {
            return String.format("\t%s \"%s\"", token, token.getPayload());
        } else {
            return String.format("\t%s", token);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Below are tests that use a CUP generated parser to ensure a sane token
    // sequence is generated by the lexer.

    private SoyParser buildParser(String name) throws Exception {
        CharSequence testSource = SoyTestUtil.getTestSourceBuffer(name);
        TestableSoyScanner scanner = new TestableSoyScanner(SoyToken.TEMPLATE_TEXT) {
            @Override
            public Symbol next_token() throws IOException {
                Symbol symbol = super.next_token();
                System.out.println("next_token() -> " + symbol);
                return symbol;
            }
        };
        SoyParser parser = new SoyParser(scanner, new ComplexSymbolFactory());
        scanner.reset(testSource);
        return parser;
    }

    private void write(Symbol symbol) {
        Object payload = symbol.value;
        if (payload instanceof String) payload = SoyTestUtil.toSource((String)payload);
        System.out.printf("%s = %s -- <%s>%n", symbol, payload, symbol.getClass().getSimpleName());
    }

    private void dumpTokensToStdout(SoyParser parser, boolean withDebugOutput) throws Exception {
        Symbol symbol;
        while ((symbol = withDebugOutput ? parser.debug_parse() : parser.parse()).sym != SoyParserSymbols.EOF) {
            write(symbol);
        }
        write(symbol);
    }

    @Test
    public void testMinimal_cup() throws Exception {
        dumpTokensToStdout(buildParser("minimal.soy"), true);
    }

    @Test
    public void testFeatures_cup() throws Exception {
        dumpTokensToStdout(buildParser("features.soy"), true);
    }

    @Test
    public void testExample_cup() throws Exception {
        dumpTokensToStdout(buildParser("example.soy"), false);
    }

    @Test
    public void testEdgeCases_cup() throws Exception {
        dumpTokensToStdout(buildParser("edge-cases.soy"), true);
    }
}
