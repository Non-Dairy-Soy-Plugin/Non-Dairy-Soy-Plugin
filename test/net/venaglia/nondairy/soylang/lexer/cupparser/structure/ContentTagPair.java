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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.BeginTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.EndTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.UnaryTag;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 9:41:35 PM
 */
public class ContentTagPair {

    private final BeginTag beginTag;
    private final EndTag endTag;
    private final List<ContentWrapper<?>> contents;

    public ContentTagPair(UnaryTag tag) {
        this(tag, null, (List<ContentWrapper<?>>)null);
    }

    public ContentTagPair(BeginTag beginTag, EndTag endTag) {
        this(beginTag, endTag, (List<ContentWrapper<?>>)null);
    }

    public ContentTagPair(BeginTag beginTag, EndTag endTag, ContentWrapper<?> singelton) {
        this(beginTag, endTag, Collections.<ContentWrapper<?>>singletonList(singelton));
    }

    public ContentTagPair(BeginTag beginTag, EndTag endTag, List<ContentWrapper<?>> contents) {
        this.beginTag = beginTag;
        this.endTag = endTag;
        this.contents = contents == null ? Collections.<ContentWrapper<?>>emptyList() : Collections.unmodifiableList(contents);
    }

    public BeginTag getBeginTag() {
        return beginTag;
    }

    public EndTag getEndTag() {
        return endTag;
    }

    public List<ContentWrapper<?>> getContents() {
        return contents;
    }
}
