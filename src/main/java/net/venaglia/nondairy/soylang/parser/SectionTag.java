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

import static net.venaglia.nondairy.soylang.lexer.SoyToken.*;

import net.venaglia.nondairy.soylang.lexer.SoyToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 23, 2010
 * Time: 5:49:54 PM
 */
public class SectionTag {

    private static final Map<SoyToken,SectionTag> SECTION_TAGS_BY_SOY_TOKEN;

    static {
        Map<SoyToken, SectionTag> sectionTagsBySoyToken = new HashMap<SoyToken, SectionTag>();
        sectionTagsBySoyToken.put(ELSE_IF, new SectionTag(ELSE_IF, IF, true, true));
        sectionTagsBySoyToken.put(ELSE, new SectionTag(ELSE, IF, true, true));
        sectionTagsBySoyToken.put(CASE, new SectionTag(CASE, SWITCH, true, false));
        sectionTagsBySoyToken.put(DEFAULT, new SectionTag(DEFAULT, SWITCH, false, false));
        sectionTagsBySoyToken.put(IF_EMPTY, new SectionTag(IF_EMPTY, FOREACH, false, true));
        sectionTagsBySoyToken.put(PARAM, new SectionTag(PARAM, Arrays.asList(CALL, DELCALL), true, false));
        SECTION_TAGS_BY_SOY_TOKEN = Collections.unmodifiableMap(sectionTagsBySoyToken);
    }

    private final SoyToken token;
    private final List<SoyToken> containerTokens;
    private final boolean repeatable;
    private final boolean orderImportant;

    public SectionTag(SoyToken token, SoyToken containerToken, boolean repeatable, boolean orderImportant) {
        this(token, Collections.singletonList(containerToken), repeatable, orderImportant);
    }
    
    public SectionTag(SoyToken token, List<SoyToken> containerTokens, boolean repeatable, boolean orderImportant) {
        this.token = token;
        this.containerTokens = containerTokens;
        this.repeatable = repeatable;
        this.orderImportant = orderImportant;
    }

    public SoyToken getToken() {
        return token;
    }

    public Collection<SoyToken> getContainerTokens() {
        return containerTokens;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public boolean isOrderImportant() {
        return orderImportant;
    }

    public static SectionTag getBySoyToken(SoyToken token) {
        return SECTION_TAGS_BY_SOY_TOKEN.get(token);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionTag that = (SectionTag)o;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
