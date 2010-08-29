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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 */
public class AbsoluteTemplateNameRef extends SoyASTElement {

    private final PsiElementPath referencePath;
    private final PsiElementPath namespaceReferencePath;
    private final ElementPredicate templateNamePredicate;

    public AbsoluteTemplateNameRef(@NotNull ASTNode node) {
        super(node);
        templateNamePredicate = new ElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                if ((element instanceof LocalTemplateNameDef)) {
                    LocalTemplateNameDef def = (LocalTemplateNameDef)element;
                    return getName().equals(def.getNamespace() + "." + def.getName());
                }
                return false;
            }
            @Override
            public String toString() {
                return "[name=" + getName() + "]"; //NON-NLS
            }
        };
        referencePath = new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onFirstAncestor(),
                                    new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                                    new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_name).onChildren())
                .or(    new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onAncestors(),
                                    new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                                    new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_name).onChildren()));
        namespaceReferencePath = new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onFirstAncestor(),
                                             new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                                             new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                             new ElementTypePredicate(SoyElement.namespace_name).onChildren());
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }
}
