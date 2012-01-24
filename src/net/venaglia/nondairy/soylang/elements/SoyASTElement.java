/*
 * Copyright 2010 Ed Venaglia
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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.extapi.psi.PsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 31, 2010
 * Time: 10:21:04 PM
 */
public class SoyASTElement extends PsiElementBase implements PsiElement {

    @NotNull
    private final ASTNode node;

    public SoyASTElement(@NotNull ASTNode node) {
        this.node = node;
    }

    @Override
    @NotNull
    public ASTNode getNode() {
        return node;
    }

    @Override
    public PsiElement getParent() {
        return SharedImplUtil.getParent(getNode());
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        PsiElement psiChild = getFirstChild();
        if (psiChild == null) return PsiElement.EMPTY_ARRAY;

        List<PsiElement> result = new ArrayList<PsiElement>();
        while (psiChild != null) {
            if (psiChild.getNode() instanceof CompositeElement) {
                result.add(psiChild);
            }
            psiChild = psiChild.getNextSibling();
        }
        return PsiUtilCore.toPsiElementArray(result);
    }

    @Override
    public PsiElement getFirstChild() {
        return SharedImplUtil.getFirstChild(getNode());
    }

    @Override
    public PsiElement getLastChild() {
        return SharedImplUtil.getLastChild(getNode());
    }

    @Override
    public PsiElement getNextSibling() {
        return SharedImplUtil.getNextSibling(getNode());
    }

    @Override
    public PsiElement getPrevSibling() {
        return SharedImplUtil.getPrevSibling(getNode());
    }

    @Override
    public TextRange getTextRange() {
        return getNode().getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return getNode().getStartOffset() - getNode().getTreeParent().getStartOffset();
    }

    @Override
    public int getTextLength() {
        return getNode().getTextLength();
    }

    @Override
    public PsiElement findElementAt(int offset) {
        ASTNode treeElement = getNode().findLeafElementAt(offset);
        return SourceTreeToPsiMap.treeElementToPsi(treeElement);
    }

    @Override
    public int getTextOffset() {
        return getNode().getStartOffset();
    }

    @Override
    public String getText() {
        return getNode().getText();
    }

    @Override
    @NotNull
    public char[] textToCharArray() {
        return getNode().getText().toCharArray();
    }

    @Override
    public boolean textContains(char c) {
        return getNode().textContains(c);
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return getNode().getElementType().getLanguage();
    }

    @Override
    public ItemPresentation getPresentation() {
        return (this instanceof ItemPresentation) ? (ItemPresentation)this : null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getNode().getElementType() + "]";
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Rename is not implemented for " + getClass().getSimpleName());
    }

    protected void buildLookupElements(PsiElement element, Collection<? super LookupElement> buffer) {
        buffer.add(LookupElementBuilder.create((PsiNamedElement)element));
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }
}
