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

import com.intellij.codeInspection.CommonProblemDescriptorImpl;
import com.intellij.codeInspection.HintAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.ProblemGroup;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * User: ed
 * Date: 4/1/12
 * Time: 5:36 PM
 */
public class MockProblemDescriptor extends CommonProblemDescriptorImpl implements ProblemDescriptor {

    private final PsiElement startElement;
    private final PsiElement endElement;
    private final ProblemHighlightType highlightType;
    private final boolean afterEndOfLine;
    private final TextRange rangeInElement;
    private final boolean tooltip;
    private final HintAction hintAction;
    private final boolean onTheFly;

    private int lineNumber = -1;
    private TextAttributesKey key;
    private ProblemGroup problemGroup;

    public MockProblemDescriptor(PsiElement startElement,
                                 PsiElement endElement,
                                 String descriptionTemplate,
                                 LocalQuickFix[] fixes,
                                 ProblemHighlightType highlightType,
                                 boolean isAfterEndOfLine,
                                 TextRange rangeInElement,
                                 boolean tooltip,
                                 HintAction hintAction,
                                 boolean onTheFly) {
        super(fixes, descriptionTemplate);
        assertNotNull(fixes);
        assertNotNull(descriptionTemplate);
        assertNotNull(startElement);
        assertNotNull(endElement);
        assertNotNull(highlightType);
        assertTrue(startElement.getTextOffset() <= endElement.getTextOffset());
        this.startElement = startElement;
        this.endElement = endElement;
        this.highlightType = highlightType;
        this.afterEndOfLine = isAfterEndOfLine;
        this.rangeInElement = rangeInElement;
        this.tooltip = tooltip;
        this.hintAction = hintAction;
        this.onTheFly = onTheFly;
    }

    @Override
    public PsiElement getPsiElement() {
        PsiElement startElement = getStartElement();
        PsiElement endElement = getEndElement();
        if (startElement == endElement) {
            return startElement;
        }
        return PsiTreeUtil.findCommonParent(startElement, endElement);
    }

    @Override
    public PsiElement getStartElement() {
        return startElement;
    }

    @Override
    public PsiElement getEndElement() {
        return endElement;
    }

    @Override
    public int getLineNumber() {
        if (lineNumber == -1) {
            int l = 0;
            String source = getStartElement().getContainingFile().getText();
            int offset = source.lastIndexOf('\n', getStartElement().getTextOffset());
            while (offset > 0) {
                l++;
                offset = source.lastIndexOf('\n', offset - 1);
            }
            lineNumber = l;
        }
        return lineNumber;
    }

    @Override
    public ProblemHighlightType getHighlightType() {
        return highlightType;
    }

    @Override
    public boolean isAfterEndOfLine() {
        return afterEndOfLine;
    }

    @Override
    public void setTextAttributes(TextAttributesKey key) {
        this.key = key;
    }

    @Override
    public boolean showTooltip() {
        return tooltip;
    }

    public TextRange getTextRangeInElement() {
        return rangeInElement;
    }

    public boolean isTooltip() {
        return tooltip;
    }

    public HintAction getHintAction() {
        return hintAction;
    }

    public boolean isOnTheFly() {
        return onTheFly;
    }

    public TextAttributesKey getKey() {
        return key;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        String source;
        if (startElement != endElement) {
            source = startElement.getText() + "..." + endElement.getText();
        } else {
            source = startElement.getText();
        }
        return "Problem: " + getDescriptionTemplate() + " -> \"" + source + "\" with fixes " + Arrays.asList(getFixes());
    }

    @Override
    public void setProblemGroup(@Nullable ProblemGroup problemGroup) {
        this.problemGroup = problemGroup;
    }

    @Nullable
    @Override
    public ProblemGroup getProblemGroup() {
        return problemGroup;
    }
}
