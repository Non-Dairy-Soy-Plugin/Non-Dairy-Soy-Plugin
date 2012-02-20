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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:46:27 PM
 *
 * Extension of {@link ElementPredicate} that permits traversal of the psi tree
 * before executing the predicate.
 */
public interface TraversalPredicate extends ElementPredicate {

    /**
     * Transforms the passed collection of psi elements to another, by 
     * following the traversal defined by the implementation class.
     * @param current The starting collection of psi elements 
     * @return A collection of psi elements after traversal.
     */
    @NotNull
    PsiElementCollection traverse(@NotNull Collection<PsiElement> current);

    /**
     * If the element collection returned by
     * {@link #traverse(java.util.Collection)} contains no element found
     * acceptable by {@link #test(com.intellij.psi.PsiElement)}, the collection
     * may be traversed again.
     *
     * Typically this method will return false. An implementation that is
     * expected to repeat traversal until the first match is found, should
     * return true.
     * @return true if traversal should be repeated until a match is found,
     *     false otherwise.
     */
    boolean traverseAgainIfNoMatch();
}
