package net.venaglia.nondairy.soylang.parser;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Aug 14, 2010
* Time: 5:01:07 PM
*/
class TagParser {

    private static final TokenSet END_OF_TAG_TOKENS = TokenSet.create(SoyToken.TAG_END_RBRACE, SoyToken.TAG_RBRACE, SoyToken.RBRACE_ERROR);

    private final TokenSource source;
    private final boolean isCloseTag;

    private int expected = TagDataType.COMMAND.value();
    private SoyElement element = tag;
    private IElementType tagToken = null;
    private Set<SoyToken> companions = Collections.emptySet();
    private boolean requiresCloseTag = false;
    private boolean markerIsDone = false;
    private PsiBuilder.Marker tagPairMarker;
    private PsiBuilder.Marker tagMarker;
    private PsiBuilder.Marker innerMarker;

    TagParser(TokenSource source) {
        this.source = source;
        if (source.eof()) {
            throw new AssertionError("Cannot being parsing a tag unless the lexer is at a '{'");
        }
        IElementType token = source.token();
        if (token != SoyToken.TAG_LBRACE && token != SoyToken.TAG_END_LBRACE) {
            throw new AssertionError("Cannot being parsing a tag unless the lexer is at a '{'");
        }
        this.isCloseTag = token == SoyToken.TAG_END_LBRACE;
        this.tagPairMarker = source.mark();
        this.tagMarker = source.mark();
        source.advance();
        this.innerMarker = source.mark();
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
            } else if (isExpecting(TagDataType.EXPRESSION)) {
                errorMessage = I18N.msg("syntax.error.expected.expression");
            }
            innerMarker.done(tag_between_braces);
            if (source.eof()) {
                if (errorMessage == null) errorMessage = I18N.msg("syntax.error.unexpected.eof");
                source.error(errorMessage);
            } else if (END_OF_TAG_TOKENS.contains(source.token())) {
                if (errorMessage != null) source.advanceAndMarkBad(unexpected_symbol, errorMessage);
                else source.advance();
            } else {
                if (errorMessage == null) source.errorBadToken();
                else source.error(errorMessage);
            }
            tagMarker.done(element);
            if (tagPairMarker != null) {
                if (isCloseTag) tagPairMarker.done(tag_pair);
                else tagPairMarker.drop();
            }
            markerIsDone = true;
        }
    }

    public TagParser setTagPairMarker(PsiBuilder.Marker tagPairMarker) {
        if (this.tagPairMarker != tagPairMarker) {
            if (this.tagPairMarker != null) this.tagPairMarker.drop();
            this.tagPairMarker = tagPairMarker;
        }
        return this;
    }

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
                        source.advanceAndMarkBad(token);
                    }
                    break;
                case NAME:
                    if (SoyToken.NAME_TOKENS.contains(token)) {
                        parser = parser.parseName();
                    } else {
                        source.advanceAndMarkBad(token);
                    }
                    break;
                case EXPRESSION:
                    notExpect(TagDataType.EXPRESSION);
                    if (!SoyToken.EXPRESSION_TOKENS.contains(token)) {
                        source.advanceAndMarkBad(token);
                        break;
                    }
                    new ExpressionParser(source).parse();
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
                    parseDirective();
                    break;
            }
            if (parser.expected == 0) {
//                tagPairMarker = parser.requiresCloseTag ? tagMarker.precede() : null;
                parser.parseToTagRBrace(SoyToken.TAG_RBRACE, SoyToken.TAG_END_RBRACE);
//                if (parser.requiresCloseTag) {
//                    parseContent();
//                    if (!source.eof() && source.token() == TAG_END_LBRACE) {
//                        TagParser closeTagParser = new TagParser(parser);
//                        closeTagParser.tagToken = parser.tagToken;
//                        closeTagParser.parseCloseTag();
//                    }
//                } else if (tagPairMarker != null) {
//                    tagPairMarker.done(tag_pair);
//                }
                parser.done();
                parser = null;
            }
            next = parser == null ? null : TagDataType.nextIn(parser.expected);
        }
        if (parser != null) parser.done();
    }

    private void parseAttribute() {
        IElementType token = source.token();
        PsiBuilder.Marker beginAttribute = null;
        if (token == SoyToken.CAPTURED_IDENTIFIER) {
            beginAttribute = source.mark();
        } else {
            source.advanceAndMarkBad(unexpected_symbol);
            return;
        }
        source.advanceAndMark(attribute_key);
        if (!source.eof()) {
            token = source.token();
            if (token == SoyToken.CAPTURED_IDENTIFIER || !SoyToken.ATTRIBUTE_TOKENS.contains(token)) {
                beginAttribute.done(attribute);
                return;
            } else if (token != SoyToken.EQ) {
                beginAttribute.drop();
                source.advanceAndMarkBad(unexpected_symbol);
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
                source.advanceAndMarkBad(unexpected_symbol);
                return;
            } else {
                beginValue = source.mark();
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
                beginAttribute.done(expression);
                return;
            } else if (!SoyToken.STRING_LITERAL_TOKENS.contains(token)) {
                if (beginValueInner != null) {
                    beginValueInner.done(attribute_value);
                }
                if (beginValue != null) {
                    beginValue.done(expression);
                }
                source.advanceAndMarkBad(unexpected_symbol);
                return;
            } else {
                if (beginValueInner == null) {
                    beginValueInner = source.mark();
                }
                if (token == SoyToken.STRING_PARAMETER_REF) {
                    source.advanceAndMark(parameter_ref);
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
    }

    private void parseDirective() {
        IElementType token = source.token();
        PsiBuilder.Marker beginPipe = source.mark();
        if (token != SoyToken.DIRECTIVE_PIPE) {
            source.advanceAndMark(unexpected_symbol);
            beginPipe.drop();
            return;
        } else {
            source.advance();
        }
        PsiBuilder.Marker beginDirective = null;
        if (!source.eof()) {
            token = source.token();
            if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                source.error(I18N.msg("syntax.error.expected.directive"));
                beginPipe.done(unexpected_symbol);
                return;
            } else if (token == SoyToken.DIRECTIVE_IDENTIFIER) {
                beginDirective = source.mark();
                source.advanceAndMark(directive_key);
            } else {
                source.advanceAndMarkBad(directive_key);
                beginPipe.drop();
                return;
            }
            beginPipe.drop();
        } else {
            beginPipe.drop();
            return;
        }

        PsiBuilder.Marker beginValueList = null;
        PsiBuilder.Marker beginValue = null;
        if (!source.eof()) {
            token = source.token();
            if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                beginDirective.drop();
                return;
            } else if (token == SoyToken.DIRECTIVE_COLON) {
                source.advance();
                beginValueList = source.mark();
                beginValue = source.mark();
            } else {
                beginDirective.done(directive);
                source.advanceAndMarkBad(unexpected_symbol);
                return;
            }
        } else {
            return;
        }

        while (!source.eof()) {
            token = source.token();
            if (SoyToken.EXPRESSION_TOKENS.contains(token)) {
                new ExpressionParser(source).parse();
                if (!source.eof() && source.token() == SoyToken.COMMA) {
                    beginValue.done(directive_value);
                    source.advance();
                    beginValue = source.mark();
                } else {
                    beginValue.done(directive_value);
                    beginValueList.done(directive_value_list);
                    beginDirective.done(directive);
                    return;
                }
            } else if (!SoyToken.DIRECTIVE_TOKENS.contains(token)) {
                beginValue.drop();
                beginDirective.done(directive);
                return;
            }
        }
        beginDirective.done(directive);
        beginValue.done(directive_value);
        beginValueList.done(directive_value_list);
    }

//    private void parseCloseTag() {
//        PsiBuilder.Marker closeTagMarker = source.mark();
//        if (!source.eof()) {
//            IElementType token = source.token();
//            if (token == tagToken) {
//                notExpect(TagDataType.COMMAND);
//                source.advanceAndMark(command_keyword);
//            } else if (COMMANDS.contains(token)) {
//                notExpect(TagDataType.COMMAND);
//                source.advanceAndMarkBad(command_keyword, I18N.msg("syntax.error.invalid.close.tag"));
//            } else if (END_OF_TAG_TOKENS.contains(token)) {
//                source.error(I18N.msg("syntax.error.invalid.close.tag"));
//            } else {
//                source.advanceAndMarkBad(invalid_text, I18N.msg("syntax.error.invalid.close.tag"));
//            }
//            parseToTagRBrace(TAG_RBRACE, null);
//        }
//        done();
//    }

//    private void parseContent() {
//        PsiBuilder.Marker contentMarker = null;
//        while (!source.eof()) {
//            IElementType token = source.token();
//            if (token instanceof SoyToken && !NON_TAG_TOKENS.contains(token)) {
//                break;
//            }
//            if (contentMarker == null) contentMarker = source.mark();
//            source.advance();
//        }
//        if (contentMarker != null) contentMarker.done(template_content);
//    }

    private TagParser parseInitial() {
        if (isExpecting(TagDataType.COMMAND)) {
            notExpect(TagDataType.COMMAND);
            IElementType token = source.token();
            if (token == SoyToken.NAMESPACE) {
                nowExpect(TagDataType.NAME);
                source.advanceAndMark(command_keyword);
                element = namespace_def;
            } else if (token == SoyToken.TEMPLATE) {
                nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword);
                element = SoyElement.template_def;
                requiresCloseTag = true;
            } else if (token == SoyToken.IF) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword);
                setCompanions(SoyToken.ELSE_IF, SoyToken.ELSE);
                requiresCloseTag = true;
            } else if (token == SoyToken.SWITCH) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword);
                setCompanions(SoyToken.CASE, SoyToken.DEFAULT);
                requiresCloseTag = true;
            } else if (token == SoyToken.FOREACH) {
                source.advanceAndMark(command_keyword);
                // todo: parse "foreach" in its entirety
                setCompanions(SoyToken.IF_EMPTY);
                requiresCloseTag = true;
            } else if (token == SoyToken.LITERAL) {
                source.advanceAndMark(command_keyword);
                requiresCloseTag = true;
            } else if (token == SoyToken.MSG) {
                nowExpect(TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword);
                requiresCloseTag = true;
            } else if (token == SoyToken.FOR) {
                source.advanceAndMark(command_keyword);
                // todo: parse "for" in its entirety
                requiresCloseTag = true;
            } else if (token == SoyToken.CALL) {
                nowExpect(TagDataType.NAME, TagDataType.ATTRIBUTES);
                source.advanceAndMark(command_keyword);
                requiresCloseTag = true;
            } else if (token == SoyToken.PARAM) {
                nowExpect(TagDataType.EXPRESSION);
                source.advanceAndMark(command_keyword);
                // todo: parse the key name
                requiresCloseTag = true;
            } else if (token == SoyToken.PRINT) {
                nowExpect(TagDataType.EXPRESSION, TagDataType.DIRECTIVES);
                source.advanceAndMark(command_keyword);
            } else if (token == SoyToken.PRINT_IMPLICIT) {
                nowExpect(TagDataType.EXPRESSION, TagDataType.DIRECTIVES);
                source.advance();
            } else if (token == SoyToken.CSS) {
                source.advanceAndMark(command_keyword);
            } else if (SoyToken.NON_TAG_TOKENS.contains(token)) {
                source.advanceAndMarkBad(invalid_text);
                done();
                return null;
            }
            tagToken = token;
            return this;
        } else {
            source.advanceAndMarkBad(command_keyword);
            return this;
        }
    }

    private void parseToTagRBrace(SoyToken rBraceToken1, SoyToken rBraceToken2) {
        IElementType token = source.token();
        if (token == rBraceToken1 || token == rBraceToken2) {
            done();
            return;
        }
        PsiBuilder.Marker errorMarker = source.mark();
        source.advance();
        while (!source.eof()) {
            if (END_OF_TAG_TOKENS.contains(source.token())) break;
            source.advance();
        }
        errorMarker.done(SoyToken.ILLEGAL_CLOSE_TAG);
        done();
    }

    private TagParser parseName() {
        if (isExpecting(TagDataType.NAME)) {
            notExpect(TagDataType.NAME);
            IElementType token = source.token();
            if (token == SoyToken.NAMESPACE_IDENTIFIER) {
                source.advanceAndMark(namespace_name);
            } else if (token == SoyToken.TEMPLATE_IDENTIFIER) {
                source.advanceAndMark(element == template_def ? template_name : template_name_ref);
            } else if (token == SoyToken.PARAMETER_REF) {
                source.advanceAndMark(parameter_ref);
            } else if (token == SoyToken.CAPTURED_IDENTIFIER) {
                source.advanceAndMarkBad(member_property_ref);
            } else {
                return this;
            }
        } else {
            source.advanceAndMarkBad(unexpected_symbol);
        }
        return this;
    }

    private void setCompanions(SoyToken... companions) {
        this.companions = new HashSet<SoyToken>(Arrays.asList(companions));
    }

    public boolean isCloseTag() {
        return isCloseTag;
    }

    public IElementType getTagToken() {
        return tagToken;
    }

    public Set<SoyToken> getCompanions() {
        return companions;
    }

    public boolean isRequiresCloseTag() {
        return requiresCloseTag;
    }

    public PsiBuilder.Marker getTagPairMarker() {
        return tagPairMarker;
    }
}
