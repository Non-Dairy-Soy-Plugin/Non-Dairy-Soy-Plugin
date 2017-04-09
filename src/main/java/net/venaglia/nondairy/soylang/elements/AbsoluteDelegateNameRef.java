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
import net.venaglia.nondairy.soylang.elements.path.DeltemplatePath;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: ed
 * Date: 5/22/12
 * Time: 6:25 PM
 */
public class AbsoluteDelegateNameRef extends SoyPsiElement implements SoyNamedElement, ItemPresentation, DelegateMemberElement {

    public AbsoluteDelegateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
    }

    @Override
    public PsiReference getReference() {
        String packageName = getDelegatePackage();
        String templateName = getName();
        if (templateName == null) return null;
        PsiElementPath pathToTemplateName;
        if (packageName == null) {
            pathToTemplateName = DeltemplatePath.forTemplateName(templateName).debug("for_deltemplate_name!simple");
        } else {
            pathToTemplateName = DeltemplatePath.forTemplateName(packageName, templateName).debug("for_deltemplate_name!absolute");
        }
        return new SoyPsiElementReference(this, pathToTemplateName, null);
    }

    @Override
    public String getPresentableText() {
        String packageName = getDelegatePackage();
        String templateName = getName();
        return packageName == null ? templateName : "[" + packageName + "] " + templateName;
    }

    @Override
    public String getLocationString() {
        return getDelegatePackage();
    }

    @Override
    public Icon getIcon(boolean open) {
        return SoyIcons.DELTEMPLATE;
    }

    @Override
    @Nullable
    public String getDelegatePackage() {
        PsiElementCollection elements = PATH_TO_DELEGATE_PACKAGE.navigate(this);
        DelegatePackageElement delegatePackageElement = (DelegatePackageElement)elements.oneOrNull();
        return delegatePackageElement != null ? delegatePackageElement.getDelegatePackage() : null;
    }
}
