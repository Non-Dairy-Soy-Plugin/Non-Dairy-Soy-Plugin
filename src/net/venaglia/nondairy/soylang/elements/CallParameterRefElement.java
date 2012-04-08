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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.NamePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import net.venaglia.nondairy.soylang.elements.path.SoyFileElementTraversalPredicate;
import net.venaglia.nondairy.soylang.elements.path.TemplatePath;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 *
 * SoyPsiElement that represents a parameter reference within a soy param tag.
 */
public class CallParameterRefElement extends ParameterElement {

    private static final BindHandler BIND_HANDLER = new BindHandler() {
        @Override
        public PsiElement bind(PsiElement ref, PsiElement def) throws IncorrectOperationException {
            if (ref instanceof CallParameterRefElement && def instanceof ParameterDefElement) {
                ((CallParameterRefElement)ref).setName(((ParameterDefElement)def).getName());
            }
            throw new IncorrectOperationException();
        }
    };

    private static final PushPopPredicate.PopJoin POP_JOIN = new PushPopPredicate.PopJoin() {

        @Override
        public PsiElementCollection join(PsiElementCollection current, PsiElementCollection popped) {
            Set<String> names = new HashSet<String>(popped.size());
            for (PsiElement element : popped) {
                if (element instanceof TemplateMemberElement) {
                    names.add(((TemplateMemberElement)element).getTemplateName());
                }
            }
            PsiElementCollection result = new PsiElementCollection();
            for (PsiElement element : current) {
                if (element instanceof TemplateMemberElement &&
                        names.contains(((TemplateMemberElement)element).getTemplateName())) {
                    result.add(element);
                }
            }
            return result;
        }
    };

    private static final PsiElementPath PATH_TO_INVOKED_TEMPLATE_NAME =
                    new PsiElementPath(new ElementTypePredicate(param_tag).onFirstAncestor(),
                                       new ElementTypePredicate(call_tag).onPreviousSiblings(false),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(template_name_ref, template_name_ref_absolute).onChildren(),
                                       PushPopPredicate.push(),
                                       SoyFileElementTraversalPredicate.filesStartingOnNamespaceElement(),
                                       new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                                       new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                                       new ElementTypePredicate(SoyElement.template_tag).onChildrenOfChildren(),
                                       PushPopPredicate.popAndJoin(POP_JOIN),
                                       new ElementTypePredicate(SoyElement.template_name).onChildrenOfChildren()
)
            .debug("path_to_invoked_template_name");


    public CallParameterRefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        PsiElement element = PATH_TO_INVOKED_TEMPLATE_NAME.navigate(this).oneOrNull();
        if (element instanceof TemplateMemberElement) {
            String myTemplateName = ((TemplateMemberElement)element).getTemplateName();
            if (myTemplateName != null) {
                ElementPredicate parameterNamePredicate = new NamePredicate(getName());
                PsiElementPath pathToTemplateParameters =
                        TemplatePath.forTemplateName(myTemplateName)
                                    .debug("for_template_name!call")
                                    .append(ParameterRefElement.PATH_TO_PARAMETER_DEF)
                                    .debug("path_to_template_params");
                return new SoyPsiElementReference(this, pathToTemplateParameters, parameterNamePredicate).bound(BIND_HANDLER);
            }
        }
        return null;
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
