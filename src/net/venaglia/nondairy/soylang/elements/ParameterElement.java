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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 1/17/12
 * Time: 6:06 PM
 *
 * Common base class for SoyPsiElement implementations that represent template
 * parameters.
 */
public abstract class ParameterElement
        extends SoyPsiElement
        implements SoyNamedElement, TemplateMemberElement {

    protected ParameterElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        return name.startsWith("$") ? name.substring(1) : name;
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        if (getText().startsWith("$") ^ name.startsWith("$")) {
            name = name.startsWith("$") ? name.substring(1) : "$" + name;
        }
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
    }

    @Override
    public String getCanonicalName() {
        String tn = getTemplateName();
        return (tn == null ? "" : tn) + "$" + getName();
    }

    @Override
    public String getTemplateName() {
        PsiElement element = PATH_TO_CONTAINING_TEMPLATE_NAME.navigate(this).oneOrNull();
        if (element instanceof TemplateMemberElement) {
            return ((TemplateMemberElement)element).getTemplateName();
        }
        return null;
    }

    @Override
    public String getNamespace() {
        PsiElementCollection elements = PATH_TO_NAMESPACE_NAME.navigate(this);
        NamespaceMemberElement namespaceDefElement = (NamespaceMemberElement)elements.oneOrNull();
        return namespaceDefElement != null ? namespaceDefElement.getNamespace() : null;
    }

}
