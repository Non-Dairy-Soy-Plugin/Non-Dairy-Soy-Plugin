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
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 2/2/12
 * Time: 6:34 PM
 *
 * Interface to be implemented by SoyPsiElements that live within a soy
 * namespace.
 */
public interface DelegateMemberElement extends PsiElement {

    static final PsiElementPath PATH_TO_DELEGATE_PACKAGE =
            new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.package_def).onChildren())
                    .debug("path_to_delegate_package");

    /**
     * @return The name of the delegate package this namespace element is
     *     within, or null is there is no delegate package.
     */
    @Nullable
    String getDelegatePackage();

}
