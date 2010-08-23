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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.lexer.SoyToken;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 2, 2010
 * Time: 8:18:40 PM
 */
public class SoyStructureParser {

    private TokenSource source;

    public SoyStructureParser(TokenSource source) {
        this.source = source;
    }

    /**
     * Parses the body of a soy file.
     * @see net.venaglia.nondairy.soylang.lexer.SoyScanner#YYINITIAL
     */
    public void parse() {
        PsiBuilder.Marker marker = source.mark();
//        PsiBuilder.Marker docBeginMarker = null;
        while (!source.eof()) {
            IElementType token = source.token();
            if (token == SoyToken.TAG_LBRACE || token == SoyToken.TAG_END_LBRACE) {
                new TagParser(source).parse();
//                if (docBeginMarker != null) {
//                    docBeginMarker.done(tag_and_doc_comment);
//                    docBeginMarker = null;
//                }
            } else if (SoyToken.DOC_COMMENT == token) {
//                if (docBeginMarker != null) {
//                    docBeginMarker.drop();
//                }
//                docBeginMarker = source.mark();
                new DocParser(source).parse();
            } else {
//                if (docBeginMarker != null) {
//                    docBeginMarker.drop();
//                    docBeginMarker = null;
//                }
                if (token == SoyToken.IGNORED_TEXT || token == SoyToken.TEMPLATE_TEXT || token == SoyToken.LITERAL_TEXT) {
                    source.advance();
                } else {
                    source.advanceAndMarkBad(unexpected_symbol, I18N.msg("lexer.error.unexpected.token", token));
                }
            }
        }
        marker.done(soy_file);
    }

    private void parseToEndOfTag(PsiBuilder.Marker marker, IElementType type) {
        boolean end = source.token() == SoyToken.TAG_RBRACE;
        while (!source.eof()) {
            source.advance();
            if (end) break;
            end = source.token() == SoyToken.TAG_RBRACE;
        }
        marker.done(type);
    }

    private void parseToEndOfComment(PsiBuilder.Marker marker, IElementType type) {
        boolean end = source.token() == SoyToken.DOC_COMMENT_END;
        while (!source.eof()) {
            source.advance();
            if (end) break;
            end = source.token() == SoyToken.DOC_COMMENT_END;
        }
        marker.done(type);
    }

    /**
     * Parses the body of a soy doc comment.
     * @see net.venaglia.nondairy.soylang.lexer.SoyScanner#DOCS
     * @see net.venaglia.nondairy.soylang.lexer.SoyScanner#DOCS_BOL
     * @see net.venaglia.nondairy.soylang.lexer.SoyScanner#DOCS_IDENT
     */
    private void parseDoc() {
        PsiBuilder.Marker commentMarker = source.mark();
        source.advance();
        PsiBuilder.Marker textMarker = source.mark();
        try {
            while (!source.eof()) {
                IElementType token = source.token();
                if (token == SoyToken.DOC_COMMENT) {
                    // nothing to do, keep accumulating
                    source.advance();
                } else if (token == SoyToken.DOC_COMMENT_TAG) {
                    source.advanceAndMark(doc_comment_param);
                } else if (token == SoyToken.DOC_COMMENT_IDENTIFIER) {
                    source.advanceAndMark(doc_comment_param);
                } else if (token == SoyToken.DOC_COMMENT_END) {
                    textMarker.done(doc_comment_text);
                    textMarker = null;
                    source.advance();
                    commentMarker.done(doc_comment);
                    commentMarker = null;
                    return;
                } else {
                    source.advanceAndMarkBad(unexpected_symbol);
                    return;
                }
            }
        } finally {
            if (textMarker != null) textMarker.done(doc_comment_text);
            if (commentMarker != null) commentMarker.drop();
        }
    }
}
