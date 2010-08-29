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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:24:32 PM
 */
public class NamespaceDefElement extends SoyASTElement implements ItemPresentation {

    public static final Icon SOY_NAMESPACE_ICON = IconLoader.getIcon("/net/venaglia/nondairy/soylang/icons/soy-namespace.png");

    public NamespaceDefElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
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
        return SOY_NAMESPACE_ICON;
    }

    @Override
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }
}
