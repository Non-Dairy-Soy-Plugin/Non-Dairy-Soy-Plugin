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
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.UnaryTag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 24, 2010
 * Time: 4:15:05 PM
 */
public class AlternatePathTagBuffer {

    private final ArrayList<AlternatePath> alternatePaths = new ArrayList<AlternatePath>();
    private final BeginTag beginTag;
    private AlternatePath lastAlternatePath;

    public AlternatePathTagBuffer(BeginTag beginTag) {
        this.beginTag = beginTag;
        append((UnaryTag)null);
    }

    public BeginTag getBeginTag() {
        return beginTag;
    }

    public AlternatePathTagBuffer append(UnaryTag alternatePathTag) {
        alternatePaths.add(lastAlternatePath = new AlternatePath(alternatePathTag));
        return this;
    }

    public AlternatePathTagBuffer append(List<ContentWrapper<?>> contents) {
        if (contents != null) lastAlternatePath.contents.addAll(contents);
        return this;
    }

    public int size() {
        return alternatePaths.size();
    }

    public UnaryTag getAlternatePathTag(int i) {
        return alternatePaths.get(i).alternatePathTag;
    }

    public List<ContentWrapper<?>> getContents(int i) {
        return alternatePaths.get(i).contents;
    }
    
    private static class AlternatePath {
        final UnaryTag alternatePathTag;
        final List<ContentWrapper<?>> contents;

        private AlternatePath(UnaryTag alternatePathTag) {
            this.alternatePathTag = alternatePathTag;
            this.contents = new LinkedList<ContentWrapper<?>>();
        }
    }
}
