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

package net.venaglia.nondairy.mocks;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 4/1/12
 * Time: 1:27 PM
 */
public class MockInspectionManager extends InspectionManager {

    @NotNull
    @Override
    public Project getProject() {
        return MockProjectEnvironment.getUnitTestProject();
    }

    @NotNull
    @Override
    public CommonProblemDescriptor createProblemDescriptor(@NotNull String descriptionTemplate, QuickFix... fixes) {
        return new CommonProblemDescriptorImpl(fixes, descriptionTemplate);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     LocalQuickFix fix,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean onTheFly) {
        return new MockProblemDescriptor(psiElement, psiElement, descriptionTemplate, new LocalQuickFix[]{fix}, highlightType, false, null, false, null, onTheFly);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     boolean onTheFly,
                                                     LocalQuickFix[] fixes,
                                                     @NotNull ProblemHighlightType highlightType) {
        return new MockProblemDescriptor(psiElement, psiElement, descriptionTemplate, fixes, highlightType, false, null, false, null, onTheFly);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     LocalQuickFix[] fixes,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean onTheFly,
                                                     boolean isAfterEndOfLine) {
        return new MockProblemDescriptor(psiElement, psiElement, descriptionTemplate, fixes, highlightType, isAfterEndOfLine, null, false, null, onTheFly);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement startElement,
                                                     @NotNull PsiElement endElement,
                                                     @NotNull String descriptionTemplate,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean onTheFly,
                                                     LocalQuickFix... fixes) {
        return new MockProblemDescriptor(startElement, endElement, descriptionTemplate, fixes, highlightType, false, null, false, null, onTheFly);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     TextRange rangeInElement,
                                                     @NotNull String descriptionTemplate,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean onTheFly,
                                                     LocalQuickFix... fixes) {
        return new MockProblemDescriptor(psiElement, psiElement, descriptionTemplate, fixes, highlightType, false, rangeInElement, false, null, onTheFly);
    }

    @NotNull
    @Override
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     boolean showTooltip,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean onTheFly,
                                                     LocalQuickFix... fixes) {
        return new MockProblemDescriptor(psiElement, psiElement, descriptionTemplate, fixes,  highlightType, false, null, showTooltip, null, onTheFly);
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     LocalQuickFix fix,
                                                     @NotNull ProblemHighlightType highlightType) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     LocalQuickFix[] fixes,
                                                     @NotNull ProblemHighlightType highlightType) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     LocalQuickFix[] fixes,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     boolean isAfterEndOfLine) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement startElement,
                                                     @NotNull PsiElement endElement,
                                                     @NotNull String descriptionTemplate,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     LocalQuickFix... fixes) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     TextRange rangeInElement,
                                                     @NotNull String descriptionTemplate,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     LocalQuickFix... fixes) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    @Deprecated
    public ProblemDescriptor createProblemDescriptor(@NotNull PsiElement psiElement,
                                                     @NotNull String descriptionTemplate,
                                                     boolean showTooltip,
                                                     @NotNull ProblemHighlightType highlightType,
                                                     LocalQuickFix... fixes) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public GlobalInspectionContext createNewGlobalContext(boolean reuse) {
        throw new UnsupportedOperationException();
    }
}
