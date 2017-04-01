/*
   Copyright 2010 - 2013 Ed Venaglia

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
HtmlCDataText = [^\]\{\}]+ | "]" [^\]\{\}] | "]]" [^>\{\}]

HtmlMnemonicEntityId = [a-zA-Z] [a-zA-Z0-9] {1,9}
HtmlDecimalEntityId = "#" [1-9] [0-9] {0,4} + "#0"
HtmlHexEntityId = "#x" [1-9a-fA-F] [0-9a-fA-F] {0,3} | "#x0"
HtmlEntityRef = "&" ( {HtmlMnemonicEntityId} | {HtmlDecimalEntityId} | {HtmlHexEntityId} ) ";"

%state OPEN_TAG, CLOSE_TAG, SOY_TAG, LITERAL_BLOCK, DELPACKAGE_TAG, NAMESPACE_TAG, TEMPLATE_TAG, DELTEMPLATE_TAG, LET_TAG, IDENTIFIER_TAG, TAG_DIRECTIVE
%state DOCS, DOCS_BOL, DOCS_IDENT, STRING, STRING_PARAM, STRING_IN_SINGLE_BRACES, STRING_IN_DOUBLE_BRACES

%state HTML_INITIAL, HTML_TAG_START, HTML_TAG_END, HTML_ATTRIBUTE_NAME, HTML_ATTRIBUTE_NAME_RESUME, HTML_ATTRIBUTE_EQ
%state HTML_ATTRIBUTE_VALUE, HTML_ATTRIBUTE_VALUE_1, HTML_ATTRIBUTE_VALUE_2, HTML_COMMENT
%state HTML_DOCTYPE, HTML_DIRECTIVE, HTML_CDATA

%state AFTER_WHITESPACE

%%

<YYINITIAL> {
  "/**"                          { yybegin(DOCS); return symbol(DOC_COMMENT_BEGIN); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  {EndOfLineComment}             { return symbol(LINE_COMMENT, yytext().toString()); }
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

  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  [^{}/ \r\n\t\f] ( [^{}\r\n]* [^{} \r\n\t\f] )? |
  [^\r\n{}/]+ | [^\r\n]          { return symbol(IGNORED_TEXT, yytext().toString()); }
  <<EOF>>                        { return null; }
}

<OPEN_TAG> {
  "/"                            { yybegin(SOY_TAG); yypushback(1); }
  "delpackage" [^a-zA-Z0-9_]     { yypushback(1);
                                   yybegin(DELPACKAGE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   nextStateAfterCloseTag = YYINITIAL;
                                   return symbol(closeTag ? ILLEGAL_TAG_DECLARATION : DELPACKAGE); }
  "namespace" [^a-zA-Z0-9_]      { yypushback(1);
                                   yybegin(NAMESPACE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   nextStateAfterCloseTag = YYINITIAL;
                                   return symbol(closeTag ? ILLEGAL_TAG_DECLARATION : NAMESPACE); }
  "alias" [^a-zA-Z0-9_]          { yypushback(1);
                                   yybegin(NAMESPACE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   nextStateAfterCloseTag = YYINITIAL;
                                   return symbol(closeTag ? ILLEGAL_TAG_DECLARATION : ALIAS); }
  "template" [^a-zA-Z0-9_]       { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : TEMPLATE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   boolean ok = closeTag || currentTemplate == null;
                                   if (closeTag) {
                                     currentTemplate = null;
                                     nextStateAfterCloseTag = YYINITIAL;
                                   } else {
                                     nextStateAfterCloseTag = HTML_INITIAL;
                                   }
                                   return symbol(ok ? TEMPLATE : UNTERMINATED_TEMPLATE); }
  "deltemplate" [^a-zA-Z0-9_]    { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : DELTEMPLATE_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   boolean ok = closeTag || currentTemplate == null;
                                   if (closeTag) {
                                     currentTemplate = null;
                                     nextStateAfterCloseTag = YYINITIAL;
                                   } else {
                                     nextStateAfterCloseTag = HTML_INITIAL;
                                   }
                                   return symbol(ok ? DELTEMPLATE : UNTERMINATED_TEMPLATE); }

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
  "let" [^a-zA-Z0-9_]            { yypushback(1);
                                   yybegin(closeTag ? CLOSE_TAG : LET_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(LET); }
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
  "delcall" [^a-zA-Z0-9_]        { yypushback(1);
                                   capturedIdentifierType = DELTEMPLATE_IDENTIFIER;
                                   yybegin(closeTag ? CLOSE_TAG : IDENTIFIER_TAG);
                                   currentCommand = closeTag ? null : yytext().toString();
                                   return symbol(DELCALL); }
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
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
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
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  {Identifier} {ParameterDotRef}* { yybegin(SOY_TAG);
                                    if (currentNamespace == null) currentNamespace = yytext().toString();
                                    return symbol(NAMESPACE_IDENTIFIER, yytext().toString());
                                  }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DELPACKAGE_TAG> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  {Identifier} {ParameterDotRef}* { yybegin(SOY_TAG);
                                    if (currentNamespace == null) currentNamespace = yytext().toString();
                                    return symbol(PACKAGE_IDENTIFIER, yytext().toString());
                                  }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<TEMPLATE_TAG> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  "."{Identifier}                { yybegin(SOY_TAG);
                                   currentTemplate = yytext().toString();
                                   return symbol(TEMPLATE_IDENTIFIER, currentTemplate); }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  {Identifier}                   { yybegin(SOY_TAG); yypushback(yylength()); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DELTEMPLATE_TAG> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  {Identifier} {ParameterDotRef}*
                                 { yybegin(SOY_TAG);
                                   currentTemplate = yytext().toString();
                                   return symbol(DELTEMPLATE_IDENTIFIER, currentTemplate); }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<LET_TAG> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
  "$" |
  {ParameterRef} |
  {Identifier}                   { yybegin(SOY_TAG);
                                   return symbol(LET_IDENTIFIER, yytext().toString()); }
  "}"                            { yybegin(CLOSE_TAG); yypushback(1); }
  .                              { yybegin(SOY_TAG); yypushback(1); return symbol(ILLEGAL_TAG_DECLARATION); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<IDENTIFIER_TAG> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }
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

  /* empty object literal */
  "[]"                           { return symbol(EMPTY_ARRAY_LITERAL); }
  "[:]"                          { return symbol(EMPTY_OBJECT_LITERAL); }

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
  "?."                           { return symbol(QUESTION_DOT); }
  "."                            { return symbol(DOT); }

  /* operators */
  "="                            { return symbol(EQ); }
  "=="                           { return symbol(EQEQ); }
  ">"                            { return symbol(GT); }
  "<"                            { return symbol(LT); }
  "not"                          { return symbol(NOT); }
  "and"                          { return symbol(AND); }
  "or"                           { return symbol(OR); }
  "?:"                           { return symbol(ELVIS); }
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
  {WhiteSpace}                   { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }

  /* identifiers */
  {ParameterRef}                 { return symbol(PARAMETER_REF, yytext().toString().substring(1)); }
  /* function calls & identifiers */
  {Identifier} {WhiteSpace}* "(" { Matcher matcher = MATCH_NON_IDENTIFIER_CHAR.matcher(yytext());
                                   if (matcher.find()) {
                                     yypushback(yylength() - matcher.start());
                                   }
                                   String ident = yytext().toString();
                                   if ("for".equals(currentCommand) && "range".equals(ident)) return symbol(RANGE);
                                   if ("in".equals(ident) && ("for".equals(currentCommand) || "foreach".equals(currentCommand))) {
                                     return symbol(IN);
                                   }
                                   if ("as".equals(ident) && "alias".equals(currentCommand)) {
                                     return symbol(AS);
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
                                  if ("as".equals(ident) && "alias".equals(currentCommand)) {
                                    return symbol(AS);
                                  }
                                   return symbol(CAPTURED_IDENTIFIER, yytext().toString()); }
  .                              { return symbol(ILLEGAL_TAG_DECLARATION, yytext().toString()); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<TAG_DIRECTIVE> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(WHITESPACE); }

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
  {LineTerminator}               { yybegin(DOCS_BOL); return symbol(DOC_COMMENT_EOL); }
  [ \t\f]+                       { return symbol(DOC_COMMENT_WHITESPACE); }
  [^* \r\n\t\f]+ ( [ \t\f]+ [^* \r\n\t\f]+ )* |
  .                              { return symbol(DOC_COMMENT, yytext().toString()); }
  "*/"                           { yybegin(YYINITIAL); return symbol(DOC_COMMENT_END, yytext().toString()); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DOCS_BOL> {
  "@param" "?"?                  { yybegin(DOCS_IDENT); return symbol(DOC_COMMENT_PARAM_TAG, yytext().toString()); }
  {DocTag}                       { yybegin(DOCS); return symbol(DOC_COMMENT_TAG, yytext().toString()); }
  {EndOfLineComment}             { return symbol(LINE_COMMENT, yytext().toString()); }
  {LineTerminator}               { return symbol(DOC_COMMENT_EOL); }
  {BeginOfLineComment} |
  {WhiteSpace}+                  { return symbol(DOC_COMMENT_WHITESPACE); }
  .                              { yybegin(DOCS); yypushback(1); }
  "*/"                           { yybegin(DOCS); yypushback(2); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<DOCS_IDENT> {
  "$" {Identifier}               { yypushback(yylength() - 1);
                                   return symbol(DOC_COMMENT_BAD_CHARACTER); }
  {Identifier}                   { yybegin(DOCS); return symbol(DOC_COMMENT_IDENTIFIER, yytext().toString()); }
  {LineTerminator}               { yybegin(DOCS_BOL);
                                   return symbol(DOC_COMMENT_EOL, yytext().toString()); }
  [ \t\f]+                       { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(DOC_COMMENT_WHITESPACE, yytext().toString()); }
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

  {StringCharacter}+             { String text = yytext().toString();
                                   boolean foundBrace = false;
                                   for (int i = 0, l = text.length(); i < l && !foundBrace; ++i) {
                                       char c = text.charAt(i);
                                       foundBrace = c == '{' || c == '}';
                                   }
                                   if (foundBrace) {
                                       yypushback(text.length());
                                       if (doubleBraceTag) {
                                           yybegin(STRING_IN_DOUBLE_BRACES);
                                       } else {
                                           yybegin(STRING_IN_SINGLE_BRACES);
                                       }
                                   } else {
                                       return symbol(STRING_LITERAL, yytext().toString());
                                   }
                                 }

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
  "\\"                           { return symbol(BAD_STRING_ESCAPE, "\\"); }
  {LineTerminator}               { yybegin(SOY_TAG); return symbol(UNTERMINATED_STRING_LITERAL); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<STRING_IN_DOUBLE_BRACES> {
  "}" "}"                        { yypushback(2); yybegin(nextStateAfterString); }
  [^\r\n\'\"\\\}]+ "}" "}"       { String text = yytext().toString();
                                   yypushback(2);
                                   return symbol(STRING_LITERAL, text.substring(0, text.length() - 2));
                                 }
  [^\r\n\'\"\\]+                 { yybegin(STRING);
                                   return symbol(STRING_LITERAL, yytext().toString());
                                 }
  .                              { yypushback(1); yybegin(STRING); }
  <<EOF>>                        { yybegin(STRING); }
}

<STRING_IN_SINGLE_BRACES> {
  "{"                            { return symbol(BRACE_IN_STRING, yytext().toString()); }
  "}"                            { yypushback(1); yybegin(nextStateAfterString); }
  [^\r\n\'\"\\\{\}]+ "{" |
  [^\r\n\'\"\\\{\}]+ "}"         { String text = yytext().toString();
                                   yypushback(1);
                                   return symbol(STRING_LITERAL, text.substring(0, text.length() - 1));
                                 }
  [^\r\n\'\"\\]+                 { yybegin(STRING);
                                   return symbol(STRING_LITERAL, yytext().toString());
                                 }
  .                              { yypushback(1); yybegin(STRING); }
  <<EOF>>                        { yybegin(STRING); }
}

<STRING_PARAM> {
  {ParameterRef}                 { return symbol(STRING_PARAMETER_REF, yytext().toString()); }
  \' | \"                        { yybegin(nextStateAfterString); return symbol(STRING_LITERAL_END, yytext().toString()); }
}

<HTML_INITIAL> {
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
  "<!DOCTYPE"                    { yybegin(HTML_DOCTYPE);
                                   return symbol(XML_DOCTYPE_START); }
  "<![CDATA["                    { yybegin(HTML_CDATA);
                                   return symbol(XML_CDATA_START); }
  "<?"                           { yybegin(HTML_DIRECTIVE);
                                   return symbol(XML_DECL_START); }
  "<" | ">"                      { return symbol(XML_BAD_CHARACTER); }

  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(XML_WHITE_SPACE); }
  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  [^{}<>&/ \r\n\t\f] ( [^{}<>&\r\n]* [^{}<>& \r\n\t\f] )? |
  [^\r\n&{}<>/]+ | [^&\r\n]      { return symbol(XML_DATA_CHARACTERS, yytext()); }
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
  {HtmlIdentifier}               { nextStateAfterHtmlAttribute = HTML_INITIAL;
                                   yybegin(closeHtml ? HTML_TAG_END : HTML_ATTRIBUTE_NAME);
                                   return symbol(XML_TAG_NAME, yytext()); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_TAG_END> {
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_TAG_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_NAME> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  {HtmlIdentifier}               { yybegin(HTML_ATTRIBUTE_EQ); return symbol(XML_NAME); }
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
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
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
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  "="                            { nextStateAfterHtmlAttribute = HTML_ATTRIBUTE_NAME;
                                   yybegin(HTML_ATTRIBUTE_VALUE);
                                   return symbol(XML_EQ); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  .                              { yypushback(1); yybegin(HTML_ATTRIBUTE_NAME); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  {HtmlIdentifier}               { yybegin(nextStateAfterHtmlAttribute); yypushback(yylength()); }
  \'                             { yybegin(HTML_ATTRIBUTE_VALUE_1);
                                   return symbol(XML_ATTRIBUTE_VALUE_START_DELIMITER); }
  \"                             { yybegin(HTML_ATTRIBUTE_VALUE_2);
                                   return symbol(XML_ATTRIBUTE_VALUE_START_DELIMITER); }
  .                              { yypushback(1);
                                   yybegin(nextStateAfterHtmlAttribute); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE_1> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
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
  \'                             { yybegin(nextStateAfterHtmlAttribute);
                                   return symbol(XML_ATTRIBUTE_VALUE_END_DELIMITER); }

  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  [^{}<>&']+                     { return symbol(XML_ATTRIBUTE_VALUE_TOKEN, yytext()); }
  ">"                            { yybegin(nextStateAfterHtmlAttribute);
                                   return symbol(XML_BAD_CHARACTER); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_ATTRIBUTE_VALUE_2> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
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
  \"                             { yybegin(nextStateAfterHtmlAttribute);
                                   return symbol(XML_ATTRIBUTE_VALUE_END_DELIMITER); }

  {HtmlEntityRef}                { return symbol(XML_CHAR_ENTITY_REF, yytext()); }
  [^{}<>&\"]+                    { return symbol(XML_ATTRIBUTE_VALUE_TOKEN, yytext()); }
  ">"                            { yybegin(nextStateAfterHtmlAttribute);
                                   return symbol(XML_BAD_CHARACTER); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_DOCTYPE> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n} ]        { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_DOCTYPE;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_DOCTYPE;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  "SYSTEM"                       { return symbol(XML_DOCTYPE_SYSTEM); }
  "PUBLIC"                       { return symbol(XML_DOCTYPE_PUBLIC); }
  {HtmlIdentifier}               { return symbol(XML_NAME); }
  "="                            { nextStateAfterHtmlAttribute = HTML_DOCTYPE;
                                   yybegin(HTML_ATTRIBUTE_VALUE);
                                   return symbol(XML_EQ); }
  \" [^\"]* \"                   { return symbol(XML_ATTRIBUTE_VALUE_TOKEN); }
  \' [^\']* \'                   { return symbol(XML_ATTRIBUTE_VALUE_TOKEN); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_DOCTYPE_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_DIRECTIVE> {
  {DocComment} |
  {TraditionalComment}           { return symbol(COMMENT, yytext().toString()); }
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n} ]        { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_DIRECTIVE;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_DIRECTIVE;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  {HtmlIdentifier}               { return symbol(XML_NAME); }
  "="                            { nextStateAfterHtmlAttribute = HTML_DIRECTIVE;
                                   yybegin(HTML_ATTRIBUTE_VALUE);
                                   return symbol(XML_EQ); }
  \" | \'                        { yypushback(1);
                                   nextStateAfterCloseTag = HTML_DIRECTIVE;
                                   yybegin(HTML_ATTRIBUTE_VALUE); }
  {WhiteSpace}+                  { nextStateAfterWhitespace = yystate();
                                   yybegin(AFTER_WHITESPACE);
                                   return symbol(TAG_WHITE_SPACE); }
  "?>"                           { yybegin(HTML_INITIAL); return symbol(XML_DECL_END); }
  ">"                            { yybegin(HTML_INITIAL); return symbol(XML_DECL_END); }
  .                              { return symbol(XML_BAD_CHARACTER); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<HTML_CDATA> {
  "{" "{"?                       { return symbol(LBRACE_ERROR); }
  "}" "}"?                       { return symbol(RBRACE_ERROR); }
  "{" "{"? [^ \t\f\r\n} ]        { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = false;
                                   doubleBraceTag = yylength() == 2;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_CDATA;
                                   tagStartLine = yyline;
                                   return symbol(TAG_LBRACE, yytext().toString()); }
  "{" "{"? "/" [^ \t\f\r\n}]     { yybegin(OPEN_TAG);
                                   yypushback(1);
                                   closeTag = true;
                                   doubleBraceTag = yylength() == 3;
                                   currentCommand = null;
                                   nextStateAfterCloseTag = HTML_CDATA;
                                   tagStartLine = yyline;
                                   return symbol(TAG_END_LBRACE, yytext().toString()); }
  "]]>"                          { yybegin(HTML_INITIAL); return symbol(XML_CDATA_END); }
  {HtmlCDataText}+               { return symbol(XML_PCDATA); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}

<AFTER_WHITESPACE> {
  {EndOfLineComment}             { return symbol(LINE_COMMENT, yytext().toString()); }
  . | [\f\r\n]                   { yypushback(1);
                                   yybegin(nextStateAfterWhitespace); }
  <<EOF>>                        { yybegin(YYINITIAL); }
}
