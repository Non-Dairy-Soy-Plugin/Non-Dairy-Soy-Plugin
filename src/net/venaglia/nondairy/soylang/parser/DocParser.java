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
 * Date: Aug 18, 2010
 * Time: 10:17:54 PM
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
        docMarker = source.mark();
    }

    public void parse() {
        IElementType lastToken = null;
        PsiBuilder.Marker lastMarker = null;
        IElementType lastMarkerElementType = doc_comment_text;
        while (!source.eof()) {
            IElementType token = source.token();
            if (token == SoyToken.DOC_COMMENT_END) {
                if (lastMarker != null) {
                    lastMarker.done(lastMarkerElementType);
                }
                source.advance();
                break;
            } else if (!SoyToken.DOC_COMMENT_TOKENS.contains(token)) {
                if (lastMarker != null) {
                    lastMarker.done(lastMarkerElementType);
                }
                source.error(I18N.msg("lexer.error.unexpected.token", token));
                break;
            }
            if (token != lastToken && token == SoyToken.DOC_COMMENT_TAG) {
                if (lastMarker != null) {
                    lastMarker.done(lastMarkerElementType);
                }
                lastMarker = source.mark();
            }
            source.advance();
            lastToken = token;
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
