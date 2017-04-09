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

import com.intellij.extapi.psi.PsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.ModuleRef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 10:21:04 PM
 *
 * PsiElement base class used by the plugin. This class implements helper
 * methods that can be overridden to consolidate the functionality specific to
 * the variety of structures of closure templates.
 */
public class SoyPsiElement extends PsiElementBase implements PsiElement, ModuleRef {

    private static final AtomicLong LAST_CREATED = new AtomicLong();

    @NotNull
    private final ASTNode node;

    public SoyPsiElement(@NotNull ASTNode node) {
        LAST_CREATED.set(System.currentTimeMillis());
        this.node = node;
    }

    @Override
    @NotNull
    public ASTNode getNode() {
        return node;
    }

    @Override
    public PsiElement getParent() {
        return TreeNavigator.INSTANCE.getParent(getNode());
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        return TreeNavigator.INSTANCE.getAllChildren(getNode());
    }

    @Override
    public PsiElement getFirstChild() {
        return TreeNavigator.INSTANCE.getFirstChild(getNode());
    }

    @Override
    public PsiElement getLastChild() {
        return TreeNavigator.INSTANCE.getLastChild(getNode());
    }

    @Override
    public PsiElement getNextSibling() {
        return TreeNavigator.INSTANCE.getNextSibling(getNode());
    }

    @Override
    public PsiElement getPrevSibling() {
        return TreeNavigator.INSTANCE.getPrevSibling(getNode());
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
        return TreeNavigator.INSTANCE.getNthChild(getNode(), offset);
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
        String n = null;
        String l = null;
        ItemPresentation presentation = this.getPresentation();
        if (presentation != null) {
            n = presentation.getPresentableText();
            l = presentation.getLocationString();
        }
        if (n == null) {
            n = getCanonicalName();
        }
        if (n == null) {
            n = getName();
        }
        @NonNls
        String fmt;
        if (l != null) {
            fmt = "%s [%s]: <%4$s> \"%3$s\"";
        } else if (n != null) {
            fmt = "%s [%s]: \"%s\"";
        } else {
            fmt = "%s [%s]";
        }
        return String.format(fmt, getClass().getSimpleName(), getNode().getElementType(), n, l);
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Rename is not implemented for " + getClass().getSimpleName());
    }

    /**
     * Returns a string that can be used as a unique name for the element
     * represented by implementing class. Subclasses that also implement
     * {@link PsiNamedElement} should provide an appropriate implementation of
     * this method.
     * @return a string that can be used as a canonical name for this element.
     */
    @Nullable
    public String getCanonicalName() {
        return null;
    }

    /**
     * @return true if this SoyNamedElement is one that is references, false if
     *     this element references another.
     * @see net.venaglia.nondairy.soylang.elements.SoyNamedElement#isDefinitionElement()
     */
    public boolean isDefinitionElement() {
        return false;
    }

    /**
     * @return the Module the containing file belongs to.
     */
    @Nullable
    public Module getModule() {
        PsiElement element = getParent();
        if (element instanceof ModuleRef) {
            return ((ModuleRef)element).getModule();
        }
        return null;
    }

    /**
     * This method provides the timestamp of the most recently created
     * SoyPsiElement. This timestamp is useful for knowing when cached data
     * that relates to SoyPsiElement objects should be invalidated.
     * @return a timestamp indicating the last time a SoyPsiElement was created.
     */
    public static long getLastCreated() {
        return LAST_CREATED.get();
    }

}
