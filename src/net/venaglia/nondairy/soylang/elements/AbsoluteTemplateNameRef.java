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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplatePath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 *
 * PsiElement implementation that represents the fully qualified template name
 * in a call or delcall soy tag.
 */
public class AbsoluteTemplateNameRef extends SoyPsiElement implements SoyNamedElement, ItemPresentation, TemplateMemberElement {

    public AbsoluteTemplateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        String text = getText();
        int i = text.lastIndexOf('.');
        if (i >= 0) {
            text = text.substring(i + 1);
        }
        return text;
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        String prefix = getText();
        int i = prefix.lastIndexOf('.');
        prefix = prefix.substring(0, i + 1);
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, prefix + name);
    }

    @Override
    public PsiReference getReference() {
        String templateName = getTemplateName();
        if (templateName == null) return null;
        PsiElementPath pathToTemplateName = TemplatePath.forTemplateName(templateName)
                .debug("for_template_name!absolute");
        return new SoyPsiElementReference(this, pathToTemplateName, null);
    }

    @Override
    public String getPresentableText() {
        return getTemplateName();
    }

    @Override
    public String getLocationString() {
        return getNamespace();
    }

    @Override
    public Icon getIcon(boolean open) {
        return null;
    }

    @Override
    public String getCanonicalName() {
        return getText();
    }

    @Override
    public String getTemplateName() {
        return getText();
    }

    @Override
    public String getNamespace() {
        String name = getText();
        int index = name.lastIndexOf('.');
        return index > 1 ? name.substring(0, index) : null;
    }
}
