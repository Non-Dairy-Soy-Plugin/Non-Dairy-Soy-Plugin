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

package net.venaglia.nondairy.soylang.parser;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NonNls;

import java.util.Map;
import java.util.HashMap;

/**
 * User: ed
 * Date: Aug 18, 2010
 * Time: 10:17:54 PM
 *
 * Parser component that specializes in parsing soy doc comments.
 */
public class DocParser {

    private final TokenSource source;

    private PsiBuilder.Marker docMarker;
    private boolean markerIsDone = false;

    public DocParser(TokenSource source) {
        this.source = source;
        if (source.eof()) {
            throw new AssertionError("Cannot begin parsing a doc comment unless the lexer is at a '/**'");
        }
        docMarker = source.mark("docMarker");
        source.advance();
    }

    public void parse() {
        Map<PsiBuilder.Marker,Integer> indexByMarker = new HashMap<PsiBuilder.Marker,Integer>();
        PsiBuilder.Marker textMarker = markWithIndex(indexByMarker, source, "textMarker");
        PsiBuilder.Marker tagAndText = null;
        while (!source.eof()) {
            IElementType token = source.token();
            if (token == SoyToken.DOC_COMMENT_END) {
                if (textMarker != null) {
                    dropOrDone(indexByMarker, textMarker, doc_comment_text);
                    textMarker = null;
                }
                if (tagAndText != null) {
                    dropOrDone(indexByMarker, tagAndText, doc_comment_tag_with_description);
                    tagAndText = null;
                }
                source.advance();
                break;
            } else if (!SoyToken.DOC_COMMENT_TOKENS.contains(token)) {
                if (textMarker != null) {
                    dropOrDone(indexByMarker, textMarker, doc_comment_text);
                    textMarker = null;
                }
                if (tagAndText != null) {
                    dropOrDone(indexByMarker, tagAndText, doc_comment_tag_with_description);
                    tagAndText = null;
                }
                source.error(I18N.msg("lexer.error.unexpected.token", token));
                break;
            } else if (token == SoyToken.DOC_COMMENT_PARAM_TAG) {
                if (textMarker != null) {
                    dropOrDone(indexByMarker, textMarker, doc_comment_text);
                    textMarker = null;
                }
                if (tagAndText != null) {
                    dropOrDone(indexByMarker, tagAndText, doc_comment_tag_with_description);
                }
                tagAndText = markWithIndex(indexByMarker, source, "tagAndText");
                source.advanceAndMark(doc_comment_tag, "docTag");
                if (!source.eof() && source.token() == SoyToken.DOC_COMMENT_IDENTIFIER) {
                    source.advanceAndMark(doc_comment_param_def, "doc_comment_param");
                }
                if (!source.eof() && source.token() != SoyToken.DOC_COMMENT_END) {
                    textMarker = markWithIndex(indexByMarker, source, "textMarker");
                }
            } else if (token == SoyToken.DOC_COMMENT_TAG) {
                if (textMarker != null) {
                    dropOrDone(indexByMarker, textMarker, doc_comment_text);
                    textMarker = null;
                }
                if (tagAndText != null) {
                    dropOrDone(indexByMarker, tagAndText, doc_comment_tag_with_description);
                }
                tagAndText = markWithIndex(indexByMarker, source, "tagAndText");
                source.advanceAndMark(doc_comment_tag, "docTag");
                if (!source.eof() && source.token() != SoyToken.DOC_COMMENT_END) {
                    textMarker = markWithIndex(indexByMarker, source, "textMarker");
                }
            } else {
                source.advance();
            }
        }
        if (textMarker != null) {
            dropOrDone(indexByMarker, textMarker, doc_comment_text);
        }
        if (tagAndText != null) {
            dropOrDone(indexByMarker, tagAndText, doc_comment_tag_with_description);
        }
        done();
    }

    private PsiBuilder.Marker markWithIndex(Map<PsiBuilder.Marker,Integer> indexByMarker,
                                            TokenSource tokenSource,
                                            @NonNls String name) {
        PsiBuilder.Marker marker = tokenSource.mark(name);
        indexByMarker.put(marker, tokenSource.index());
        return marker;
    }

    private void dropOrDone(Map<PsiBuilder.Marker,Integer> indexByMarker,
                            PsiBuilder.Marker textMarker,
                            IElementType element) {
        if (indexByMarker.get(textMarker) == source.index()) {
            textMarker.drop();
        } else {
            textMarker.done(element);
        }
    }

    private void done() {
        if (!markerIsDone) {
            docMarker.done(doc_comment);
            markerIsDone = true;
        }
    }
}
