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
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: ed
 * Date: 3/19/12
 * Time: 8:21 AM
 */
public class MsgTagInIteratorInspection extends AbstractSoyInspection {

    private static final PsiElementPath PATH_TO_MSG_TAGS_IN_ITERATOR_TAGS =
            new PsiElementPath(new ElementTypePredicate(SoyElement.iterator_tag_pair).onAllDescendants(),
                               new ElementTypePredicate(SoyElement.msg_tag).onAllDescendants()).debug("msg_in_iterator");

    public MsgTagInIteratorInspection() {
        super("msg.in.iterator");
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

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String getStaticDescription() {
        return super.getStaticDescription() + "\n" +
                "<code>{for $i in range(1,5)}</code>\n" +
                "<code>    {msg desc=\"repeated\"}...{/msg}</code>\n" +
                "<code>{/for}</code>";
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        PsiElementCollection elements = PATH_TO_MSG_TAGS_IN_ITERATOR_TAGS.navigate(file);
        for (PsiElement element : elements) {
            checkCanceled();
            problems.add(manager.createProblemDescriptor(element,
                                                         getMessage(),
                                                         ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                         null,
                                                         true));
        }
    }
}
