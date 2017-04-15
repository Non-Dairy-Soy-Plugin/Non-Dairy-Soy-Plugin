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

package net.venaglia.nondairy.soylang.parser;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.i18n.MessageBuffer;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NonNls;

import java.util.Deque;
import java.util.LinkedList;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Aug 14, 2010
* Time: 5:01:07 PM
*/
class TagParser {

    private static final TokenSet END_OF_TAG_TOKENS = TokenSet.create(SoyToken.TAG_END_RBRACE, SoyToken.TAG_RBRACE, SoyToken.RBRACE_ERROR);

    private final TokenSource source;
    private final PsiBuilder.Marker tagMarker;
    private final boolean isCloseTag;

    private int expected = TagDataType.COMMAND.value();
    private SoyElement element = tag;
    private String command = null;
    private IElementType tagToken = null;
    private Deque<SectionTag> companions = new LinkedList<SectionTag>();
    private boolean requiresCloseTag = false;
    private boolean markerIsDone = false;
//    private PsiBuilder.Marker tagPairMarker;
    private PsiBuilder.Marker innerMarker;

    TagParser(TokenSource source) {
        this.source = source;
        if (source.eof()) {
            throw new AssertionError("Cannot begin parsing a tag unless the lexer is at a '{'");
        }
        IElementType token = source.token();
        if (token != SoyToken.TAG_LBRACE && token != SoyToken.TAG_END_LBRACE) {
            throw new AssertionError("Cannot begin parsing a tag unless the lexer is at a '{'");
        }
        this.isCloseTag = token == SoyToken.TAG_END_LBRACE;
//        this.tagPairMarker = source.mark();
        this.tagMarker = source.mark("tagMarker");
        source.advance();
        this.innerMarker = source.mark("innerMarker");
        if (source.eof()) done();
    }

    private void done() {
        if (!markerIsDone) {
            IElementType closingMarker = source.eof() ? null : source.token();
            if (closingMarker == SoyToken.TAG_END_RBRACE && requiresCloseTag) requiresCloseTag = false;
            String errorMessage = null;
            if (isExpecting(TagDataType.COMMAND)) {
                errorMessage = I18N.msg("syntax.error.expected.command");
            } else if (isExpecting(TagDataType.NAME)) {
                errorMessage = I18N.msg("syntax.error.expected.name");
            } else if (isExpecting(TagDataType.EXPRESSION) || isExpecting(TagDataType.EXPRESSION_LIST)) {
                errorMessage = I18N.msg("syntax.error.expected.expression");
            }
            innerMarker.done(tag_between_braces);
            if (source.eof()) {
                if (errorMessage == null) errorMessage = I18N.msg("syntax.error.unexpected.eof");
                source.error(errorMessage);
            } else if (END_OF_TAG_TOKENS.contains(source.token())) {
                if (errorMessage != null) source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol", errorMessage);
                else source.advance();
            } else {
                if (errorMessage == null) source.errorBadToken();
                else source.error(errorMessage);
            }
            tagMarker.done(element);
//            if (tagPairMarker != null) {
//                if (isCloseTag) tagPairMarker.done(tag_pair);
//                else tagPairMarker.drop();
//            }
            markerIsDone = true;
        }
    }

//    public TagParser setTagPairMarker(PsiBuilder.Marker tagPairMarker) {
//        if (this.tagPairMarker != tagPairMarker) {
//            if (this.tagPairMarker != null) this.tagPairMarker.drop();
//            this.tagPairMarker = tagPairMarker;
//        }
//        return this;
//    }

    private void mayExpect(TagDataType tagDataClass) {
        expected = tagDataClass.removeExclusive(expected);
    }

    private void notExpect(TagDataType tagDataClass) {
        expected = tagDataClass.removeInclusive(expected);
    }

    private void nowExpect(TagDataType... tagDataClasses) {
        for (TagDataType tagDataClass : tagDataClasses) {
            if (!isCloseTag || tagDataClass == TagDataType.COMMAND) {
                expected |= tagDataClass.value();
            }
        }
    }

