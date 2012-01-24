/*
 * Copyright 2010 Ed Venaglia
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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplateNamePredicate;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 */
public class LocalTemplateNameRef extends SoyASTElement implements PsiNamedElement, TemplateMemberElement {

    public static final PsiElementPath PATH_TO_TEMPLATE_NAMES =
                    new PsiElementPath(new ElementTypePredicate(soy_file).onFirstAncestor(),
                                       new ElementTypePredicate(tag_and_doc_comment).onChildren(),
                                       new ElementTypePredicate(template_tag_pair).onChildren(),
                                       new ElementTypePredicate(template_tag).onChildren(),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(template_name).onChildren())
                .or(new PsiElementPath(new ElementTypePredicate(soy_file).onFirstAncestor(),
                                       new ElementTypePredicate(template_tag_pair).onChildren(),
                                       new ElementTypePredicate(template_tag).onChildren(),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(template_name).onChildren()));

    public LocalTemplateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return setName(newElementName);
    }

    @Override
    public PsiReference getReference() {
        final String myTemplateName = getTemplateName();
        if (myTemplateName == null) {
            return null;
        }

        ElementPredicate templateNamePredicate = new TemplateNamePredicate(myTemplateName);
        return new SoyASTElementReference(this, PATH_TO_TEMPLATE_NAMES, templateNamePredicate);
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

    @Nullable
    public LocalTemplateNameDef getTemplate() {
        return (LocalTemplateNameDef)PATH_TO_TEMPLATE_NAMES.navigate(this).oneOrNull();
    }

    @Override
    public String getTemplateName() {
        String localName = getName();
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
