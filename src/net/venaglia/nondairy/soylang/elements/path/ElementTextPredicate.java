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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:30:25 AM
 */
public class ElementTextPredicate extends AbstractElementPredicate {

    private final Pattern pattern;

    public ElementTextPredicate(@NotNull @NonNls String text) {
        this.pattern = Pattern.compile(text, Pattern.LITERAL);
    }

    public ElementTextPredicate(@NotNull Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(PsiElement element) {
        return pattern.matcher(element.getText()).matches();
    }

    @Override
    public String toString() {
        return "[text=/" + pattern + "/]"; //NON-NLS
    }
}
