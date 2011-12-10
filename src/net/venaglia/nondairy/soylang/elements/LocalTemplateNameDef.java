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

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.RowIcon;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTextPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:36:22 PM
 */
public class LocalTemplateNameDef extends SoyASTElement implements PsiNamedElement, ItemPresentation {

    public static final Icon SOY_TEMPLATE_ICON = IconLoader.getIcon("/net/venaglia/nondairy/soylang/icons/soy-template.png");

    private final PsiElementPath namespaceReferencePath;
    private final PsiElementPath parameterDeclarationPath;
    private final PsiElementPath privateAttributePath;

    public LocalTemplateNameDef(@NotNull ASTNode node) {
        super(node);
        namespaceReferencePath = new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onFirstAncestor(),
                                             new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                                             new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                             new ElementTypePredicate(SoyElement.namespace_name).onChildren());
        parameterDeclarationPath = new PsiElementPath(new ElementTypePredicate(SoyElement.tag_and_doc_comment).onFirstAncestor(),
                                               new ElementTypePredicate(SoyElement.doc_comment).onChildren(),
                                               new ElementTypePredicate(SoyElement.doc_comment_param).onChildren());
        privateAttributePath = new PsiElementPath(new ElementTypePredicate(SoyElement.template_tag).onFirstAncestor(),
                                           new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                           new ElementTypePredicate(SoyElement.attribute).onChildren(),
                                           new ElementTypePredicate(SoyElement.attribute_key).onChildren(),
                                           new ElementTextPredicate("private"),
                                           PsiElementPath.PARENT_ELEMENT,
                                           new ElementTypePredicate(SoyElement.expression).onChildren(),
                                           new ElementTypePredicate(SoyElement.attribute_value).onChildren(),
                                           new ElementTextPredicate("true"));
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
        if (getText().startsWith(".")) name = "." + name;
        return ElementManipulators.getManipulator(this).handleContentChange(this, getTextRange().shiftRight(0 - getTextOffset()), name);
    }

    @Nullable
    public String getNamespace() {
        PsiElement namespace = namespaceReferencePath.navigate(this).oneOrNull();
        return namespace == null ? null : ((NamespaceDefElement)namespace).getName();
    }

    private boolean isPrivate() {
        return privateAttributePath.navigate(this).oneOrNull() != null;
    }

    public PsiElementCollection getParameterDeclarations() {
        return parameterDeclarationPath.navigate(this);
    }

    @Override
    public String getPresentableText() {
        return getText();
    }

    @Override
    public String getLocationString() {
        return getNamespace();
    }

    @Override
    public Icon getIcon(boolean open) {
        RowIcon icon = new RowIcon(2);
        icon.setIcon(SOY_TEMPLATE_ICON, 0);
        icon.setIcon(isPrivate() ? Icons.PRIVATE_ICON : Icons.PUBLIC_ICON, 1);
        return icon;
    }
}
