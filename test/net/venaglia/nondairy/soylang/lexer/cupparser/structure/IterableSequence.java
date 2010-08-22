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

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.CapturedIdentifier;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.Expression;
import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.FloatLiteralExpression;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 24, 2010
 * Time: 6:36:36 PM
 */
public class IterableSequence {

    private final CapturedIdentifier identifier;
    private final Expression initial, terminal, increment;
    private final Expression valueList;

    private IterableSequence(CapturedIdentifier identifier,
                             List<Expression> rangeArgs,
                             Expression valueList) {
        this.identifier = identifier;
        if (rangeArgs == null || rangeArgs.size() < 2) {
            this.initial = new FloatLiteralExpression(0);
            this.terminal = get(rangeArgs, 0, 0);
            this.increment = new FloatLiteralExpression(1);
        } else {
            this.initial = rangeArgs.get(0);
            this.terminal = rangeArgs.get(1);
            this.increment = get(rangeArgs, 2, 1);
        }
        this.valueList = valueList;
    }

    private static Expression get(List<Expression> list, int index, double defaultValue) {
        return (list != null && list.size() > index) ? list.get(index) : new FloatLiteralExpression(defaultValue);
    }

    public IterableSequence(CapturedIdentifier identifier) {
        this(identifier, null, null);
    }

    public IterableSequence(CapturedIdentifier identifier, Expression valueList) {
        this(identifier, null, valueList);
    }

    public IterableSequence(CapturedIdentifier identifier, List<Expression> rangeArgs) {
        this(identifier, rangeArgs, null);
    }

    public CapturedIdentifier getIdentifier() {
        return identifier;
    }

    public Expression getInitial() {
        return initial;
    }

    public Expression getTerminal() {
        return terminal;
    }

    public Expression getIncrement() {
        return increment;
    }

    public Expression getValueList() {
        return valueList;
    }
}
