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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr.Expression;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 23, 2010
 * Time: 9:45:07 PM
 */
public class Directive {

    private final String directive;
    private final List<Expression> params;

    public Directive(String directive) {
        this(directive, null);
    }

    public Directive(String directive, List<Expression> params) {
        this.directive = directive;
        this.params = params == null ? Collections.<Expression>emptyList() : params;
    }

    public String getDirective() {
        return directive;
    }

    public int getParamCount() {
        return params.size();
    }

    public Expression getParam(int index) {
        return params.get(index);
    }
}
