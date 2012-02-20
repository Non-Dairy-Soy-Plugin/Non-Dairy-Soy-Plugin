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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.psi.PsiElement;

/**
 * User: ed
 * Date: 1/22/12
 * Time: 10:48 AM
 *
 * A predicate that matches all elements.
 */
public class AlwaysTruePredicate extends AbstractElementPredicate {

    public static final AlwaysTruePredicate INSTACE = new AlwaysTruePredicate();

    private AlwaysTruePredicate() { }

    @Override
    public boolean test(PsiElement element) {
        return true;
    }
}
