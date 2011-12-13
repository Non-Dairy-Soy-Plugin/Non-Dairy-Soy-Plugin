/*
   Copyright 2010 Ed Venaglia

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.venaglia.nondairy.soylang.lexer;

import static com.intellij.psi.xml.XmlTokenType.*;
import static net.venaglia.nondairy.soylang.lexer.SoyToken.*;

import java.util.regex.Matcher;
import com.intellij.psi.tree.IElementType;

@SuppressWarnings({ "ALL" })

%%

//%public
%class SoyScanner
%extends BaseSoyScanner
%implements com.intellij.lexer.FlexLexer
%function advance

%unicode
%type IElementType

%line
%column
%char

//%cup
//%cupdebug
//%cupsym SoyParserSymbols

//%eofval{
//  return symbol(SoyToken.EOF);
//%eofval}

%{
  public SoyScanner() {
    this((java.io.Reader)null);
  }
%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
DocComment = "/**" ~[^*] ~"*/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
BeginOfLineComment = ( [ \t]* "*" )? [ \t]+
//DocumentationComment = "/*" "*"+ [^/*] ~"*/"
CommentText = ( [^*\r\n]+ | !"*/" )+

/* identifiers */
ParameterRef = "$" {Identifier}
DocTag = "@" {Identifier}
ParameterDotRef = "." {Identifier}
CompoundIdentifier = {Identifier} {ParameterDotRef}+
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexDigit          = [0-9a-fA-F]
UnicodeCharLiteral = "\\u" {HexDigit} {4}

/* floating point literals */
FloatLiteral = [0-9]+ \. [0-9]+ {Exponent}?
Exponent = [e] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\'\"\\]

/* literal block close tag */
LiteralBlockText = [^{]+ | "{" [^/] | "{/" [^l] | "{/l" [^i] | "{/li" [^t] | "{/lit" [^e] | "{/lite" [^r] | "{/liter" [^a] | "{/litera" [^l] | "{/literal" [^}]
EndLiteralBlock = "{/literal}"

/* HTML classes */
HtmlIdentifier = [a-zA-Z_] [-a-zA-Z0-9_-]*
HtmlCommentText = [^-]+ | "-" [^-] | "--" [^>]

HtmlMnemonicEntityId = [a-zA-Z] [a-zA-Z0-9] {1,9}
HtmlDecimalEntityId = "#" [1-9] [0-9] {0,4} + "#0"
HtmlHexEntityId = "#x" [1-9a-fA-F] [0-9a-fA-F] {0,3} | "#x0"
HtmlEntityRef = "&" ( {HtmlMnemonicEntityId} | {HtmlDecimalEntityId} | {HtmlHexEntityId} ) ";"

%state OPEN_TAG, CLOSE_TAG, SOY_TAG, LITERAL_BLOCK, NAMESPACE_TAG, TEMPLATE_TAG, IDENTIFIER_TAG, TAG_DIRECTIVE
%state DOCS, DOCS_BOL, DOCS_IDENT, STRING, STRING_PARAM

%state HTML_INITIAL, HTML_TAG_START, HTML_TAG_END, HTML_ATTRIBUTE_NAME, HTML_ATTRIBUTE_NAME_RESUME, HTML_ATTRIBUTE_EQ
%state HTML_ATTRIBUTE_VALUE, HTML_ATTRIBUTE_VALUE_1, HTML_ATTRIBUTE_VALUE_2, HTML_COMMENT

%%

<YYINITIAL> {
//  {DocumentationComment}         { yybegin(DOCS);
//                                   yypushback(yylength() - 3);
//                                   return symbol(DOC_COMMENT);
//                                 }
  "/**"                          { yybegin(DOCS); return symbol(DOC_COMMENT); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  {EndOfLineComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  "{" "{"? [^ \t\f\r\n}]         { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = YYINITIAL;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString());
                                 }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = YYINITIAL;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString());
                                 }

  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  [^{}/ \r\n\t\f] ( [^{}\r\n]* [^{} \r\n\t\f] )? |
  [^\r\n{}/]+ | [^\r\n]          { return symbol(IGNORED_TEXT, yytext().toString()); }
  <<EOF>>                        { return null; }
}

