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
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 *
 * SoyPsiElement that represents a template parameter definition in the soy doc
 * that precedes a soy template definition. This element may also represent the
 * parameter declaration in a for or foreach soy tag.
 */
public class ParameterDefElement extends ParameterElement implements ItemPresentation {

    public ParameterDefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getPresentableText() {
        return getName();
    }

    @Override
    public String getLocationString() {
        return getTemplateName();
    }

    @Override
    public Icon getIcon(boolean open) {
        return getNode().getElementType() == SoyElement.doc_comment_param_def ? SoyIcons.PARAMETER : null;
    }

    @Override
    public boolean isDefinitionElement() {
        return true;
    }
}
