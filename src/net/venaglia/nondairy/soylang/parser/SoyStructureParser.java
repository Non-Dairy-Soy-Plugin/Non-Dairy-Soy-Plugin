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

package net.venaglia.nondairy.soylang.parser;

import static net.venaglia.nondairy.i18n.MessageBuffer.msg;
import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.xml.IXmlElementType;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.i18n.MessageBuffer;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.util.Visitor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 2, 2010
 * Time: 8:18:40 PM
 */
public class SoyStructureParser {

    private TokenSource source;
    private Stack<TagParser> unclosedTagParsers = new Stack<TagParser>();
    private PsiBuilder.Marker docBeginMarker = null;

    private static final Visitor<TagParser> CAPTURE_TAG_PAIR_VISITOR = new Visitor<TagParser>() {
        @Override
        public void visit(TagParser within) {
            SoyElement tagElement = within.getElement();
            SoyElement tagPairElement = null;
            if (tagElement.name().endsWith("_tag")) { // NON-NLS
                tagPairElement = SoyElement.valueOf(tagElement.name() + "_pair"); //NON-NLS
            }
            if (tagPairElement == null) tagPairElement = SoyElement.tag_pair;
            within.getTagMarker().precede().done(tagPairElement);
        }
    };

    public SoyStructureParser(TokenSource source) {
        this.source = source;
    }

    /**
     * Parses the body of a soy file.
     * @see net.venaglia.nondairy.soylang.lexer.SoyScanner#YYINITIAL
     */
    public void parse() {
        PsiBuilder.Marker marker = source.mark("marker");
        PsiBuilder.Marker docBeginMarker = null;
        while (!source.eof()) {
            IElementType token = source.token();
            if (token == SoyToken.TAG_LBRACE || token == SoyToken.TAG_END_LBRACE) {
                TagParser tagParser = new TagParser(source);
                tagParser.parse();
                processParsedTag(tagParser);
                if (docBeginMarker != null &&
                    unclosedTagParsers.size() == 1 &&
                    unclosedTagParsers.peek() == tagParser &&
                    tagParser.getTagToken() == SoyToken.TEMPLATE) {
                    this.docBeginMarker = docBeginMarker;
                    docBeginMarker = null;
                }
            } else if (SoyToken.DOC_COMMENT == token || SoyToken.DOC_COMMENT_BEGIN == token) {
                if (docBeginMarker != null) {
                    docBeginMarker.drop();
                }
                docBeginMarker = source.mark("docBeginMarker");
                new DocParser(source).parse();
            } else if (token instanceof IXmlElementType) {
                new TemplateTextParser(source).parse();
            } else {
                if (docBeginMarker != null) {
                    docBeginMarker.drop();
                    docBeginMarker = null;
                }
                if (token == SoyToken.IGNORED_TEXT || token == SoyToken.TEMPLATE_TEXT || token == SoyToken.LITERAL_TEXT) {
                    source.advance();
                } else {
                    source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol", I18N.msg("lexer.error.unexpected.token", token));
                }
            }
        }
        if (docBeginMarker != null) {
            docBeginMarker.drop();
        }
        if (this.docBeginMarker != null) {
            this.docBeginMarker.drop();
        }
        marker.done(soy_file);
    }

    private void processParsedTag(TagParser tagParser) {
        IElementType type = tagParser.getTagToken();
        if (!(type instanceof SoyToken)) return;
        SoyToken tagToken = (SoyToken)type;
        String tagTokenName = tagToken.name().toLowerCase();
        if (tagParser.isCloseTag()) {
            findInStack(Collections.singleton(tagToken),
                        tagParser,
                        CAPTURE_TAG_PAIR_VISITOR,
                        msg("syntax.error.unexpected.close.tag",
                            tagTokenName),
                        msg("syntax.error.unclosed.open.tag"));
        } else if (SoyToken.TAG_SECTION_TOKENS.contains(tagToken)) {
            SectionTag section = SectionTag.getBySoyToken(tagToken);
            String sectionTokenName = section.getContainerTokens().iterator().next().name();
            TagParser within = findInStack(section.getContainerTokens(),
                                           tagParser,
                                           null,
                                           msg("syntax.error.orphaned.section.tag",
                                               tagTokenName,
                                               sectionTokenName),
                                           msg("syntax.error.unclosed.open.tag"));
            if (within != null) {
                within.updateContainedSection(tagParser,
                                              msg("syntax.error.out-of-order.section.tag",
                                                  tagTokenName),
                                              msg("syntax.error.duplicate.section.tag",
                                                  tagTokenName,
                                                  sectionTokenName));
                unclosedTagParsers.push(within); // put it back
            }
            if (tagParser.isRequiresCloseTag()) unclosedTagParsers.push(tagParser);
        } else if (tagToken == SoyToken.NAMESPACE || tagToken == SoyToken.TEMPLATE) {
            if (!unclosedTagParsers.isEmpty()) {
                TagParser within = unclosedTagParsers.get(0);
                processBadTag(within, I18N.msg("syntax.error.unexpected.close.tag",
                                               tagTokenName));
                unclosedTagParsers.clear();
            }
            if (tagParser.isRequiresCloseTag()) {
                unclosedTagParsers.push(tagParser);
            }
        } else if (tagParser.isRequiresCloseTag() && SoyToken.COMMAND_TOKENS.contains(tagToken)) {
            unclosedTagParsers.push(tagParser);
        }
    }

    private TagParser findInStack(Collection<? extends IElementType> types,
                                  TagParser offendingTag,
                                  @Nullable Visitor<TagParser> visitBeforeDone,
                                  MessageBuffer notInStack,
                                  MessageBuffer notAtTopOfStack) {
        if (unclosedTagParsers.isEmpty()) {
            processBadTag(offendingTag, notInStack.toString());
            return null;
        }
        if (types.contains(unclosedTagParsers.peek().getTagToken())) {
            TagParser top = unclosedTagParsers.pop();
            if (unclosedTagParsers.isEmpty() && docBeginMarker != null) {
                if (visitBeforeDone != null) {
                    visitBeforeDone.visit(top);
                }
                docBeginMarker.done(tag_and_doc_comment);
                docBeginMarker = null;
            }
            return top;
        }
        boolean found = false;
        for (TagParser within : unclosedTagParsers) {
            if (types.contains(within.getTagToken())) {
                found = true;
                break;
            }
        }
        if (!found) {
            processBadTag(offendingTag, notInStack.toString());
            return null;
        }
        TagParser last = null;
        while (!unclosedTagParsers.isEmpty()) {
            TagParser top = unclosedTagParsers.pop();
            if (types.contains(top.getTagToken())) {
                processBadTag(last, notAtTopOfStack.toString());
                if (unclosedTagParsers.isEmpty() && docBeginMarker != null) {
                    if (visitBeforeDone != null) {
                        visitBeforeDone.visit(top);
                    }
                    docBeginMarker.done(tag_and_doc_comment);
                    docBeginMarker = null;
                }
                return top;
            }
            last = top;
        }
        // should never get here
        processBadTag(offendingTag, notInStack.toString());
        return null;
    }

    private void processBadTag(TagParser tagParser, String message) {
        PsiBuilder.Marker badMarker = tagParser.getTagMarker().precede();
        badMarker.error(message);
    }
}
