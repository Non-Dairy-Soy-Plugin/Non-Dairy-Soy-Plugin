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
 * Date: Aug 26, 2010
 * Time: 7:29:49 AM
 *
 * ElementPredicate implementations are used to filter elements, typically when
 * traversing the psi tree.
 *
 * @see TraversalPredicate
 */
public interface ElementPredicate {

    /**
     * Performs an implementation specific test against the passed value
     * @param element The element to test.
     * @return true if the passed element is permitted by this predicate, false
     *     otherwise.
     */
    boolean test(PsiElement element);

    /**
     * User: ed
     * Date: 1/22/12
     * Time: 10:48 AM
     *
     * An interface used to identify predicates that always match all elements.
     */
    public interface AlwaysTrue extends ElementPredicate {

        /**
         * @param element The element to test.
         * @return true, always
         */
        @Override
        boolean test(PsiElement element);
    }
}
