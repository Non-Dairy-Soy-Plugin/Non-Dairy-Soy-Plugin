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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.psi.ResolveResult;

/**
 * User: ed
 * Date: 2/5/12
 * Time: 12:23 PM
 *
 * Provides an orphaned implementation of
 * {@link com.intellij.psi.PsiPolyVariantReference#multiResolve(boolean)}.
 */
public interface ResolveHandler<E extends SoyPsiElementReference> {

    /**
     * Returns the results of resolving an element reference.
     *
     * @see com.intellij.psi.PsiPolyVariantReference#multiResolve(boolean)
     *
     * @param reference the element reference being resolved
     * @param incompleteCode if true, the code in the context of which the reference is
     * being resolved is considered incomplete, and the method may return additional
     * invalid results.
     * @return the array of results for resolving the reference.
     */
    ResolveResult[] resolve(E reference, boolean incompleteCode);
}
