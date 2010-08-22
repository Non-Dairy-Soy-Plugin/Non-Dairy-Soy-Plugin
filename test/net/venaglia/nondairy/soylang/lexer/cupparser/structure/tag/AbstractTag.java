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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.Expression;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 9:20:45 PM
 */
public abstract class AbstractTag {

    private final String command;
    private List<Expression> expressions;

    protected AbstractTag(String command) {
        this(command, null);
    }

    protected AbstractTag(String command, List<Expression> expressions) {
        this.command = command;
        this.expressions = expressions == null ? Collections.<Expression>emptyList() : Collections.unmodifiableList(expressions);
    }

    public String getCommand() {
        return command;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
