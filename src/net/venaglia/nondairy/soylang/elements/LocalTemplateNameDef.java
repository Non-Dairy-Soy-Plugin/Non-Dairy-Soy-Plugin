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
public class LocalTemplateNameDef
        extends SoyPsiElement
        implements SoyNamedElement, ItemPresentation, TemplateMemberElement {

    private static final PsiElementPath PARAMETER_DECLARATION_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.doc_comment).onChildren(),
                               new ElementTypePredicate(SoyElement.doc_comment_param).onChildren());
    private static final PsiElementPath PRIVATE_ATTRIBUTE_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.template_tag).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               AttributePredicate.hasAttributeWithValue("private","true").onChildren());
    private static final PsiElementPath DELTEMPLATE_PATH =
            new PsiElementPath(PsiElementPath.PARENT_ELEMENT,
                               new ElementTypePredicate(SoyElement.command_keyword).onFirstChild(),
                               new ElementTextPredicate("deltemplate"));
    
    public LocalTemplateNameDef(@NotNull ASTNode node) {
        super(node);
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
        return getTemplateName();
    }

    @Override
    public String getTemplateName() {
        String namespace = getNamespace();
        return namespace == null ? getName() : namespace + "." + getName();
    }

    @Override
    @Nullable
    public String getNamespace() {
        PsiElement namespace = PATH_TO_NAMESPACE_NAME.navigate(this).oneOrNull();
        return namespace instanceof NamespaceMemberElement
               ? ((NamespaceMemberElement)namespace).getNamespace()
               : null;
    }

    private boolean isPrivate() {
        return !PRIVATE_ATTRIBUTE_PATH.navigate(this).isEmpty();
    }

    public PsiElementCollection getParameterDeclarations() {
        return PARAMETER_DECLARATION_PATH.navigate(this);
    }

    @Override
    public String getPresentableText() {
        String namespace = getNamespace();
        String name = getText();
        boolean dotted = name.startsWith(".");
        if ((namespace == null || namespace.length() == 0) && dotted) {
            return name.substring(1);
        }
        if (dotted) {
            return namespace + name;
        }
        return namespace + "." + name;
    }

    @Override
    public String getLocationString() {
        return getNamespace();
    }

    @Override
    public Icon getIcon(boolean open) {
        boolean isDelegate = !DELTEMPLATE_PATH.navigate(this).isEmpty();
        RowIcon icon = new RowIcon(2);
        icon.setIcon(isDelegate ? SoyIcons.DELTEMPLATE : SoyIcons.TEMPLATE, 0);
        icon.setIcon(isPrivate() ? PlatformIcons.PRIVATE_ICON : PlatformIcons.PUBLIC_ICON, 1);
        return icon;
    }

    @Override
    public boolean isDefinitionElement() {
        return true;
    }
}
