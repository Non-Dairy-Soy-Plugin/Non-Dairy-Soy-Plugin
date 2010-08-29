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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class CallParameterRefElement extends SoyASTElement implements PsiNamedElement {

    private static final PsiElementPath PATH_TO_INVOKED_TEMPLATE_NAME =
                    new PsiElementPath(new ElementTypePredicate(call_tag_pair).onFirstAncestor(),
                                new ElementTypePredicate(call_tag).onChildren(),
                                new ElementTypePredicate(tag_between_braces).onChildren(),
                                new ElementTypePredicate(template_name_ref).onChildren());

    private final ElementPredicate templateNamePredicate;
    private final ElementPredicate parameterNamePredicate;

    public CallParameterRefElement(@NotNull ASTNode node) {
        super(node);
        templateNamePredicate = new ElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return (element instanceof LocalTemplateNameDef) && ((LocalTemplateNameDef)element).getName().equals(getTemplateName());
            }
            @Override
            public String toString() {
                return "[name=" + getTemplateName() + "]"; //NON-NLS
            }
        };
        parameterNamePredicate = new ElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return (element instanceof PsiNamedElement) && getName().equals(((PsiNamedElement)element).getName());
            }
            @Override
            public String toString() {
                return "[name=" + getName() + "]"; //NON-NLS
            }
        };

    }

    @Override
    public PsiReference getReference() {
        String templateName = getTemplateName();
        if (templateName == null) {
            // this call either points outside this file, or a template name is not specified
            return null;
        }
        PsiElementPath pathToTemplateParameters =
                LocalTemplateNameRef.PATH_TO_TEMPLATE_NAMES.append(templateNamePredicate)
                                                           .append(ParameterRefElement.PATH_TO_PARAMETER_DEF);
        int prefix = getText().startsWith("$") ? 1 : 0;
        TextRange textRange = TextRange.from(prefix, getTextLength() - prefix);
        return new SoyASTElementReference(pathToTemplateParameters, parameterNamePredicate, textRange);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        return name.startsWith("$") ? name.substring(1) : name;
    }

    public String getTemplateName() {
        PsiElementCollection callTo = PATH_TO_INVOKED_TEMPLATE_NAME.navigate(this);
        LocalTemplateNameRef localTemplateNameRef = (LocalTemplateNameRef)callTo.oneOrNull();
        return (localTemplateNameRef == null) ? null : localTemplateNameRef.getName();
    }
}
