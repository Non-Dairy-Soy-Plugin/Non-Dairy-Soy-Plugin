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

package net.venaglia.nondairy.soylang.lexer;

import org.jetbrains.annotations.NonNls;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 7:32:11 PM
 */
class SoySyntaxUtil {

    @NonNls
    private static final Pattern MATCH_TERMINAL_NAME = Pattern.compile("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*");

    @NonNls
    private static final Pattern MATCH_NON_TERMINAL_NAME = Pattern.compile("[a-z][a-z0-9]*(_[a-z0-9]+)*");

    static Map<Integer,String> extractSymbolNames(Class<?> klass) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>();
        for (Field field : klass.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) &&
                Integer.TYPE.equals(field.getType()) && MATCH_TERMINAL_NAME.matcher(field.getName()).matches()) {
                try {
                    map.put((Integer)field.get(null), field.getName());
                } catch (IllegalAccessException e) {
                    // should never happen
                }
            }
            if (Modifier.isProtected(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) &&
                Integer.TYPE.equals(field.getType()) && MATCH_NON_TERMINAL_NAME.matcher(field.getName()).matches()) {
                try {
                    map.put((Integer)field.get(null), field.getName());
                } catch (IllegalAccessException e) {
                    // should never happen
                }
            }
        }
        return map;
    }
}
