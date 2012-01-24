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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: 1/23/12
* Time: 5:20 PM
* To change this template use File | Settings | File Templates.
*/
public class NamePredicate extends AbstractElementPredicate {

    private final String myName;

    public NamePredicate(@NotNull String myName) {
        this.myName = myName;
    }

    @Override
    public boolean test(PsiElement element) {
        if (element instanceof PsiNamedElement) {
            String name = ((PsiNamedElement)element).getName();
            return myName.equals(name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[name=" + myName + "]"; //NON-NLS
    }
}