<OPEN_TAG> {
  "/"                            { yybegin(SOY_TAG); yypushback(1); }
  "namespace" [^a-zA-Z0-9_]      { yypushback(1);
                                   yybegin(NAMESPACE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   nextStateAfterCloseTag = YYINITIAL;
                                   return symbol(closeTag ? ILLEGAL_TAG_DECLARATION : NAMESPACE); }
  "template" [^a-zA-Z0-9_]       { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : TEMPLATE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   boolean ok = closeTag || currentTemplate == null;
                                   if (closeTag) {
                                     activeDocCommentBuffer = null;
                                     currentTemplate = null;
                                     nextStateAfterCloseTag = YYINITIAL;
                                   } else {
                                     if (activeDocCommentBuffer != null) {
                                       activeDocCommentBuffer.setTemplateDeclarationLine(tagStartLine);
                                     }
                                     nextStateAfterCloseTag = HTML_INITIAL;
                                   }
                                   return symbol(ok ? TEMPLATE : UNTERMINATED_TEMPLATE); }

  /* commands */
  "print" [^a-zA-Z0-9_]          { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : PRINT); }
  "literal" [^a-zA-Z0-9_]        { yypushback(1);
                                   yybegin(CLOSE_TAG);
                                   if (closeTag) {
                                     nextStateAfterCloseTag = nextStateAfterLiterealTag;
                                   } else {
                                     nextStateAfterLiterealTag = nextStateAfterCloseTag;
                                     nextStateAfterCloseTag = LITERAL_BLOCK;
                                   }
                                   return symbol(LITERAL); }
  "msg" [^a-zA-Z0-9_]            { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(MSG); }
  "if" [^a-zA-Z0-9_]             { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(IF); }
  "elseif" [^a-zA-Z0-9_]         { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : ELSE_IF); }
  "else" [^a-zA-Z0-9_]           { yypushback(1);
                                   yybegin(CLOSE_TAG);
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : ELSE); }
  "switch" [^a-zA-Z0-9_]         { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(SWITCH); }
  "case" [^a-zA-Z0-9_]           { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : CASE); }
  "default" [^a-zA-Z0-9_]        { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : DEFAULT); }
  "foreach" [^a-zA-Z0-9_]        { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(FOREACH); }
  "ifempty" [^a-zA-Z0-9_]        { yypushback(1);
                                   yybegin(CLOSE_TAG);
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : IF_EMPTY); }
  "for" [^a-zA-Z0-9_]            { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(FOR); }
  "call" [^a-zA-Z0-9_]           { yypushback(1);
                                   capturedIdentifierType = TEMPLATE_IDENTIFIER;
                                   yybegin(closeTag ? CLOSE_TAG : IDENTIFIER_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(CALL); }
  "param" [^a-zA-Z0-9_]          { yypushback(1);
                                   capturedIdentifierType = PARAMETER_REF;
                                   yybegin(closeTag ? CLOSE_TAG : IDENTIFIER_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(PARAM); }
  "css" [^a-zA-Z0-9_]            { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : SOY_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(closeTag ? ILLEGAL_CLOSE_TAG : CSS); }

  /* special characters */
  "sp" [^a-zA-Z0-9_]             { yypushback(1); yybegin(CLOSE_TAG); return symbol(SP_LITERAL); }
  "nil" [^a-zA-Z0-9_]            { yypushback(1); yybegin(CLOSE_TAG); return symbol(NIL_LITERAL); }
  "\\r" [^a-zA-Z0-9_]            { yypushback(1); yybegin(CLOSE_TAG); return symbol(CR_LITERAL); }
  "\\n" [^a-zA-Z0-9_]            { yypushback(1); yybegin(CLOSE_TAG); return symbol(LF_LITERAL); }
  "\\t" [^a-zA-Z0-9_]            { yypushback(1); yybegin(CLOSE_TAG); return symbol(TAB_LITERAL); }
  "lb" [^a-zA-Z0-9_]             { yypushback(1); yybegin(CLOSE_TAG); return symbol(LB_LITERAL); }
  "rb" [^a-zA-Z0-9_]             { yypushback(1); yybegin(CLOSE_TAG); return symbol(RB_LITERAL); }

  "}}"                           { yybegin(currentCommand == null ? YYINITIAL : HTML_INITIAL);
                                   if (!doubleBraceTag) yypushback(1);
                                   return symbol(RBRACE_ERROR); }
  "}"                            { if (doubleBraceTag) return symbol(RBRACE);
                                   yybegin(currentCommand == null ? YYINITIAL : HTML_INITIAL);
                                   return symbol(RBRACE_ERROR); }
  .                              { yybegin(SOY_TAG);
                                   currentCommand = "";
                                   yypushback(1);
                                   return symbol(PRINT_IMPLICIT); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<CLOSE_TAG> {
  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  "/}}"                          { yybegin(nextStateAfterCloseTag);
                                   if (!doubleBraceTag) yypushback(1);
                                   return symbol(TAG_END_RBRACE);
                                 }
  "/}"                           { if (!doubleBraceTag) {
                                     yybegin(nextStateAfterCloseTag);
                                     return symbol(TAG_END_RBRACE);
                                   }
                                   yypushback(1);
                                   return symbol(DIV);
                                 }
  "}}"                           { yybegin(nextStateAfterCloseTag);
                                   if (!doubleBraceTag) yypushback(1);
                                   return symbol(TAG_RBRACE);
                                 }
  "}"                            { if (!doubleBraceTag) {
                                     yybegin(nextStateAfterCloseTag);
                                     return symbol(TAG_RBRACE);
                                   }
                                   return symbol(RBRACE);
                                 }
  .                              { yybegin(nextStateAfterCloseTag); return symbol(RBRACE_ERROR); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<NAMESPACE_TAG> {
  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  {Identifier} {ParameterDotRef}* { yybegin(SOY_TAG);
                                    if (currentNamespace == null) currentNamespace = yytext().toString();
                                    return symbol(NAMESPACE_IDENTIFIER, yytext().toString());
                                  }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<TEMPLATE_TAG> {
  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  ("."{Identifier})+             { yybegin(SOY_TAG);
                                   currentTemplate = yytext().toString();
                                   if (activeDocCommentBuffer != null) activeDocCommentBuffer.setTemplateName(currentTemplate);
                                   return symbol(TEMPLATE_IDENTIFIER, currentTemplate); }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<IDENTIFIER_TAG> {
  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  {Identifier} |
  {ParameterDotRef} |
  {CompoundIdentifier}           { yybegin(SOY_TAG);
                                   return symbol(capturedIdentifierType, yytext().toString()); }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<SOY_TAG> {

//  /* template attributes */
//  "private"                      { return symbol(PRIVATE); }
//  "autoescape"                   { return symbol(AUTOESCAPE); }

//  /* functions */
//  "isFirst"                      { return symbol(IS_FIRST); }
//  "isLast"                       { return symbol(IS_LAST); }
//  "index"                        { return symbol(INDEX); }
//  "hasData"                      { return symbol(HAS_DATA); }
//  "length"                       { return symbol(LENGTH); }
//  "round"                        { return symbol(ROUND); }
//  "floor"                        { return symbol(FLOOR); }
//  "ceiling"                      { return symbol(CEILING); }
//  "min"                          { return symbol(MIN); }
//  "max"                          { return symbol(MAX); }
//  "randomInt"                    { return symbol(RANDOM_INT); }
//  "bidiGlobalDir"                { return symbol(BIDI_GLOBAL_DIR); }
//  "bidiDirAttr"                  { return symbol(BIDI_DIR_ATTR); }
//  "bidiMark"                     { return symbol(BIDI_MARK); }
//  "bidiMarkAfter"                { return symbol(BIDI_MARK_AFTER); }
//  "bidiStartEdge"                { return symbol(BIDI_START_EDGE); }
//  "bidiEndEdge"                  { return symbol(BIDI_END_EDGE); }
//  "bidiTextDir"                  { return symbol(BIDI_TEXT_DIR); }

//  /* msg attributes */
//  "desc"                         { return symbol(DESC); }
//  "meaning"                      { return symbol(MEANING); }

//  /* call attributes */
//  "data"                         { return symbol(DATA); }

  /* boolean literals */
  "true"                         { return symbol(BOOLEAN_LITERAL, new Boolean(true)); }
  "false"                        { return symbol(BOOLEAN_LITERAL, new Boolean(false)); }

  /* null literal */
  "null"                         { return symbol(NULL_LITERAL); }

  /* separators */
  "("                            { return symbol(LPAREN); }
  ")"                            { return symbol(RPAREN); }
  "["                            { return symbol(LBRACK); }
  "]"                            { return symbol(RBRACK); }
  "{"                            { return symbol(doubleBraceTag ? LBRACE : LBRACE_ERROR); }
  "}"                            { if (doubleBraceTag) return symbol(RBRACE);
                                   yybegin(CLOSE_TAG); yypushback(1);
                                 }
  "}}"                           { if (doubleBraceTag) {
                                     yybegin(CLOSE_TAG); yypushback(2);
                                   } else {
                                     yypushback(1);
                                     return symbol(TAG_RBRACE);
                                   }
                                 }
  "/}"                           { if (doubleBraceTag) { yypushback(1); return symbol(DIV); }
                                   yybegin(CLOSE_TAG); yypushback(2); }
  "/}}"                          { if (!doubleBraceTag) yypushback(1);
                                   yybegin(CLOSE_TAG); yypushback(3); }
  "."                            { return symbol(DOT); }

  /* operators */
  "="                            { return symbol(EQ); }
  "=="                           { return symbol(EQEQ); }
  ">"                            { return symbol(GT); }
  "<"                            { return symbol(LT); }
  "not"                          { return symbol(NOT); }
  "and"                          { return symbol(AND); }
  "or"                           { return symbol(OR); }
  "?"                            { return symbol(QUESTION); }
  ":"                            { return symbol(COLON); }
  "<="                           { return symbol(LTEQ); }
  ">="                           { return symbol(GTEQ); }
  "!="                           { return symbol(NOTEQ); }
  "+"                            { return symbol(PLUS); }
  "-"                            { return symbol(MINUS); }
  "*"                            { return symbol(MULT); }
  "/"                            { return symbol(DIV); }
  "%"                            { return symbol(MOD); }
  "|"                            { yybegin(TAG_DIRECTIVE); return symbol(DIRECTIVE_PIPE); }
  ","                            { return symbol(COMMA); }

  /* string literal */
  \' {ParameterRef} \' |
  \" {ParameterRef} \"           { nextStateAfterString = yystate();
                                   yybegin(STRING_PARAM);
                                   yypushback(yylength() - 1);
                                   return symbol(STRING_LITERAL_BEGIN, yytext().toString()); }
  \' | \"                        { nextStateAfterString = yystate();
                                   yybegin(STRING);
                                   stringTerminator = yytext().charAt(0);
                                   return symbol(STRING_LITERAL_BEGIN, yytext().toString()); }

  /* numeric literals */

  /* This is matched together with the minus, because the number is too big to
     be represented by a positive integer. */
  "-2147483648"                  { return symbol(INTEGER_LITERAL, new Integer(Integer.MIN_VALUE)); }
  {DecIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer(yytext().toString())); }
  {HexIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer((int) parseLong(2, yylength(), 16))); }
  {FloatLiteral}                 { return symbol(FLOATING_POINT_LITERAL, new Double(yytext().toString())); }

  /* comments */
  {Comment}                      { return symbol(COMMENT, yytext().toString()); }

  /* whitespace */
  {WhiteSpace}                   { return symbol(WHITESPACE); }

  /* identifiers */
  {ParameterRef}                 { return symbol(PARAMETER_REF, yytext().toString().substring(1)); }
  /* function calls & identifiers */
//  {CompoundIdentifier} {WhiteSpace}* "(" |
  {Identifier} {WhiteSpace}* "(" { Matcher matcher = MATCH_NON_IDENTIFIER_CHAR.matcher(yytext());
                                   if (matcher.find()) {
                                     yypushback(yylength() - matcher.start());
                                   }
                                   String ident = yytext().toString();
                                   if ("for".equals(currentCommand) && "range".equals(ident)) return symbol(RANGE);
                                   if ("in".equals(ident) && ("for".equals(currentCommand) || "foreach".equals(currentCommand))) {
                                     return symbol(IN);
                                   }
                                   if (EXPRESSION_TOKENS.containsKey(ident)) {
                                     return EXPRESSION_TOKENS.get(ident).toSymbol(this, ident);
                                   }
                                   return symbol(CAPTURED_FUNCTION_IDENTIFIER, ident);
                                 }
  {Identifier}                   { String ident = yytext().toString();
                                   if ("in".equals(ident) && ("for".equals(currentCommand) || "foreach".equals(currentCommand))) {
                                     return symbol(IN);
                                   }
                                   return symbol(CAPTURED_IDENTIFIER, yytext().toString()); }
  .                              { return symbol(ILLEGAL_TAG_DECLARATION, yytext().toString()); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<TAG_DIRECTIVE> {
  {WhiteSpace}+                  { return symbol(WHITESPACE); }

  /* print directives */
//  "noAutoescape"                 { return symbol(NO_AUTOESCAPE); }
//  "id"                           { return symbol(ID); }
//  "escapeHtml"                   { return symbol(ESCAPE_HTML); }
//  "escapeUri"                    { return symbol(ESCAPE_URI); }
//  "escapeJs"                     { return symbol(ESCAPE_JS); }
//  "insertWordBreaks"             { return symbol(INSERT_WORD_BREAKS); }
//  "bidiSpanWrap"                 { return symbol(BIDI_SPAN_WRAP); }
//  "bidiUnicodeWrap"              { return symbol(BIDI_UNICODE_WRAP); }

  ":"                            { return symbol(DIRECTIVE_COLON); }
  "|"                            { return symbol(DIRECTIVE_PIPE); }
  ","                            { return symbol(DIRECTIVE_COMMA); }

  /* string literal */
  \' {ParameterRef} \' |
  \" {ParameterRef} \"           { nextStateAfterString = yystate();
                                   yybegin(STRING_PARAM);
                                   yypushback(yylength() - 1);
                                   return symbol(STRING_LITERAL_BEGIN, yytext().toString());
                                 }
  \' | \"                        { nextStateAfterString = yystate();
                                   yybegin(STRING);
                                   stringTerminator = yytext().charAt(0);
                                   return symbol(STRING_LITERAL_BEGIN, yytext().toString());
                                 }

  /* numeric literals */
  "-2147483648"                  { return symbol(INTEGER_LITERAL, new Integer(Integer.MIN_VALUE)); }
  {DecIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer(yytext().toString())); }
  {HexIntegerLiteral}            { return symbol(INTEGER_LITERAL, new Integer((int) parseLong(2, yylength(), 16))); }
  {FloatLiteral}                 { return symbol(FLOATING_POINT_LITERAL, new Double(yytext().toString())); }

  /* boolean literals */
  "true"                         { return symbol(BOOLEAN_LITERAL, new Boolean(true)); }
  "false"                        { return symbol(BOOLEAN_LITERAL, new Boolean(false)); }

  /* null literal */
  "null"                         { return symbol(NULL_LITERAL); }

  {Identifier} |
  {CompoundIdentifier}           { return symbol(DIRECTIVE_IDENTIFIER, yytext().toString()); }
  {ParameterRef}                 { return symbol(PARAMETER_REF, yytext().toString()); }

  .                              { yybegin(CLOSE_TAG); yypushback(1); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<LITERAL_BLOCK> {
  {LiteralBlockText}+            { return symbol(LITERAL_TEXT, yytext().toString()); }
  {EndLiteralBlock}              { yypushback(10); yybegin(currentCommand == null ? YYINITIAL : HTML_INITIAL); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DOCS> {
  {LineTerminator}               { yybegin(DOCS_BOL); return symbol(DOC_COMMENT); }
  [ \t\f]+                       { return symbol(DOC_COMMENT_WHITESPACE); }
  [^* \r\n\t\f]+ ( [ \t\f]+ [^* \r\n\t\f]+ )* |
  .                              { return symbol(DOC_COMMENT, yytext().toString()); }
  "*/"                           { yybegin(YYINITIAL); return symbol(DOC_COMMENT_END, yytext().toString()); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DOCS_BOL> {
  "@param" "?"?                  { yybegin(DOCS_IDENT); return symbol(DOC_COMMENT_TAG, yytext().toString()); }
  {DocTag}                       { yybegin(DOCS); return symbol(DOC_COMMENT_TAG, yytext().toString()); }
  {BeginOfLineComment} |
  {WhiteSpace}+                  { return symbol(DOC_COMMENT_WHITESPACE); }
  .                              { yybegin(DOCS); yypushback(1); }
  "*/"                           { yybegin(DOCS); yypushback(2); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DOCS_IDENT> {
  {Identifier}                   { yybegin(DOCS); return symbol(DOC_COMMENT_IDENTIFIER, yytext().toString()); }
  {LineTerminator}               { yybegin(DOCS_BOL); return symbol(DOC_COMMENT, yytext().toString()); }
  [ \t\f]+                       { return symbol(DOC_COMMENT_WHITESPACE, yytext().toString()); }
  [^a-zA-Z_ \t\f\r\n]            { yybegin(DOCS); yypushback(1); }
  "*/"                           { yybegin(DOCS); yypushback(2); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<STRING> {
  \' | \"                        { if (stringTerminator == yytext().charAt(0)) {
                                       yybegin(nextStateAfterString);
                                       return symbol(STRING_LITERAL_END, yytext().toString());
                                   } else {
                                       return symbol(STRING_LITERAL, yytext().toString());
                                   }
                                 }

  {StringCharacter}+             { return symbol(STRING_LITERAL, yytext().toString()); }

  /* escape sequences */
  "\\b"                          { return symbol(STRING_LITERAL_ESCAPE, "\b"); }
  "\\t"                          { return symbol(STRING_LITERAL_ESCAPE, "\t"); }
  "\\n"                          { return symbol(STRING_LITERAL_ESCAPE, "\n"); }
  "\\f"                          { return symbol(STRING_LITERAL_ESCAPE, "\f"); }
  "\\r"                          { return symbol(STRING_LITERAL_ESCAPE, "\r"); }
  "\\\""                         { return symbol(STRING_LITERAL_ESCAPE, "\""); }
  "\\'"                          { return symbol(STRING_LITERAL_ESCAPE, "\'"); }
  "\\\\"                         { return symbol(STRING_LITERAL_ESCAPE, "\\"); }
  {UnicodeCharLiteral}           { char val = (char)parseLong(2, yylength(), 16);
                                   return symbol(STRING_LITERAL_ESCAPE, String.valueOf(val));
                                 }

  /* error cases */
  \\.                            { return symbol(BAD_STRING_ESCAPE, yytext().toString()); }
  {LineTerminator}               { yybegin(SOY_TAG); return symbol(UNTERMINATED_STRING_LITERAL); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<STRING_PARAM> {
  {ParameterRef}                 { return symbol(STRING_PARAMETER_REF, yytext().toString()); }
  \' | \"                        { yybegin(nextStateAfterString); return symbol(STRING_LITERAL_END, yytext().toString()); }
}

<HTML_INITIAL> {
  {EndOfLineComment} |
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }

  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n} ]        { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_INITIAL;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_INITIAL;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  "<" [a-zA-Z_]                  { yybegin(HTML_TAG_START);
                                   closeHtml = false;
                                   yypushback(1);
                                   return symbol(XML_START_TAG_START); }
  "</" [a-zA-Z_]                 { yybegin(HTML_TAG_START);
                                   closeHtml = true;
                                   yypushback(1);
                                   return symbol(XML_END_TAG_START); }
  "<!--"                         { yybegin(HTML_COMMENT);
                                   return symbol(XML_COMMENT_START); }
  "<" | ">"                      { return symbol(XML_BAD_CHARACTER); }

  {WhiteSpace}+                  { return symbol(WHITESPACE); }
  [^{}<>&/ \r\n\t\f] ( [^{}<>&\r\n]* [^{}<>& \r\n\t\f] )? |
  [^\r\n{}<>/]+ | [^\r\n]        { return symbol(XML_DATA_CHARACTERS, yytext()); }
  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_COMMENT> {
  {HtmlCommentText}+             { return symbol(XML_COMMENT_CHARACTERS); }
  "-->"                          { yybegin(HTML_INITIAL); return symbol(XML_COMMENT_END); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_TAG_START> {
  {HtmlIdentifier} ":" {HtmlIdentifier} |
  {HtmlIdentifier}               { yybegin(closeHtml ? HTML_TAG_END : HTML_ATTRIBUTE_NAME); return symbol(XML_TAG_NAME, yytext()); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_TAG_END> {
  {WhiteSpace}+                  { return symbol(TAG_WHITE_SPACE); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_TAG_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_NAME> {
  {WhiteSpace}+                  { return symbol(TAG_WHITE_SPACE); }
  {HtmlIdentifier}               { yybegin(HTML_ATTRIBUTE_EQ); return symbol(XML_EQ); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n}]         { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_NAME_RESUME;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_NAME_RESUME;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  "/>"                           { if (closeHtml) { yypushback(1); return symbol(XML_BAD_CHARACTER); }
                                   yybegin(HTML_INITIAL); return symbol(XML_TAG_END);
                                 }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_TAG_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_NAME_RESUME> {
  {WhiteSpace}+                  { return symbol(TAG_WHITE_SPACE); }
  [-a-zA-Z0-9_-]+                { yybegin(HTML_ATTRIBUTE_EQ); return symbol(XML_EQ); }
  "="                            { yypushback(1); yybegin(HTML_ATTRIBUTE_EQ); return symbol(XML_EQ); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n}]         { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_NAME;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_NAME;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  "/>"                           { if (closeHtml) { yypushback(1); return symbol(XML_BAD_CHARACTER); }
                                   yybegin(HTML_INITIAL); return symbol(XML_TAG_END);
                                 }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_TAG_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_EQ> {
  "="                            { yybegin(HTML_ATTRIBUTE_VALUE); return symbol(XML_EQ); }
  {WhiteSpace}+                  { return symbol(TAG_WHITE_SPACE); }
  .                              { yypushback(1); yybegin(HTML_ATTRIBUTE_NAME); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE> {
  {WhiteSpace}+                  { return symbol(TAG_WHITE_SPACE); }
  {HtmlIdentifier}               { yybegin(HTML_ATTRIBUTE_NAME); return symbol(XML_ATTRIBUTE_VALUE_TOKEN); }
  \'                             { yybegin(HTML_ATTRIBUTE_VALUE_1); return symbol(XML_ATTRIBUTE_VALUE_START_DELIMITER); }
  \"                             { yybegin(HTML_ATTRIBUTE_VALUE_2); return symbol(XML_ATTRIBUTE_VALUE_START_DELIMITER); }
  .                              { yypushback(1); yybegin(HTML_ATTRIBUTE_NAME); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE_1> {
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n}]         { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_VALUE_1;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_VALUE_1;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  \'                             { yybegin(HTML_ATTRIBUTE_NAME); return symbol(XML_ATTRIBUTE_VALUE_END_DELIMITER); }

  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  [^{}<>&']+                     { return symbol(XML_ATTRIBUTE_VALUE_TOKEN, yytext()); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_BAD_CHARACTER); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE_2> {
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n}]         { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_VALUE_2;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_ATTRIBUTE_VALUE_2;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  \"                             { yybegin(HTML_ATTRIBUTE_NAME); return symbol(XML_ATTRIBUTE_VALUE_END_DELIMITER); }

  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  [^{}<>&\"]+                    { return symbol(XML_ATTRIBUTE_VALUE_TOKEN, yytext()); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_BAD_CHARACTER); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}
