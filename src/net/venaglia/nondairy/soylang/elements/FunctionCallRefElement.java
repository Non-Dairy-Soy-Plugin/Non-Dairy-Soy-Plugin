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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.DummyHolderElement;
import com.intellij.psi.impl.source.IdentityCharTable;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyLanguage;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ed
 * Date: 1/17/12
 * Time: 7:31 PM
 *
 * SoyPsiElement that represents a function invocation within a soy expression.
 */
public class FunctionCallRefElement extends SoyPsiElement implements PsiNamedElement, ItemPresentation {

    private FunctionCallRefElement.FunctionElementReference functionElementReference;

    public FunctionCallRefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        functionElementReference = null;
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
    }

    @Override
    public PsiReference getReference() {
        if (functionElementReference == null) {
            functionElementReference = new FunctionElementReference();
        }
        return functionElementReference;
    }

    @Override
    public String getPresentableText() {
        return getText();
    }

    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return SoyIcons.FUNCTION;
    }

    /**
     * Custom reference object that resolves to a dummy psi element for the
     * encapsulating FunctionCallRefElement.
     */
    // someday this could find the Java class that implements SoyFunction
    private class FunctionElementReference extends SoyPsiElementReference {

        private final String functionName;
        private final FunctionDefElement element;

        public FunctionElementReference() {
            super(FunctionCallRefElement.this);
            functionName = FunctionCallRefElement.this.getText();
            element = new FunctionDefElement(functionName);
            new DummyHolder(FunctionCallRefElement.this.getManager(),
                            new DummyHolderElement(functionName),
                            null,
                            IdentityCharTable.INSTANCE,
                            true,
                            SoyLanguage.INSTANCE);
        }

        @Override
        public PsiElement resolve() {
            return element;
        }

        @Override
        public boolean isReferenceTo(PsiElement element) {
            return (element instanceof FunctionDefElement ||
                    element instanceof FunctionCallRefElement) &&
                   functionName.equals(element.getText());
        }

        @Override
        public boolean isSoft() {
            return true;
        }
    }

    /**
     * A dummy PsiElement to act as a surrogate reference for
     * FunctionCallRefElement objects.
     */
    private static class FunctionDefElement extends SoyPsiElement implements ItemPresentation {

        private final String name;

        private FunctionDefElement(@NotNull String name) {
            super(new LazyParseableElement(SoyElement.function_call_name, name));
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPresentableText() {
            return name;
        }

        @Override
        public String getLocationString() {
            return null;
        }

        @Override
        public Icon getIcon(boolean open) {
            return SoyIcons.FUNCTION;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FunctionDefElement && name.equals(((FunctionDefElement)o).getName());
        }
    }

}
