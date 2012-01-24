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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.soylang.elements.path.AlwaysTruePredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class ParameterRefElement extends ParameterElement {

    public static final PsiElementPath PATH_TO_PARAMETER_DEF =
                    new PsiElementPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                                       new ElementTypePredicate(doc_comment).onChildren(),
                                       new ElementTypePredicate(doc_comment_param).onChildren())
                .or(new PsiElementPath(AlwaysTruePredicate.INSTACE.onAncestors(),
                                       new ElementTypePredicate(iterator_tag).onPreviousSiblings(true),
                                       new ElementTypePredicate(tag_between_braces).onChildren(),
                                       new ElementTypePredicate(parameter_def).onChildren()));

    private final ElementPredicate parameterNamePredicate;

    public ParameterRefElement(@NotNull ASTNode node) {
        super(node);
        parameterNamePredicate = new ElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return (element instanceof PsiNamedElement) && getName().equals(((PsiNamedElement)element).getName());
            }
            @Override
            public String toString() {
                return "[name=" + getName() + "]"; //NON-NLS
            }
        };
    }

    @Override
    public PsiReference getReference() {
        return new SoyASTElementReference(this, PATH_TO_PARAMETER_DEF, parameterNamePredicate);
    }
}
