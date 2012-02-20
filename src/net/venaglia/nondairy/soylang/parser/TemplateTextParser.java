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

import static com.intellij.psi.xml.XmlTokenType.*;
import static net.venaglia.nondairy.soylang.SoyElement.template_content;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.xml.IXmlElementType;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 23, 2010
 * Time: 4:03:43 PM
 */
public class TemplateTextParser {

    private final TokenSource source;

    TemplateTextParser(TokenSource source) {
        this.source = source;
        if (source.eof()) {
            throw new AssertionError("Cannot begin parsing template text when the lexer is at EOF.");
        }
    }

    public void parse() {
        PsiBuilder.Marker htmlMarker = source.mark("htmlMarker");
        PsiBuilder.Marker htmlTagMarker = null;
        IElementType token = source.token();
        while (!source.eof() && token instanceof IXmlElementType) {
            if (token == XML_START_TAG_START || token == XML_END_TAG_START) {
                if (htmlTagMarker != null) htmlTagMarker.drop();
                htmlTagMarker = source.mark("htmlTagMarker");
            }
            source.advance();
            if (token == XML_TAG_END) {
                if (htmlTagMarker != null) htmlTagMarker.done(template_content);
                htmlTagMarker = null;
            }
            token = source.token();
        }
        if (htmlTagMarker != null) htmlTagMarker.done(template_content);
        htmlMarker.done(template_content);
    }
}
