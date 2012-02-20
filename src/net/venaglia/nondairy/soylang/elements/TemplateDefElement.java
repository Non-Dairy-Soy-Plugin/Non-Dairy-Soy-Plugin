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
import com.intellij.psi.PsiFile;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/27/12
 * Time: 8:03 PM
 *
 * SoyPsiElement that represents a template or deltemplate soy tag.
 */
public class TemplateDefElement extends SoyCommandTag implements TemplateMemberElement {

    private static final PsiElementPath PATH_TO_TEMPLATE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.template_name).onChildren()
    ).debug("path_to_template_name");

    public TemplateDefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public String getFoldedLabel() {
        String localName = getLocalName();
        return localName == null ? null : "." + localName;
    }

    @Override
    @Nullable
    public String getTemplateName() {
        String localName = getLocalName();
        if (localName != null) {
            String namespace = getNamespace();
            return namespace == null ? localName : namespace + "." + localName;
        }
        return null;
    }

    @Nullable
    public String getLocalName() {
        PsiElement element = PATH_TO_TEMPLATE_NAME.navigate(this).oneOrNull();
        return element instanceof LocalTemplateNameDef ? ((LocalTemplateNameDef)element).getName() : null;
    }

    @Override
    public String getNamespace() {
        PsiFile containingFile = getContainingFile();
        NamespaceDefElement ns = containingFile instanceof SoyFile
                                 ? ((SoyFile)containingFile).getNamespaceElement()
                                 : null;
        return ns == null ? null : ns.getName();
    }
}
