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
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.path.AttributePredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * User: ed
 * Date: 3/19/12
 * Time: 8:21 AM
 */
public class MsgTagWithoutDescriptionInspection extends AbstractSoyInspection {

    @NonNls
    private static final Pattern HAS_DESC = Pattern.compile("\\s*\\S.*");

    private static final PsiElementPath PATH_TO_DESC_VALUE =
            new PsiElementPath(new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               AttributePredicate.hasAttributeWithValue("desc", HAS_DESC).onChildren()).debug("msg_without_desc!desc");

    private static final PsiElementPath PATH_TO_MSG_TAGS_WITHOUT_DESC =
            new PsiElementPath(new ElementTypePredicate(SoyElement.msg_tag).onAllDescendants(),
                               new ElementPredicate() {
                                   @Override
                                   public boolean test(PsiElement element) {
                                       return PATH_TO_DESC_VALUE.navigate(element).isEmpty();
                                   }

                                   @Override
                                   public String toString() {
                                       return "[desc=]"; // NON-NLS
                                   }
                               }).debug("msg_without_desc!tags");

    public MsgTagWithoutDescriptionInspection() {
        super("msg.without.description");
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        PsiElementCollection elements = PATH_TO_MSG_TAGS_WITHOUT_DESC.navigate(file);
        for (PsiElement element : elements) {
            checkCanceled();
            problems.add(manager.createProblemDescriptor(element,
                                                         getMessage(),
                                                         (LocalQuickFix)null,
                                                         ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                         true));
        }
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
