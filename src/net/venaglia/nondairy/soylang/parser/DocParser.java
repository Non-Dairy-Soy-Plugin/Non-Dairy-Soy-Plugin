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
            throw new AssertionError("Cannot begin parsing a tag unless the lexer is at a '/**'");
        }
        source.advance();
        docMarker = source.mark("docMarker");
    }

    public void parse() {
        PsiBuilder.Marker textMarker = source.mark("textMarker");
        while (!source.eof()) {
            IElementType token = source.token();
            if (token == SoyToken.DOC_COMMENT_END) {
                if (textMarker != null) {
                    textMarker.done(doc_comment_text);
                    textMarker = null;
                }
                source.advance();
                break;
            } else if (!SoyToken.DOC_COMMENT_TOKENS.contains(token)) {
                if (textMarker != null) {
                    textMarker.done(doc_comment_text);
                    textMarker = null;
                }
                source.error(I18N.msg("lexer.error.unexpected.token", token));
                break;
            } else if (token == SoyToken.DOC_COMMENT_TAG) {
                if (textMarker != null) {
                    textMarker.done(doc_comment_text);
                    textMarker = null;
                }
                source.advance();
                if (!source.eof() && source.token() == SoyToken.DOC_COMMENT_IDENTIFIER) {
                    source.advanceAndMark(doc_comment_param, "doc_comment_param");
                }
                if (!source.eof()) {
                    textMarker = source.mark("textMarker");
                }
            } else {
                source.advance();
            }
        }
        if (textMarker != null) {
            textMarker.done(doc_comment_text);
        }
        done();
    }

    private void done() {
        if (!markerIsDone) {
            docMarker.done(doc_comment);
            markerIsDone = true;
        }
    }
}
