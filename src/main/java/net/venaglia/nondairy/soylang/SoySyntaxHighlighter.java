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

package net.venaglia.nondairy.soylang;

import com.intellij.ide.highlighter.HtmlFileHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyLexer;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * IntelliJ syntax highlighter implementation for soy files.
 */
public class SoySyntaxHighlighter extends SyntaxHighlighterBase {

    private static Map<IElementType, TextAttributesKey> keys1;
    private static Map<IElementType, TextAttributesKey> keys2;
    public static final HtmlFileHighlighter HTML_FILE_HIGHLIGHTER = new HtmlFileHighlighter();

    @NotNull
    public Lexer getHighlightingLexer() {
        return new SoyLexer();
    }

    static final TextAttributesKey SOY_DOC_COMMENT = TextAttributesKey.createTextAttributesKey("SOY.DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
    static final TextAttributesKey SOY_DOC_COMMENT_TAG = TextAttributesKey.createTextAttributesKey("SOY.DOC_COMMENT_TAG", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);
    static final TextAttributesKey SOY_DOC_COMMENT_IDENTIFIER = TextAttributesKey.createTextAttributesKey("SOY.DOC_COMMENT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    static final TextAttributesKey SOY_TEMPLATE_PARAMETER = TextAttributesKey.createTextAttributesKey("SOY.TEMPLATE_PARAMETER", DefaultLanguageHighlighterColors.IDENTIFIER);
    static final TextAttributesKey SOY_FUNCTION = TextAttributesKey.createTextAttributesKey("SOY.FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    static final TextAttributesKey SOY_KEYWORD = TextAttributesKey.createTextAttributesKey("SOY.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey SOY_COMMAND = TextAttributesKey.createTextAttributesKey("SOY.COMMAND", DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey SOY_SPECIAL_CHAR = TextAttributesKey.createTextAttributesKey("SOY.SPECIAL_CHAR", DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey SOY_NAMESPACE_ID = TextAttributesKey.createTextAttributesKey("SOY.NAMESPACE_ID", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    static final TextAttributesKey SOY_TEMPLATE_ID = TextAttributesKey.createTextAttributesKey("SOY.TEMPLATE_ID", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    static final TextAttributesKey SOY_DIRECTIVE_IDENTIFIER = TextAttributesKey.createTextAttributesKey("SOY.DIRECTIVE", HighlighterColors.TEXT);
    static final TextAttributesKey SOY_DIRECTIVE_OPERATOR = TextAttributesKey.createTextAttributesKey("SOY.DIRECTIVE_OPERATOR", DefaultLanguageHighlighterColors.DOT);
    static final TextAttributesKey SOY_STRING = TextAttributesKey.createTextAttributesKey("SOY.STRING", DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey SOY_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("SOY.STRING_ESCAPE", DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey SOY_BAD_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey("SOY.BAD_STRING_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    static final TextAttributesKey SOY_NUMBER = TextAttributesKey.createTextAttributesKey("SOY.NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    static final TextAttributesKey SOY_OPERATOR = TextAttributesKey.createTextAttributesKey("SOY.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    static final TextAttributesKey SOY_TAG_BRACES = TextAttributesKey.createTextAttributesKey("SOY.TAG_BRACES", DefaultLanguageHighlighterColors.BRACES);
    static final TextAttributesKey SOY_BRACES = TextAttributesKey.createTextAttributesKey("SOY.BRACES", DefaultLanguageHighlighterColors.BRACES);
    static final TextAttributesKey SOY_BRACKETS = TextAttributesKey.createTextAttributesKey("SOY.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    static final TextAttributesKey SOY_PARENTHS = TextAttributesKey.createTextAttributesKey("SOY.PARENTHS", DefaultLanguageHighlighterColors.PARENTHESES);
    static final TextAttributesKey SOY_TEMPLATE_CONTENT = TextAttributesKey.createTextAttributesKey("SOY.TEMPLATE_CONTENT", HighlighterColors.TEXT);
    static final TextAttributesKey SOY_IGNORE = TextAttributesKey.createTextAttributesKey("SOY.BAD", HighlighterColors.BAD_CHARACTER);
    static final TextAttributesKey SOY_BAD = TextAttributesKey.createTextAttributesKey("SOY.IGNORE", HighlighterColors.BAD_CHARACTER);

    static {
        keys1 = new HashMap<IElementType, TextAttributesKey>();
        keys2 = new HashMap<IElementType, TextAttributesKey>();

        SyntaxHighlighterBase.fillMap(keys1, SoyToken.DOC_COMMENT_TOKENS, SOY_DOC_COMMENT);
        fillMap(keys1, SoyToken.CAPTURED_TEXT, SOY_TEMPLATE_CONTENT);
        fillMap(keys1, SoyToken.KEYWORDS, SOY_KEYWORD);
        fillMap(keys1, SoyToken.COMMANDS, SOY_COMMAND);
        fillMap(keys1, SoyToken.SPECIAL_CHARS, SOY_SPECIAL_CHAR);
        fillMap(keys1, SoyToken.DIRECTIVE_OPERATORS, SOY_DIRECTIVE_OPERATOR);
        fillMap(keys1, SoyToken.NUMERIC_LITERALS, SOY_NUMBER);
        fillMap(keys2, SoyToken.STRING_LITERAL_TOKENS, SOY_STRING);
        fillMap(keys1, SoyToken.PARENS, SOY_PARENTHS);
        fillMap(keys1, SoyToken.BRACKETS, SOY_BRACKETS);
        fillMap(keys1, SoyToken.TAG_BRACES, SOY_TAG_BRACES);
        fillMap(keys1, SoyToken.BRACES, SOY_BRACES);
        fillMap(keys1, SoyToken.OPERATORS, SOY_OPERATOR);
        fillMap(keys1, SoyToken.ILLEGALS, SOY_BAD);

        keys1.put(SoyToken.IGNORED_TEXT, SOY_IGNORE);
        keys1.put(SoyToken.DIRECTIVE_IDENTIFIER, SOY_DIRECTIVE_IDENTIFIER);
        keys1.put(SoyToken.COMMENT, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
        keys1.put(SoyToken.LINE_COMMENT, DefaultLanguageHighlighterColors.LINE_COMMENT);
        keys1.put(SoyToken.STRING_LITERAL, SOY_STRING);
        keys1.put(SoyToken.STRING_LITERAL_ESCAPE, SOY_STRING_ESCAPE);
        keys1.put(SoyToken.PARAMETER_REF, SOY_TEMPLATE_PARAMETER);
        keys1.put(SoyToken.LET_IDENTIFIER, SOY_TEMPLATE_PARAMETER);
        keys1.put(SoyToken.NAMESPACE_IDENTIFIER, SOY_NAMESPACE_ID);
        keys1.put(SoyToken.TEMPLATE_IDENTIFIER, SOY_TEMPLATE_ID);
        keys1.put(SoyToken.DELTEMPLATE_IDENTIFIER, SOY_TEMPLATE_ID);
        keys1.put(SoyToken.CAPTURED_FUNCTION_IDENTIFIER, SOY_FUNCTION);

        keys2.put(SoyToken.DOC_COMMENT_TAG, SOY_DOC_COMMENT_TAG);
        keys2.put(SoyToken.DOC_COMMENT_PARAM_TAG, SOY_DOC_COMMENT_TAG);
        keys2.put(SoyToken.DOC_COMMENT_IDENTIFIER, SOY_DOC_COMMENT_IDENTIFIER);
        keys2.put(SoyToken.STRING_PARAMETER_REF, SOY_TEMPLATE_PARAMETER);
        keys2.put(SoyToken.BAD_STRING_ESCAPE, SOY_BAD_STRING_ESCAPE);
        keys2.put(SoyToken.BRACE_IN_STRING, SOY_BAD_STRING_ESCAPE);
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType instanceof SoyToken) {
            return pack(keys1.get(tokenType), keys2.get(tokenType) );
        } else {
            return HTML_FILE_HIGHLIGHTER.getTokenHighlights(tokenType);
        }
    }
}
