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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.NamePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplateNamePredicate;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class CallParameterRefElement extends ParameterElement {

    private static final PsiElementPath PATH_TO_INVOKED_TEMPLATE_NAME =
                    new PsiElementPath(new ElementTypePredicate(param_tag).onFirstAncestor(),
                                       new ElementTypePredicate(call_tag).onPreviousSiblings(false),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(template_name_ref, template_name_ref_absolute).onChildren());

    private final ElementPredicate parameterNamePredicate;

    public CallParameterRefElement(@NotNull ASTNode node) {
        super(node);
        parameterNamePredicate = new NamePredicate(getName());
    }

    @Override
    public PsiReference getReference() {
        final String myTemplateName = getTemplateName();
        if (myTemplateName == null) {
            // this call either points outside this file, or a template name is not specified
            return null;
        }

        ElementPredicate templateNamePredicate = new TemplateNamePredicate(myTemplateName);
        PsiElementPath pathToTemplateParameters =
                LocalTemplateNameRef.PATH_TO_TEMPLATE_NAMES.append(templateNamePredicate)
                                                           .append(ParameterRefElement.PATH_TO_PARAMETER_DEF);
        return new SoyASTElementReference(this, pathToTemplateParameters, parameterNamePredicate);
    }

    @Override
    public String getTemplateName() {
        PsiElement element = PATH_TO_INVOKED_TEMPLATE_NAME.navigate(this).oneOrNull();
        if (element instanceof TemplateMemberElement) {
            return ((TemplateMemberElement)element).getTemplateName();
        }
        return null;
    }

    @Override
    public String getNamespace() {
        PsiElement element = PATH_TO_INVOKED_TEMPLATE_NAME.navigate(this).oneOrNull();
        if (element instanceof TemplateMemberElement) {
            return ((TemplateMemberElement)element).getNamespace();
        }
        return null;
    }

}
