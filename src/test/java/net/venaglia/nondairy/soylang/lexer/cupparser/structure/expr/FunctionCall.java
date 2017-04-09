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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 10:17:53 PM
 */
public class FunctionCall implements Expression {

    private final List<Expression> args;
    private final Expression func;

    public FunctionCall(Expression func, List<Expression> args) {
        this.func = func;
        this.args = args == null ? Collections.<Expression>emptyList() : Collections.unmodifiableList(args);
    }

    public List<Expression> getArgs() {
        return args;
    }

    public Expression getFunc() {
        return func;
    }

    public <T> T getValue(Class<T> conversion) {
        return null;  // todo
    }
}
