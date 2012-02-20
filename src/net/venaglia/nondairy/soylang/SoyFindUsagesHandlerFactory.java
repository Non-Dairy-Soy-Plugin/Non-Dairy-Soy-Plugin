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

package net.venaglia.nondairy.soylang;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import net.venaglia.nondairy.soylang.elements.SoyNamedElement;
import net.venaglia.nondairy.soylang.elements.SoyPsiElement;
import net.venaglia.nondairy.soylang.elements.SoyPsiElementReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * User: ed
 * Date: 2/19/12
 * Time: 9:20 AM
 */
public class SoyFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        return element instanceof SoyNamedElement;
    }

    @Override
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        if (element instanceof SoyNamedElement) {
            if (forHighlightUsages) {
                return new HighlightUsagesHandler(element);
            }
            return new FindUsagesHandler(element) {};
        }
        return null;
    }
    
    private static class HighlightUsagesHandler extends FindUsagesHandler {

        private HighlightUsagesHandler(@NotNull PsiElement psiElement) {
            super(psiElement);
        }

        @Override
        public Collection<PsiReference> findReferencesToHighlight(PsiElement target, SearchScope searchScope) {
            Collection<PsiReference> results = new HashSet<PsiReference>();
            SoyPsiElement soyPsiElement = (SoyPsiElement)getPsiElement();
            if (soyPsiElement.isDefinitionElement()) {
                results.add(new SoyPsiElementReference(soyPsiElement));
            }
            results.addAll(super.findReferencesToHighlight(target, searchScope));
            return results;
        }
    }
}
