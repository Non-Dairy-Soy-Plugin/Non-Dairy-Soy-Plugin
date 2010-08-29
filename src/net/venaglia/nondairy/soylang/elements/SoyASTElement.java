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
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 31, 2010
 * Time: 10:21:04 PM
 */
public class SoyASTElement extends ASTWrapperPsiElement {

    public SoyASTElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getNode().getElementType() + "]";
    }

    public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Rename is not implemented for " + getClass().getSimpleName());
    }

    protected class SoyASTElementReference extends PsiReferenceBase<SoyASTElement> {

        private final PsiElementPath path;
        private final ElementPredicate predicate;

        @SuppressWarnings({ "unchecked" })
        public SoyASTElementReference(@NotNull PsiElementPath path, @Nullable ElementPredicate predicate) {
            super(SoyASTElement.this);
            this.path = path;
            this.predicate = predicate;
        }

        public SoyASTElementReference(PsiElementPath path, @Nullable ElementPredicate predicate, TextRange range) {
            super(SoyASTElement.this, range);
            this.path = path;
            this.predicate = predicate;
        }

        @Override
        public PsiElement resolve() {
            PsiElementCollection elements = path.navigate(SoyASTElement.this);
            if (predicate != null) {
                elements = elements.applyPredicate(predicate);
            }
            return elements.oneOrNull();
        }

        @Override
        public boolean isReferenceTo(PsiElement element) {
            return (predicate == null || predicate.test(element)) &&
                    resolve() == element;
        }

        @NotNull
        @Override
        public Object[] getVariants() {
            PsiElementCollection elements = path.navigate(SoyASTElement.this);
            Collection<Object> objects = new LinkedList<Object>();
            for (PsiElement element : elements) {
                buildLookupElements(element, objects);
            }
            return objects.toArray();
        }

        @Override
        public boolean isSoft() {
            return false;
        }
    }

    protected void buildLookupElements(PsiElement element, Collection<? super LookupElement> buffer) {
        buffer.add(LookupElementBuilder.create((PsiNamedElement)element));
    }
}
