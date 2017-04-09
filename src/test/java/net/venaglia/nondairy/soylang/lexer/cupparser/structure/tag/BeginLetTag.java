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

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 24, 2010
 * Time: 7:39:17 PM
 */
public class BeginLetTag extends BeginTag {

    private final String identifier;
    private final Expression valueExpression;

    public BeginLetTag(String identifier) {
        this(identifier, null);
    }

    public BeginLetTag(String identifier, Expression valueExpression) {
        super("let", null);
        this.identifier = identifier;
        this.valueExpression = valueExpression;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Expression getValueExpression() {
        return valueExpression;
    }
}
