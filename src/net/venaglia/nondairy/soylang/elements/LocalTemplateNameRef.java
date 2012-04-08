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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplateNamePredicate;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 *
 * PsiElement implementation that represents the local template name in a call
 * or delcall soy tag.
 */
public class LocalTemplateNameRef
        extends SoyPsiElement
        implements SoyNamedElement, ItemPresentation, TemplateMemberElement {

    public static final PsiElementPath PATH_TO_TEMPLATE_NAMES =
                    new PsiElementPath(new ElementTypePredicate(soy_file).onFirstAncestor(),
                                       new ElementTypePredicate(template_tag_pair).onDescendants(1,2),
                                       new ElementTypePredicate(template_tag).onChildren(),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(template_name).onChildren()).debug("path_to_template_names");

    public LocalTemplateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        final String myTemplateName = getTemplateName();
        if (myTemplateName == null) {
            return null;
        }

        ElementPredicate templateNamePredicate = new TemplateNamePredicate(myTemplateName);
        return new SoyPsiElementReference(this, PATH_TO_TEMPLATE_NAMES, templateNamePredicate);//.bound(BIND_HANDLER);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        if (name.startsWith(".")) {
            name = name.substring(1);
        }
        return name;
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        if (getText().startsWith(".") ^ name.startsWith(".")) {
            name = name.startsWith(".") ? name.substring(1) : "." + name;
        }
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
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
        return getTemplateName();
    }

    @Override
    public String getTemplateName() {
        String localName = getName();
        String namespace = getNamespace();
        return (namespace == null) ? localName : namespace + "." + localName;
    }

    @Override
    public String getDelegatePackage() {
        PsiElementCollection elements = PATH_TO_DELEGATE_PACKAGE.navigate(this);
        DelegatePackageElement delegatePackageElement = (DelegatePackageElement)elements.oneOrNull();
        return delegatePackageElement != null ? delegatePackageElement.getDelegatePackage() : null;
    }

    @Override
    public String getNamespace() {
        PsiElement namespace = PATH_TO_NAMESPACE_NAME.navigate(this).oneOrNull();
        return namespace instanceof NamespaceMemberElement
               ? ((NamespaceMemberElement)namespace).getNamespace()
               : null;
    }
}
