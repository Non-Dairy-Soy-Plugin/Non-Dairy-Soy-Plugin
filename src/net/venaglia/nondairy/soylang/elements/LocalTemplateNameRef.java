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
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 */
public class LocalTemplateNameRef extends SoyASTElement implements PsiNamedElement {

    public static final PsiPath PATH_TO_TEMPLATE_NAMES =
                    new PsiPath(new ElementTypePredicate(soy_file).onFirstAncestor(),
                                new ElementTypePredicate(tag_and_doc_comment).onChildren(),
                                new ElementTypePredicate(template_tag_pair).onChildren(),
                                new ElementTypePredicate(template_tag).onChildren(),
                                new ElementTypePredicate(tag_between_braces).onChildren(),
                                new ElementTypePredicate(template_name).onChildren())
                .or(new PsiPath(new ElementTypePredicate(soy_file).onFirstAncestor(),
                                new ElementTypePredicate(template_tag_pair).onChildren(),
                                new ElementTypePredicate(template_tag).onChildren(),
                                new ElementTypePredicate(tag_between_braces).onChildren(),
                                new ElementTypePredicate(template_name).onChildren()));

    private final ElementPredicate templateNamePredicate;

    public LocalTemplateNameRef(@NotNull ASTNode node) {
        super(node);
        templateNamePredicate = new ElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return (element instanceof LocalTemplateNameDef) &&
                        getName().equals(((LocalTemplateNameDef)element).getName());
            }
            @Override
            public String toString() {
                return "[name=" + getName() + "]"; //NON-NLS
            }
        };
    }

    @Override
    public PsiReference getReference() {
        int prefix = getText().startsWith(".") ? 1 : 0;
        TextRange textRange = TextRange.from(prefix, getTextLength() - prefix);
        return new SoyASTElementReference(PATH_TO_TEMPLATE_NAMES, templateNamePredicate, textRange);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        if (name.startsWith(".")) {
            name = name.substring(1);
        }
        return name;
    }

    @Nullable
    public LocalTemplateNameDef getTemplate() {
        return (LocalTemplateNameDef)PATH_TO_TEMPLATE_NAMES.navigate(this).oneOrNull();
    }

//    @Override
//    protected void buildLookupElements(PsiElement element, Collection<? super LookupElement> buffer) {
//        if (!(element instanceof PsiNamedElement)) return;
//        String yourName = ((PsiNamedElement)element).getName();
//        if (yourName == null) return;
//        if (getText().startsWith(".")) {
//            // this is a relative reference, so the namespace must be the same
//            String myName = getName();
//            int myLastDot = myName.lastIndexOf(".");
//            int yourLastDot = yourName.lastIndexOf(".");
//            if (myLastDot == yourLastDot && myName.substring(0, myLastDot + 1).equals(yourName.substring(yourLastDot + 1))) {
//                buffer.add(LookupElementBuilder.create(element, yourName.substring(yourLastDot)));
//            }
//        } else {
//            // this is a fully qualified reference
//            buffer.add(LookupElementBuilder.create(element, yourName));
//        }
//    }
}
