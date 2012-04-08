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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 3/26/12
 * Time: 8:14 PM
 */
public class DelegatePackageElement extends SoyCommandTag {

    private static final PsiElementPath PATH_TO_DELEGATE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.package_name).onChildren()
    ).debug("path_to_delegate_name");
    
    public DelegatePackageElement(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @NonNls
    public String getDelegatePackage() {
        PsiElement element = PATH_TO_DELEGATE_NAME.navigate(this).oneOrNull();
        if (element != null) {
            return element.getText();
        }
        return null;
    }
}
