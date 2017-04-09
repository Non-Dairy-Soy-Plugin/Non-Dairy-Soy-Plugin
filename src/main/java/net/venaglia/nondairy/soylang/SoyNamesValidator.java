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

package net.venaglia.nondairy.soylang;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * User: ed
 * Date: 1/22/12
 * Time: 11:18 AM
 *
 * NamesValidator implementation for the closure template language.
 */
public class SoyNamesValidator implements NamesValidator {

    @NonNls
    public static Pattern MATCH_VALID_IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    
    @NonNls
    private static final Set<String> KEYWORDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "delpacakge", "namespace", "template", "deltemplate",
        "print", "literal", "msg", "if", "elseif", "else", "switch", "case", "default",
        "foreach", "ifempty", "for", "call", "delcall", "param", "css",
        "in", "not", "and", "or", "null", "true", "false"
    )));

    @Override
    public boolean isKeyword(String name, Project project) {
        return KEYWORDS.contains(name);
    }

    @Override
    public boolean isIdentifier(String name, Project project) {
        return MATCH_VALID_IDENTIFIER.matcher(name).matches();
    }
}
