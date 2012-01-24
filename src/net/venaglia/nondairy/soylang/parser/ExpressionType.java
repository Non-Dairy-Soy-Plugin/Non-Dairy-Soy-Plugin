/*
 * Copyright 2012 Ed Venaglia
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

import net.venaglia.nondairy.i18n.I18N;
import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 1/23/12
 * Time: 7:19 PM
 */
public enum ExpressionType {
    ANY, STRING, NUMBER, BOOLEAN, NONE;

    public ExpressionType and(ExpressionType type) {
        switch (this) {
            case ANY:
                return type;
            case NONE:
                return NONE;
        }
        return type == this ? this : NONE;
    }

    public ExpressionType or(ExpressionType type) {
        switch (this) {
            case ANY:
                return ANY;
            case NONE:
                return type;
        }
        return type == this || type == NONE ? this : ANY;
    }
    
    public boolean isAssignableTo(ExpressionType type) {
        switch (type) {
            case ANY:
            case STRING:
            case BOOLEAN:
                return true;
            case NUMBER:
                return this == NUMBER;
        }
        return false;
    }

    public String getLabel() {
        @NonNls String key = "syntax.expression.type.unknown";
        switch (this) {
            case ANY:
                key = "syntax.expression.type.any";
                break;
            case STRING:
                key = "syntax.expression.type.string";
                break;
            case NUMBER:
                key = "syntax.expression.type.number";
                break;
            case BOOLEAN:
                key = "syntax.expression.type.boolean";
                break;
        }
        return I18N.msg(key);
    }
}
