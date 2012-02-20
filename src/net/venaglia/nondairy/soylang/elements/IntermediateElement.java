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

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 1/17/12
 * Time: 8:00 PM
 *
 * An interface that represents a PsiElement that is built when the tree is
 * parsed, but needs further processing before being added to the tree.
 */
public interface IntermediateElement extends PsiElement {

    /**
     * @return The final element that should be inserted into the tree.
     */
    PsiElement resolveFinalElement();
}
