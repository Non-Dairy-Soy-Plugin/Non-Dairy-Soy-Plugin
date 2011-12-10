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
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.SoyElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class ParameterDefElement extends SoyASTElement implements PsiNamedElement, ItemPresentation {

    public static final Icon SOY_PARAM_ICON = IconLoader.getIcon("/net/venaglia/nondairy/soylang/icons/soy-param.png");

    public ParameterDefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        String name = getText();
        return name.startsWith("$") ? name.substring(1) : name;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        if (getText().startsWith("$")) name = "$" + name;
        return ElementManipulators.getManipulator(this).handleContentChange(this, getTextRange().shiftRight(0 - getTextOffset()), name);
    }

    @Override
    public String getPresentableText() {
        return getName();
    }

    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return getNode().getElementType() == SoyElement.doc_comment_param ? SOY_PARAM_ICON : null;
    }

//    @Override
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }
}
