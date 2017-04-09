/*
 * Copyright 2010 - 2014 Ed Venaglia
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: ed
 * Date: 1/27/12
 * Time: 8:03 PM
 *
 * SoyPsiElement that represents a template or deltemplate soy tag.
 */
public class DelegateTemplateDefElement extends SoyCommandTag implements SoyNamedElement, ItemPresentation {

    private static final PsiElementPath PATH_TO_DELTEMPLATE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.deltemplate_name).onChildren()
    ).debug("path_to_deltemplate_name");

    private static final PsiElementPath PATH_TO_PACKAGE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.template_name).onChildren()
    ).debug("path_to_package_name");

    public DelegateTemplateDefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isDefinitionElement() {
        return true;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Nullable
    public String getTemplateName() {
        return getName();
    }

    public String getDelegatePackage() {
        PsiElement element = PATH_TO_PACKAGE_NAME.navigate(this).oneOrNull();
        if (element instanceof DelegatePackageElement) {
            return ((DelegatePackageElement)element).getDelegatePackage();
        }
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        return SoyIcons.DELTEMPLATE;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        String packageName = getDelegatePackage();
        String templateName = getTemplateName();
        return packageName == null ? templateName : "[" + packageName + "] " + templateName;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return getDelegatePackage();
    }
}
