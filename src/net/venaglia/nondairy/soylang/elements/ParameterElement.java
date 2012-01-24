/*
 * Copyright 2012 Ed Venaglia
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
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 1/17/12
 * Time: 6:06 PM
 */
public abstract class ParameterElement extends SoyASTElement implements PsiNamedElement, TemplateMemberElement {

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
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return setName(newElementName);
    }

    @Override
    public String getTemplateName() {
        PsiElementCollection callTo = PATH_TO_CONTAINING_TEMPLATE_NAME.navigate(this);
        LocalTemplateNameRef localTemplateNameRef = (LocalTemplateNameRef)callTo.oneOrNull();
        if (localTemplateNameRef == null) {
            return null;
        }
        String localName = localTemplateNameRef.getName();
        String namespace = getNamespace();
        return (namespace == null) ? localName : namespace + "." + localName;
    }

    @Override
    public String getNamespace() {
        PsiElementCollection elements = PATH_TO_NAMESPACE_NAME.navigate(this);
        NamespaceDefElement namespaceDefElement = (NamespaceDefElement)elements.oneOrNull();
        return namespaceDefElement != null ? namespaceDefElement.getName() : null;
    }

}