    private boolean isExpecting(TagDataType tagDataClass) {
        return tagDataClass.containedIn(expected);
    }

    void parse() {
        TagParser parser = this;
        TagDataType next = TagDataType.nextIn(parser.expected);
        while (next != null && !source.eof()) {
            IElementType token = source.token();
            switch (next) {
                case COMMAND:
                    if (SoyToken.COMMAND_TOKENS.contains(token)) {
                        parser = parser.parseInitial();
                    } else {
                        source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.command"));
                    }
                    break;
                case NAME:
                    if (SoyToken.NAME_TOKENS.contains(token)) {
                        parser = parser.parseName();
                    } else if (tagToken == SoyToken.TEMPLATE || tagToken == SoyToken.DELTEMPLATE) {
                        source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.template.definition"));
                    } else {
                        source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.name"));
                    }
                    break;
                case EXPRESSION:
                    notExpect(TagDataType.EXPRESSION);
                    if (!SoyToken.EXPRESSION_TOKENS.contains(token)) {
                        if (tagToken == SoyToken.IF || tagToken == SoyToken.ELSE_IF) {
                            source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.boolean_expression"));
                        } else {
                            source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.expression"));
                        }
                        break;
                    }
                    new ExpressionParser(source).parse();
                    break;
                case EXPRESSION_LIST:
                    if (token == SoyToken.COMMA) {
                        source.advance();
                        nowExpect(TagDataType.EXPRESSION);
                    } else {
                        notExpect(TagDataType.EXPRESSION_LIST);
                    }
                    break;
                case ATTRIBUTES:
                    if (!SoyToken.ATTRIBUTE_TOKENS.contains(token)) {
                        notExpect(TagDataType.ATTRIBUTES);
                        break;
                    }
                    mayExpect(TagDataType.ATTRIBUTES);
                    parseAttribute();
                    break;
                case DIRECTIVES:
                    if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                        notExpect(TagDataType.DIRECTIVES);
                        break;
                    }
                    mayExpect(TagDataType.DIRECTIVES);
                    if (token == SoyToken.DIRECTIVE_COLON || token == SoyToken.DIRECTIVE_COMMA) {
                        nowExpect(TagDataType.EXPRESSION);
                    }
                    parseDirective();
                    break;
                case KEYWORD:
                    if (token == SoyToken.AS) {
                        source.advance();
                        token = source.token();
                        if (token == SoyToken.CAPTURED_IDENTIFIER) {
                            source.advance();
                        } else {
                            source.advanceAndMarkBad(token, "token", I18N.msg("syntax.error.expected.name"));
                        }
                    }
                    notExpect(TagDataType.KEYWORD);
                    break;
                case DIRECTIVE_ARG:

                    break;
            }
            if (parser.expected == 0) {
                parser.parseToTagRBrace(SoyToken.TAG_RBRACE, SoyToken.TAG_END_RBRACE);
                parser.done();
                parser = null;
            }
            next = parser == null ? null : TagDataType.nextIn(parser.expected);
        }
        if (parser != null) parser.done();
    }

    private void parseAttribute() {
        IElementType token = source.token();
        PsiBuilder.Marker beginAttribute;
        if (token == SoyToken.CAPTURED_IDENTIFIER) {
            beginAttribute = source.mark("beginAttribute");
        } else {
            source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
            return;
        }
        source.advanceAndMark(attribute_key, "attribute_key");
        if (!source.eof()) {
            token = source.token();
            if (token == SoyToken.CAPTURED_IDENTIFIER || !SoyToken.ATTRIBUTE_TOKENS.contains(token)) {
                beginAttribute.done(attribute);
                return;
            } else if (token != SoyToken.EQ) {
                beginAttribute.drop();
                source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
                return;
            } else {
                source.advance();
            }
        }

        PsiBuilder.Marker beginValue = null;
        PsiBuilder.Marker beginValueInner = null;
        if (!source.eof()) {
            token = source.token();
            if (token != SoyToken.STRING_LITERAL_BEGIN) {
                beginAttribute.drop();
                source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
                return;
            } else {
                beginValue = source.mark("beginValue");
                source.advance();
            }
        }
        while (!source.eof()) {
            token = source.token();
            if (token == SoyToken.STRING_LITERAL_END) {
                if (beginValueInner != null) {
                    beginValueInner.done(attribute_value);
                }
                source.advance();
                if (beginValue != null) {
                    beginValue.done(expression);
                }
                beginAttribute.done(attribute);
                return;
            } else if (!SoyToken.STRING_LITERAL_TOKENS.contains(token)) {
                if (beginValueInner != null) {
                    beginValueInner.done(attribute_value);
                }
                if (beginValue != null) {
                    beginValue.done(expression);
                }
                beginAttribute.done(attribute);
                if (token == SoyToken.UNTERMINATED_STRING_LITERAL) {
                    source.advanceAndMarkBad(expression_error, "unterminated_string_literal", I18N.msg("syntax.error.unterminated.string.literal"));
                } else {
                    source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
                }
                return;
            } else {
                if (beginValueInner == null) {
                    beginValueInner = source.mark("beginValueInner");
                }
                if (token == SoyToken.STRING_PARAMETER_REF) {
                    source.advanceAndMark(parameter_ref, "parameter_ref");
                } else {
                    source.advance();
                }
            }
        }
        if (beginValueInner != null) {
            beginValueInner.done(attribute_value);
        }
        if (beginValue != null) {
            beginValue.done(expression);
        }
        beginAttribute.done(attribute);
    }

    private void parseDirective() {
        IElementType token = source.token();
        PsiBuilder.Marker beginPipe = source.mark("beginPipe");
        if (token != SoyToken.DIRECTIVE_PIPE) {
            source.advanceAndMark(unexpected_symbol, "unexpected_symbol");
            beginPipe.drop();
            return;
        } else {
            source.advance();
        }
        PsiBuilder.Marker beginDirective;
        if (!source.eof()) {
            token = source.token();
            if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                source.error(I18N.msg("syntax.error.expected.directive"));
                beginPipe.done(unexpected_symbol);
                return;
            } else if (token == SoyToken.DIRECTIVE_IDENTIFIER) {
                beginDirective = source.mark("beginDirective");
                source.advanceAndMark(directive_key, "directive_key");
            } else {
                source.advanceAndMarkBad(directive_key, "directive_key", I18N.msg("syntax.error.expected.directive"));
                beginPipe.drop();
                return;
            }
            beginPipe.drop();
        } else {
            beginPipe.drop();
            return;
        }

        PsiBuilder.Marker beginValueList = null;
        PsiBuilder.Marker beginValue;
        if (!source.eof()) {
            token = source.token();
            if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                beginDirective.drop();
                return;
            } else if (token == SoyToken.DIRECTIVE_COLON) {
                source.advance();
                beginValue = source.mark("beginValue");
            } else {
                beginDirective.done(directive);
                source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
                return;
            }
        } else {
            beginDirective.drop();
            return;
        }

        while (!source.eof()) {
            token = source.token();
            if (SoyToken.EXPRESSION_TOKENS.contains(token)) {
                new ExpressionParser(source).parse();
                if (!source.eof() && source.token() == SoyToken.COMMA) {
                    if (beginValueList == null) {
                        beginValueList = beginValue.precede();
                    }
                    beginValue.done(directive_value);
                    source.advance();
                    beginValue = source.mark("beginValue");
                } else {
                    beginValue.done(directive_value);
                    if (beginValueList != null) {
                        beginValueList.done(directive_value_list);
                    }
                    beginDirective.done(directive);
                    return;
                }
            } else if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                beginValue.drop();
                if (beginValueList != null) {
                    beginValueList.done(directive_value_list);
                }
                beginDirective.done(directive);
                return;
            } else {
                source.advance();
            }
        }
        beginValue.done(directive_value);
        if (beginValueList != null) {
            beginValueList.done(directive_value_list);
        }
        beginDirective.done(directive);
    }

    private TagParser parseInitial() {
        if (isExpecting(TagDataType.COMMAND)) {
            notExpect(TagDataType.COMMAND);
            IElementType token = source.token();
            @NonNls String command = source.text();
            if (token == SoyToken.DELPACKAGE) {
                nowExpect(TagDataType.NAME);
                source.advanceAndMark(command_keyword, "command_keyword");
                element = package_def;
            } else if (token == SoyToken.NAMESPACE) {
                nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword, "command_keyword");
                element = namespace_def;
            } else if (token == SoyToken.ALIAS) {
                nowExpect(TagDataType.NAME, TagDataType.KEYWORD);
                source.advanceAndMark(command_keyword, "command_keyword");
                element = alias_def;
            } else if (token == SoyToken.TEMPLATE) {
                nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword, "command_keyword");
                element = template_tag;
                requiresCloseTag = true;
            } else if (token == SoyToken.DELTEMPLATE) {
                nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword, "command_keyword");
                element = deltemplate_tag;
                requiresCloseTag = true;
            } else if (token == SoyToken.LET) {
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag && !source.eof()) {
                    element = let_tag;
                    if (source.token() == SoyToken.LET_IDENTIFIER) {
                        String name = source.text();
                        if (name.equals("$") || name.charAt(0) != '$' || name.contains(".")) {
                            source.advanceAndMarkBad(let_parameter_def, "let_parameter_def", I18N.msg("syntax.error.invalid.let.variable.name", name));
                        } else {
                            name = name.substring(1); // trim the leading $
                            source.advanceAndMark(let_parameter_def, "let_parameter_def");
                        }
                        if (!source.eof()) {
                            if (source.token() == SoyToken.COLON) {
                                source.advance();
                                nowExpect(TagDataType.EXPRESSION);
                            } else if (source.token() == SoyToken.TAG_RBRACE) {
                                // this is OK, template content is an acceptable value
                                requiresCloseTag = true;
                            } else if (source.token() == SoyToken.TAG_END_RBRACE) {
                                source.error(I18N.msg("syntax.error.expected.let.value", name));
                            } else {
                                source.error(I18N.msg("syntax.error.expected.colon.after.let.name"));
                            }
                        }
                    } else if (source.token() == SoyToken.COLON || SoyToken.TAG_BRACES.contains(source.token())) {
                        source.error(I18N.msg("syntax.error.expected.let.name"));
                    }
                }
            } else if (token == SoyToken.IF) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword, "command_keyword");
                setCompanions(SoyToken.ELSE_IF, SoyToken.ELSE);
                requiresCloseTag = true;
            } else if (token == SoyToken.ELSE_IF) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.ELSE) {
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.SWITCH) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword, "command_keyword");
                setCompanions(SoyToken.CASE, SoyToken.DEFAULT);
                requiresCloseTag = true;
            } else if (token == SoyToken.CASE) {
                nowExpect(TagDataType.EXPRESSION, TagDataType.EXPRESSION_LIST);
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.DEFAULT) {
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.FOREACH) {
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag) {
                    element = iterator_tag;
                    parseIteratorTag(false);
                    setCompanions(SoyToken.IF_EMPTY);
                    requiresCloseTag = true;
                }
            } else if (token == SoyToken.IF_EMPTY) {
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.LITERAL) {
                source.advanceAndMark(command_keyword, "command_keyword");
                requiresCloseTag = true;
            } else if (token == SoyToken.MSG) {
                nowExpect(TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag) {
                    element = msg_tag;
                    requiresCloseTag = true;
                }
            } else if (token == SoyToken.FOR) {
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag) {
                    element = iterator_tag;
                    parseIteratorTag(true);
                    requiresCloseTag = true;
                }
            } else if (token == SoyToken.CALL || token == SoyToken.DELCALL) {
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag) {
                    element = call_tag;
                    nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                    setCompanions(SoyToken.PARAM);
                    requiresCloseTag = true;
                }
            } else if (token == SoyToken.PARAM) {
                source.advanceAndMark(command_keyword, "command_keyword");
                if (!isCloseTag && !source.eof()) {
                    element = param_tag;
                    if (source.token() == SoyToken.PARAMETER_REF) {
                        source.advanceAndMark(invocation_parameter_ref, "invocation_parameter_ref");
                        if (!source.eof()) {
                            if (source.token() == SoyToken.COLON) {
                                source.advance();
                                nowExpect(TagDataType.EXPRESSION);
                            } else if (source.token() == SoyToken.TAG_RBRACE) {
                                // this is OK, template content is an acceptable value
                            } else if (source.token() == SoyToken.TAG_END_RBRACE) {
                                source.error(I18N.msg("syntax.error.expected.parameter.value"));
                            } else {
                                source.error(I18N.msg("syntax.error.expected.colon.after.parameter.name"));
                            }
                        }
                    } else if (source.token() == SoyToken.COLON) {
                        source.error(I18N.msg("syntax.error.expected.parameter.name"));
                    }
                    requiresCloseTag = true;
                }
            } else if (token == SoyToken.PRINT) {
                nowExpect(TagDataType.EXPRESSION, TagDataType.DIRECTIVES);
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (token == SoyToken.PRINT_IMPLICIT) {
                command = "print";
                nowExpect(TagDataType.EXPRESSION, TagDataType.DIRECTIVES);
                source.advance();
            } else if (token == SoyToken.CSS) {
                nowExpect(TagDataType.EXPRESSION, TagDataType.DIRECTIVES);
                source.advanceAndMark(command_keyword, "command_keyword");
            } else if (SoyToken.NON_TAG_TOKENS.contains(token)) {
                source.advanceAndMarkBad(invalid_text, "invalid_text");
                done();
                return null;
            } else if (token == SoyToken.INNER_PARAM) {
                nowExpect(TagDataType.NAME);
                source.advance();
            }
            tagToken = token;
            this.command = command;
            return this;
        } else {
            source.advanceAndMarkBad(command_keyword, "command_keyword");
            return this;
        }
    }

    private void parseIteratorTag(boolean expectingRangeExpression) {
        IElementType token;
        if (!source.eof()) {
            token = source.token();
            if (token == SoyToken.PARAMETER_REF) {
                source.advanceAndMark(parameter_def, "parameter_def");
            } else {
                source.error(I18N.msg("syntax.error.expected.parameter.declaration"));
            }
        }
        if (!source.eof()) {
            token = source.token();
            if (token == SoyToken.IN) {
                source.advance();
            } else {
                source.error(I18N.msg("syntax.error.expected.keyword.in"));
            }
        }
        if (!source.eof() && expectingRangeExpression) {
            if (source.token() == SoyToken.RANGE) {
                PsiBuilder.Marker startRangeExprMarker = source.mark("startRangeExprMarker");
                source.advance();
                startRangeExprMarker.done(function_call_name);
                startRangeExprMarker = startRangeExprMarker.precede();
                if (!source.eof()) {
                    int argCount = new ExpressionParser(source).parseFunctionArgs(function_call_args);
                    if (argCount == 0) {
                        startRangeExprMarker.error(I18N.msg("syntax.error.missing.range.parameter"));
                    } else if (argCount > 3) {
                        startRangeExprMarker.error(I18N.msg("syntax.error.unexpected.range.parameter"));
                    } else {
                        startRangeExprMarker.done(function_call);
                    }
                    // todo: add support for RANGE keyword
                } else {
                    startRangeExprMarker.drop();
                }
            } else {
                source.error(I18N.msg("syntax.error.expected.keyword.in"));
                expectingRangeExpression = false;
            }
        }
        if (!source.eof() && !expectingRangeExpression) {
            if (!source.eof() && SoyToken.EXPRESSION_TOKENS.contains(source.token())) {
                new ExpressionParser(source).parse();
            }
        }
    }

    private void parseToTagRBrace(SoyToken rBraceToken1, SoyToken rBraceToken2) {
        if (source.eof()) {
            done();
            return;
        }
        IElementType token = source.token();
        if (token == tagToken) {
            source.advance();
            token = source.token();
        }
        if (token == rBraceToken1 || token == rBraceToken2) {
            done();
            return;
        }
        PsiBuilder.Marker errorMarker = source.mark("errorMarker");
        source.advance();
        while (!source.eof()) {
            if (END_OF_TAG_TOKENS.contains(source.token())) break;
            source.advance();
        }
        if (element == package_def || element == namespace_def || element == alias_def || element == template_tag) {
            errorMarker.error(I18N.msg("syntax.error.unexpected.tokens.in.declaration.tag", command));
        } else if (command == null) {
            errorMarker.error(I18N.msg("syntax.error.unexpected.tokens.in.unknown.tag"));
        } else {
            errorMarker.error(I18N.msg("syntax.error.unexpected.tokens.in.tag", command));
        }
        requiresCloseTag = false;
        done();
    }

    private TagParser parseName() {
        if (isExpecting(TagDataType.NAME)) {
            notExpect(TagDataType.NAME);
            IElementType token = source.token();
            if (token == SoyToken.PACKAGE_IDENTIFIER) {
                source.advanceAndMark(package_name, "package_name");
            } else if (token == SoyToken.NAMESPACE_IDENTIFIER) {
                if (element == alias_def) {
                    source.advanceAndMark(alias_name, "alias_name");
                } else {
                    source.advanceAndMark(namespace_name, "namespace_name");
                }
            } else if (token == SoyToken.TEMPLATE_IDENTIFIER) {
                SoyElement type = element == template_tag
                                  ? template_name
                                  : source.text().startsWith(".")
                                    ? template_name_ref
                                    : template_name_ref_absolute;
                source.advanceAndMark(type, "type");
            } else if (token == SoyToken.DELTEMPLATE_IDENTIFIER) {
                SoyElement type = element == deltemplate_tag
                                  ? deltemplate_name
                                  : deltemplate_name_ref;
                source.advanceAndMark(type, "type");
            } else if (token == SoyToken.PARAMETER_REF) {
                source.advanceAndMark(parameter_ref, "parameter_ref");
            } else if (token == SoyToken.CAPTURED_IDENTIFIER) {
                if (element == template_tag) {
                    source.advanceAndMarkBad(member_property_ref, "member_property_ref", I18N.msg("syntax.error.expected.template.definition"));
                } else {
                    source.advanceAndMarkBad(member_property_ref, "member_property_ref");
                }
            } else if (token == SoyToken.INNER_PARAMETER_DEF) {
                source.advanceAndMark(parameter_def, "parameter_def");
                if(source.token() == SoyToken.COLON) {
                    source.advance();
                }
                // skip type for now
                source.advance();
            } else {
                return this;
            }
        } else {
            source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
        }
        return this;
    }

    private void setCompanions(SoyToken... companions) {
        this.companions.clear();
        for (SoyToken companion : companions) {
            this.companions.add(SectionTag.getBySoyToken(companion));
        }
    }

    public boolean isCloseTag() {
        return isCloseTag;
    }

    public IElementType getTagToken() {
        return tagToken;
    }

    public SoyElement getElement() {
        return element;
    }

    public void updateContainedSection(TagParser parser, MessageBuffer outOfOrder, MessageBuffer duplicate) {
        SectionTag section = SectionTag.getBySoyToken((SoyToken)parser.getTagToken());
        if (section != null) {
            if (!companions.contains(section)) {
                PsiBuilder.Marker badTagMarker = parser.tagMarker.precede();
                if (section.isOrderImportant()) badTagMarker.error(outOfOrder.toString());
                else if (!section.isRepeatable()) badTagMarker.error(duplicate.toString());
                else badTagMarker.error(I18N.msg("syntax.error.unspecified"));
            } else if (section.isOrderImportant()) {
                while (!section.equals(companions.peekFirst())) companions.removeFirst();
                if (!section.isRepeatable()) companions.removeFirst();
            } else {
                if (!section.isRepeatable()) companions.remove(section);
            }
        }
    }

    public boolean isRequiresCloseTag() {
        return requiresCloseTag;
    }

    public PsiBuilder.Marker getTagMarker() {
        return tagMarker;
    }
}
