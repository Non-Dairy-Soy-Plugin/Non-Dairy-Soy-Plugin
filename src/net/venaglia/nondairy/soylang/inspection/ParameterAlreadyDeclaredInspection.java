/*
 * Copyright 2010 - 2013 Ed Venaglia
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
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.ParameterDefElement;
import net.venaglia.nondairy.soylang.elements.ParameterRefElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.ParameterPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: ed
 * Date: 11/5/13
 * Time: 5:45 PM
 */
public class ParameterAlreadyDeclaredInspection extends AbstractSoyInspection {

    private static final PsiElementPath FIND_PARAMS_DECLARED_INLINE = new PsiElementPath(
            new ElementTypePredicate(SoyElement.soy_file).onChildren(),
            new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
            new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
            new ElementTypePredicate(SoyElement.parameter_def, SoyElement.let_parameter_def).onAllDescendants()
    ).debug("already_declared_parameters!declarations");

    public ParameterAlreadyDeclaredInspection() {
        super("parameter.name.conflict");
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        PsiElementCollection inlineDeclarations = FIND_PARAMS_DECLARED_INLINE.navigate(file);
        for (PsiElement element : inlineDeclarations) {
            if (element instanceof ParameterDefElement) {
                String parameterName = ((ParameterDefElement)element).getName();
                ParameterPredicate predicate = new ParameterPredicate(parameterName);
                PsiElementPath path = ParameterRefElement.PATH_TO_PARAMETER_DEF.append(predicate);
                PsiElementCollection def = path.navigate(element);
                def.remove(element);
                if (!def.isEmpty()) {
                    problems.add(manager.createProblemDescriptor(element,
                                                                 getMessage(parameterName),
                                                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                                 null,
                                                                 true));
                }
            }
        }
    }
}
