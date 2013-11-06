/*
 * Copyright 2010 - 2013 Ed Venaglia
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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.NamespacePath;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 *
 * SoyPsiElement that represents namespace name in the namespace soy tag.
 */
public class NamespaceRefElement
        extends SoyPsiElement
        implements ItemPresentation, NamespaceMemberElement, SoyNamedElement {

    public NamespaceRefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (getText().startsWith(".") ^ name.startsWith(".")) {
            name = name.startsWith(".") ? name.substring(1) : "." + name;
        }
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
    }

    @Override
    public String getPresentableText() {
        return getName();
    }

    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return SoyIcons.ALIAS;
    }

    @Override
    public String getCanonicalName() {
        return getNamespace();
    }

    @Override
    public String getNamespace() {
        return getText();
    }

    @Override
    public PsiReference getReference() {
        final String namespace = getName();
        if (namespace == null) {
            return null;
        }

        PsiElementPath path = NamespacePath.forNamespace(namespace).debug("path_to_namespace_from_alias");
        return new SoyPsiElementReference(this, path, PsiElementPath.ANY);
    }
}
