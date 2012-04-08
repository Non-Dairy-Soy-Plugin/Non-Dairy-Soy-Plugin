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

package net.venaglia.nondairy.soylang.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.AbsoluteTemplateNameRef;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameRef;
import net.venaglia.nondairy.soylang.elements.TemplateMemberElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementMapper;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import net.venaglia.nondairy.soylang.elements.path.SoyFileElementTraversalPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: ed
 * Date: 3/30/12
 * Time: 11:08 PM
 */
public class CallToUndeclaredTemplateInspection extends AbstractSoyInspection {

    private static PsiElementMapper<String> GET_NAME = new PsiElementMapper<String>() {
        @Override
        public String map(PsiElement element) {
            return ((PsiNamedElement)element).getName();
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

    private static final PsiElementPath PATH_TO_TEMPLATE_DEFS =
            new PsiElementPath(new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildrenOfChildren(),
                               new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                               new ElementTypePredicate(SoyElement.template_name).onChildrenOfChildren()).debug("call_to_undeclared!local");

    private static final PsiElementPath PATH_TO_CALLS =
            new PsiElementPath(new ElementTypePredicate(SoyElement.call_tag).onAllDescendants(),
                               new ElementTypePredicate(SoyElement.template_name_ref, SoyElement.template_name_ref_absolute).onChildrenOfChildren()).debug("call_to_undeclared!calls");

    private static final PsiElementPath PATH_TO_FILE =
            new PsiElementPath(PushPopPredicate.push(),
                               SoyFileElementTraversalPredicate.filesStartingOnNamespaceElement(),
                               new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag).onChildrenOfChildren(),
                               PushPopPredicate.popAndJoin(POP_JOIN)).debug("call_to_undeclared!file");

    public CallToUndeclaredTemplateInspection() {
        super("call.undeclared.template");
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        Set<String> localNames = null;
        for (PsiElement element : PATH_TO_CALLS.navigate(file)) {
            checkCanceled();
            boolean isProblem = false;
            if (element instanceof LocalTemplateNameRef) {
                if (localNames == null) {
                    localNames = new HashSet<String>(PATH_TO_TEMPLATE_DEFS.navigate(file).map(GET_NAME));
                }
                if (!localNames.contains(((LocalTemplateNameRef)element).getName())) {
                    isProblem = true;
                }
            } else if (element instanceof AbsoluteTemplateNameRef) {
                if (PATH_TO_FILE.navigate(element).isEmpty()) {
                    isProblem = true;
                }
            }
            if (isProblem) {
                problems.add(manager.createProblemDescriptor(element,
                                                             getMessage(element.getText()),
                                                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                             null,
                                                             isOnTheFly));
            }
        }
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}
