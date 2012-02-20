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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 *
 * SoyPsiElement that represents the namespace soy tag.
 */
public class NamespaceTagElement extends SoyCommandTag implements NamespaceMemberElement {

    private static final PsiElementPath PATH_TO_NAMESPACE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.namespace_name).onChildren()
    ).debug("path_to_namespace_name");
    
    public NamespaceTagElement(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * @return The fully qualified name of the namespace
     */
    @Override
    @Nullable
    public String getNamespace() {
        PsiElement element = PATH_TO_NAMESPACE_NAME.navigate(this).oneOrNull();
        if (element instanceof NamespaceDefElement) {
            return ((NamespaceDefElement)element).getName();
        }
        return null;
    }
}
