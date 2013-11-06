/*
 * Copyright 2010 - 2013 Ed Venaglia
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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.Expression;
import org.jetbrains.annotations.NonNls;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 9:23:24 PM
 */
public class BeginTag extends AbstractTag {

    private final List<Attribute> attributes;
    private final List<Directive> directives;

    public BeginTag(@NonNls String command, List<Attribute> attributes) {
        this(command, (List<Expression>)null, attributes, null);
    }

    public BeginTag(String command,
                    List<Attribute> attributes,
                    List<Directive> directives) {
        this(command, (List<Expression>)null, attributes, directives);
    }

    public BeginTag(String command,
                    Expression expr,
                    List<Attribute> attributes,
                    List<Directive> directives) {
        this(command, Collections.<Expression>singletonList(expr), attributes, directives);
    }

    public BeginTag(String command,
                    List<Expression> expressions,
                    List<Attribute> attributes,
                    List<Directive> directives) {
        super(command, expressions);
        this.attributes = attributes == null ? Collections.<Attribute>emptyList() : Collections.unmodifiableList(attributes);
        this.directives = directives == null ? Collections.<Directive>emptyList() : Collections.unmodifiableList(directives);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Directive> getDirectives() {
        return directives;
    }
}
