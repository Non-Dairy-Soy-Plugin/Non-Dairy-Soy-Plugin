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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.BeginTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.EndTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.UnaryTag;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 24, 2010
 * Time: 4:19:37 PM
 */
public class AlternatePathTag extends ContentTagPair {

    public AlternatePathTag(AlternatePathTagBuffer buffer, EndTag endTag) {
        super(buffer.getBeginTag(), endTag, toList(buffer));
    }

    public AlternatePathTag(BeginTag beginTag, EndTag endTag, List<ContentWrapper<?>> contents) {
        super(beginTag, endTag, contents);
    }

    private static List<ContentWrapper<?>> toList(AlternatePathTagBuffer buffer) {
        List<ContentWrapper<?>> list = new LinkedList<ContentWrapper<?>>();
        for (int i = 0, j = buffer.size(); i < j; ++i) {
            UnaryTag tag = buffer.getAlternatePathTag(i);
            if (tag != null) list.add(ContentWrapper.create(tag));
            list.addAll(buffer.getContents(i));
        }
        return list;
    }
}
