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

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.SoyFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: ed
 * Date: 3/17/12
 * Time: 1:30 PM
 */
public abstract class AbstractSoyInspection extends LocalInspectionTool {

    @Nls private final String displayName;
    @NonNls private final String message;
    @NonNls private final String shortName;
    @Nls private final String description;

    protected AbstractSoyInspection(@NotNull @NonNls String i18nBase) {
        this.displayName = I18N.msg("inspection." + i18nBase + ".label");
        this.message = "inspection." + i18nBase + ".message";
        String shortName = getClass().getSimpleName();
        if (shortName.endsWith("Inspection")) { //NON-NLS
            shortName = shortName.substring(0, shortName.length() - 10);
        }
        this.shortName = shortName;
        this.description = I18N.msg("inspection." + i18nBase + ".description");
    }
    
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return I18N.msg("inspection.group.title");
    }

    @NonNls
    @NotNull
    @Override
    public String getShortName() {
        return shortName;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getStaticDescription() {
        return description;
    }

    protected final String getMessage() {
        return I18N.msg(message);
    }

    protected final String getMessage(Object... args) {
        return I18N.msg(message, args);
    }

    protected void checkCanceled() {
        ProgressManager.checkCanceled();

    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file,
                                         @NotNull InspectionManager manager,
                                         boolean isOnTheFly) {
        if (!(file instanceof SoyFile) || file.getVirtualFile() == null) {
            return null;
        }
        List<ProblemDescriptor> problems = new SmartList<ProblemDescriptor>();
        findProblems((SoyFile)file, manager, isOnTheFly, problems);
        if (problems.isEmpty()) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }
        return problems.toArray(new ProblemDescriptor[problems.size()]);
    }

    protected abstract void findProblems(@NotNull SoyFile file,
                                         @NotNull InspectionManager manager,
                                         boolean isOnTheFly,
                                         @NotNull List<ProblemDescriptor> problems);
}
