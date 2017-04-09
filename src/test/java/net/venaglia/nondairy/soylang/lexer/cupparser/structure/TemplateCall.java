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

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.BeginCallTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.BeginTemplateTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.EndTag;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 9:40:57 PM
 */
public class TemplateCall extends ContentTagPair {

    public TemplateCall(BeginCallTag beginTag, EndTag endTag, List<ContentWrapper<?>> contents) {
        super(beginTag, endTag, contents);
    }

    @Override
    public BeginTemplateTag getBeginTag() {
        return (BeginTemplateTag)super.getBeginTag();
    }
}
