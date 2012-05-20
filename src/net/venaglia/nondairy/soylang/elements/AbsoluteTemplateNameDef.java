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
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.ui.RowIcon;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.AttributePredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTextPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:36:22 PM
 *
 * SoyPsiElement that represents the template name within a soy template tag.
 */
public class AbsoluteTemplateNameDef
        extends SoyPsiElement
        implements SoyNamedElement, ItemPresentation, TemplateMemberElement {

    private static final PsiElementPath PARAMETER_DECLARATION_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.doc_comment).onChildren(),
                               new ElementTypePredicate(SoyElement.doc_comment_param_def).onChildren());
    private static final PsiElementPath PRIVATE_ATTRIBUTE_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.template_tag).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               AttributePredicate.hasAttributeWithValue("private","true").onChildren());
    private static final PsiElementPath DELTEMPLATE_PATH =
            new PsiElementPath(PsiElementPath.PARENT_ELEMENT,
                               new ElementTypePredicate(SoyElement.command_keyword).onFirstChild(),
                               new ElementTextPredicate("deltemplate"));

    public AbsoluteTemplateNameDef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (getText().startsWith(".") ^ name.startsWith(".")) {
            name = name.startsWith(".") ? name.substring(1) : "." + name;
        }
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, name);
    }
    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        return processor.execute(this, state);
    }

    @Override
    public String getCanonicalName() {
        return getName();
    }

    @Override
    public String getTemplateName() {
        return getName();
    }

    @Override
    @Nullable
    public String getNamespace() {
        String name = getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(0, lastDot);
        }
        return "";
    }

    @Override
    @Nullable
    public String getDelegatePackage() {
        PsiElementCollection elements = PATH_TO_DELEGATE_PACKAGE.navigate(this);
        DelegatePackageElement delegatePackageElement = (DelegatePackageElement)elements.oneOrNull();
        return delegatePackageElement != null ? delegatePackageElement.getDelegatePackage() : null;
    }

    private boolean isPrivate() {
        return !PRIVATE_ATTRIBUTE_PATH.navigate(this).isEmpty();
    }

    public PsiElementCollection getParameterDeclarations() {
        return PARAMETER_DECLARATION_PATH.navigate(this);
    }

    @Override
    public String getPresentableText() {
        return getName();
    }

    @Override
    public String getLocationString() {
        return getNamespace();
    }

    @Override
    public Icon getIcon(boolean open) {
        return SoyIcons.DELTEMPLATE;
    }

    @Override
    public boolean isDefinitionElement() {
        return true;
    }
}
