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

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import net.venaglia.nondairy.i18n.I18N;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 3/23/12
 * Time: 6:19 PM
 */
public abstract class AbstractSoyInspectionWithSingleQuickFix extends AbstractSoyInspection {

    private static final Object[] EMPTY_ARGS = {};
    
    @NonNls private final String quickFixName;

    protected AbstractSoyInspectionWithSingleQuickFix(@NotNull @NonNls String i18nBase) {
        super(i18nBase);
        this.quickFixName = "inspection." + i18nBase + ".fix";
    }

    protected final LocalQuickFix getQuickFix() {
        return new QuickFix();
    }

    protected final LocalQuickFix getQuickFix(Object... params) {
        return new QuickFix(params);
    }

    /**
     * Called to apply the fix.
     *
     * @param project    {@link com.intellij.openapi.project.Project}
     * @param descriptor problem reported by the tool which provided this quick fix action
     * @see QuickFix#applyFix(com.intellij.openapi.project.Project, com.intellij.codeInspection.ProblemDescriptor)
     */
    protected abstract void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor);

    private class QuickFix implements LocalQuickFix {

        private final Object[] args;
        
        private QuickFix() {
            this(EMPTY_ARGS);
        }

        private QuickFix(Object... args) {
            this.args = args;
        }

        @NotNull
        @Override
        public String getName() {
            return I18N.msg(quickFixName, args);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            AbstractSoyInspectionWithSingleQuickFix.this.applyFix(project, descriptor);
        }
    }
}
