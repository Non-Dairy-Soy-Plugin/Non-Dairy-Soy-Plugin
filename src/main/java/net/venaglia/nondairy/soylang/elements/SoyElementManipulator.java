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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;

/**
 * User: ed
 * Date: Aug 28, 2010
 * Time: 10:53:30 AM
 *
 * Element manipulator implementation used to manipulate the psi tree in soy files.
 */
public class SoyElementManipulator extends AbstractElementManipulator<SoyPsiElement> {

    @Override
    public SoyPsiElement handleContentChange(SoyPsiElement element, TextRange range, String newContent)
            throws IncorrectOperationException {
        CompositeElement attrNode = (CompositeElement)element.getNode();
        final ASTNode valueNode = attrNode.findLeafElementAt(range.getStartOffset());
        final PsiElement elementToReplace = valueNode.getPsi();

        String text;
        text = elementToReplace.getText();
        final int offsetInParent = elementToReplace.getStartOffsetInParent();
        String textBeforeRange = text.substring(0, range.getStartOffset() - offsetInParent);
        String textAfterRange = text.substring(range.getEndOffset() - offsetInParent, text.length());
        text = textBeforeRange + newContent + textAfterRange;

        final CharTable charTableByTree = SharedImplUtil.findCharTableByTree(attrNode);
        final LeafElement newValueElement = Factory.createSingleLeafElement(valueNode.getElementType(), text, charTableByTree, element.getManager());

        attrNode.replaceChildInternal(valueNode, newValueElement);
        return element;
    }
}
