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

package net.venaglia.nondairy.soylang.elements;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.CommandBoundaryPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 *
 * This element contains a reference to a template parameter used within a
 * template.
 */
public class ParameterRefElement extends ParameterElement {

    private static final BindHandler BIND_HANDLER = new BindHandler() {
        @Override
        public PsiElement bind(PsiElement ref, PsiElement def) throws IncorrectOperationException {
            if (ref instanceof ParameterRefElement && def instanceof ParameterDefElement) {
                ((ParameterRefElement)ref).setName(((ParameterDefElement)def).getName());
            }
            throw new IncorrectOperationException();
        }
    };

    public static final PsiElementPath PATH_TO_TEMPLATE_DEF =
            new PsiElementPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(doc_comment).onChildren(),
                               new ElementTypePredicate(doc_comment_tag_with_description).onChildren(),
                               new ElementTypePredicate(doc_comment_param_def).onChildren()).debug("path_to_parameter_def!template");

    public static final PsiElementPath PATH_TO_ITERATOR_DEF =
            new PsiElementPath(new ElementTypePredicate(iterator_tag_pair).onAncestors(),
                               new ElementTypePredicate(iterator_tag).onChildren(),
                               new CommandBoundaryPredicate(SoyCommandTag.Boundary.BEGIN),
                               new ElementTypePredicate(tag_between_braces).onChildren(),
                               new ElementTypePredicate(parameter_def).onChildren()).debug("path_to_parameter_def!iterator");

    public static final TokenSet TAG_PAIR_CONTAINERS = TokenSet.andNot(TAG_PAIR_TOKENS, TokenSet.create(let_tag_pair));

    public static final PsiElementPath PATH_TO_LET_DEF =
            new PsiElementPath(new ElementTypePredicate(TAG_PAIR_CONTAINERS).onAncestors(),
                               PushPopPredicate.push(),
                               new ElementTypePredicate(let_tag_pair).onChildren(),
                               PushPopPredicate.popAdd(),
                               new ElementTypePredicate(let_tag).onChildren(),
                               new ElementTypePredicate(tag_between_braces).onChildren(),
                               new ElementTypePredicate(let_parameter_def).onChildren()).debug("path_to_parameter_def!let");

    public static final PsiElementPath PATH_TO_PARAMETER_DEF =
                PATH_TO_TEMPLATE_DEF.or(PATH_TO_ITERATOR_DEF, PATH_TO_LET_DEF).debug("path_to_parameter_def");

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
        return new SoyPsiElementReference(this, PATH_TO_PARAMETER_DEF, parameterNamePredicate).bound(BIND_HANDLER);
    }
}
