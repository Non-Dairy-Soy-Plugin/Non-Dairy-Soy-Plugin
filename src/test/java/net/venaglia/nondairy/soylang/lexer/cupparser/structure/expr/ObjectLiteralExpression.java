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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.expr;

import java.util.Collections;
import java.util.Map;

/**
 * User: ed
 * Date: 10/31/13
 * Time: 8:15 AM
 */
public class ObjectLiteralExpression extends LiteralExpression<Map<String,Expression>> {

    public static final ObjectLiteralExpression EMPTY = new ObjectLiteralExpression(Collections.<String,Expression>emptyMap());

    public ObjectLiteralExpression(Map<String,Expression> value) {
        super(value);
    }
}
