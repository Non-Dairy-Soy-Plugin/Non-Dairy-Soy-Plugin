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

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.SpecialCharacterTag;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.UnaryTag;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 9:59:49 PM
 */
public class ContentWrapper<T> {

    private final T content;

    private ContentWrapper(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    public static ContentWrapper<CapturedText> create(CapturedText value) {
        return new ContentWrapper<CapturedText>(value);
    }

    public static ContentWrapper<ContentTagPair> create(ContentTagPair value) {
        return new ContentWrapper<ContentTagPair>(value);
    }

    public static ContentWrapper<UnaryTag> create(UnaryTag value) {
        return new ContentWrapper<UnaryTag>(value);
    }

    public static ContentWrapper<SpecialCharacterTag> create(SpecialCharacterTag value) {
        return new ContentWrapper<SpecialCharacterTag>(value);
    }

    public static ContentWrapper<Comment> create(Comment value) {
        return new ContentWrapper<Comment>(value);
    }
}
