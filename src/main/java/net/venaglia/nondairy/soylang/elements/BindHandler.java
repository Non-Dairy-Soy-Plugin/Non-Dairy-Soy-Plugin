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

import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * User: ed
 * Date: 2/4/12
 * Time: 10:58 PM
 *
 * Provides an orphaned implementation of
 * {@link com.intellij.refactoring.rename.BindablePsiReference#bindToElement(com.intellij.psi.PsiElement)}.
 */
public interface BindHandler {

    /**
     * Provide the orphaned implementation of
     * {@link com.intellij.refactoring.rename.BindablePsiReference#bindToElement(com.intellij.psi.PsiElement)}.
     * @param ref The PsiElement the reference originates form.
     * @param def The PsiElement the reference points to.
     * @return the new underlying element of the reference.
     * @throws IncorrectOperationException if the rename should defer to
     *     {@link com.intellij.psi.PsiReference#handleElementRename(String)}.
     */
    public PsiElement bind(PsiElement ref, PsiElement def) throws IncorrectOperationException;
}
