/*
 * Copyright 2012 Ed Venaglia
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

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: 1/20/12
* Time: 10:09 PM
* To change this template use File | Settings | File Templates.
*/
public class SoyASTElementReference extends PsiReferenceBase<SoyASTElement> {

    private final PsiElementPath path;
    private final ElementPredicate predicate;
    private final SoyASTElement soyASTElement;

    public SoyASTElementReference(SoyASTElement soyASTElement) {
        this(soyASTElement, PsiElementPath.SELF, null);
    }

    public SoyASTElementReference(SoyASTElement soyASTElement, TextRange range) {
        this(soyASTElement, PsiElementPath.SELF, null, range);
    }

    @SuppressWarnings({ "unchecked" })
    public SoyASTElementReference(SoyASTElement soyASTElement,
                                  @NotNull PsiElementPath path,
                                  @Nullable ElementPredicate predicate) {
        super(soyASTElement);
        this.soyASTElement = soyASTElement;
        this.path = path;
        this.predicate = predicate;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        if (myElement instanceof TemplateMemberElement) {
            String templateName = ((TemplateMemberElement)myElement).getTemplateName();
            if (templateName != null) {
                if (myElement instanceof ParameterElement) {
                    return templateName + "$" + myElement.getName();
                }
                return templateName;
            }
        }
        return super.getCanonicalText();
    }

    public SoyASTElementReference(SoyASTElement soyASTElement,
                                  PsiElementPath path,
                                  @Nullable ElementPredicate predicate,
                                  TextRange range) {
        super(soyASTElement, range);
        this.soyASTElement = soyASTElement;
        this.path = path;
        this.predicate = predicate;
    }

    @Override
    public PsiElement resolve() {
        PsiElementCollection elements = path.navigate(soyASTElement);
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
        PsiElementCollection elements = path.navigate(soyASTElement);
        Collection<Object> objects = new LinkedList<Object>();
        for (PsiElement element : elements) {
            soyASTElement.buildLookupElements(element, objects);
        }
        return objects.toArray();
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return soyASTElement.handleElementRename(newElementName);
    }
}
