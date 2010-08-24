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
import org.jetbrains.annotations.NonNls;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 14, 2010
 * Time: 9:40:08 PM
 */
abstract class BaseSoyScanner {

    @NonNls
    static final Pattern MATCH_NON_IDENTIFIER_CHAR = Pattern.compile("[^a-zA-Z0-9_.]");
    static final Map<String, SymbolTransform> EXPRESSION_TOKENS;

    static final Map<String, SoyDocCommentBuffer.InferredDataType> NON_SCALAR_TYPES_BY_COMMAND;

    static {
        Map<String, SoyDocCommentBuffer.InferredDataType> nonScalarTypesByCommand =
                new HashMap<String, SoyDocCommentBuffer.InferredDataType>();
        NON_SCALAR_TYPES_BY_COMMAND = Collections.unmodifiableMap(nonScalarTypesByCommand);

        SymbolTransform booleanSymbolTransform = new SymbolTransform(SoyToken.BOOLEAN_LITERAL) {
            @Override
            protected Object getPayload(CharSequence identifier) {
                return Boolean.parseBoolean(identifier.toString());
            }
        };
        Map<String, SymbolTransform> expressionTokens = new HashMap<String, SymbolTransform>();
        expressionTokens.put("not", new SymbolTransform(SoyToken.NOT));
        expressionTokens.put("and", new SymbolTransform(SoyToken.AND));
        expressionTokens.put("or", new SymbolTransform(SoyToken.OR));
        expressionTokens.put("true", booleanSymbolTransform); //NON-NLS
        expressionTokens.put("false", booleanSymbolTransform); //NON-NLS
        expressionTokens.put("null", new SymbolTransform(SoyToken.NULL_LITERAL)); //NON-NLS
        EXPRESSION_TOKENS = Collections.unmodifiableMap(expressionTokens);
    }

    abstract char yycharat(int pos);

    IElementType symbol(IElementType type) {
        return type;
    }

    IElementType symbol(IElementType type, Object value) {
        return type;
    }

    /**
     * assumes correct representation of a long value for
     * specified radix in scanner buffer from <code>start</code>
     * to <code>end</code>
     */
    long parseLong(int start, int end, int radix) {
        long result = 0;
        long digit;

        for (int i = start; i < end; i++) {
            digit = Character.digit(yycharat(i), radix);
            result *= radix;
            result += digit;
        }

        return result;
    }

    int yyline, yycolumn, yychar;

    boolean doubleBraceTag = false;
    boolean closeTag = false;
    boolean closeHtml = false;
    String currentNamespace = null;
    String currentTemplate = null;
    String currentCommand = null;
    int nextStateAfterCloseTag = 0;
    int nextStateAfterLiterealTag = 0;
    int nextStateAfterString = 0;
    IElementType capturedIdentifierType = SoyToken.CAPTURED_IDENTIFIER;
    char stringTerminator = '\"';
    int tagStartLine = -1;
    SoyDocCommentBuffer activeDocCommentBuffer = null;

    protected static class SymbolTransform {

        private final SoyToken token;

        public SymbolTransform(SoyToken token) {
            this.token = token;
        }

        protected Object getPayload(CharSequence identifier) {
            return null;
        }

        public IElementType toSymbol(BaseSoyScanner scanner, CharSequence identifier) {
            Object payload = getPayload(identifier);
            return scanner.symbol(token, payload);
        }
    }
}
