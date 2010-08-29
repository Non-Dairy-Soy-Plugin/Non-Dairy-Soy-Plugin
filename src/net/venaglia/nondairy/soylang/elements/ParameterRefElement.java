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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiPath;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class ParameterRefElement extends SoyASTElement implements PsiNamedElement {

    public static final PsiPath PATH_TO_PARAMETER_DEF =
                    new PsiPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                                new ElementTypePredicate(doc_comment).onChildren(),
                                new ElementTypePredicate(doc_comment_param).onChildren())
                .or(new PsiPath(new ElementTypePredicate(iterator_tag_pair).onAncestors(),
                                new ElementTypePredicate(iterator_tag).onChildren(),
                                new ElementTypePredicate(tag_between_braces).onChildren(),
                                new ElementTypePredicate(parameter_def).onChildren())
           .exclude(new PsiPath(new ElementTypePredicate(tag_between_braces).onFirstAncestor(),
                                new ElementTypePredicate(iterator_tag).onParent(),
                                new ElementTypePredicate(tag_between_braces).onChildren(),
                                new ElementTypePredicate(parameter_def).onChildren())));

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
        int prefix = getText().startsWith("$") ? 1 : 0;
        TextRange textRange = TextRange.from(prefix, getTextLength() - prefix);
        return new SoyASTElementReference(PATH_TO_PARAMETER_DEF, parameterNamePredicate, textRange);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        return name.startsWith("$") ? name.substring(1) : name;
    }
}
