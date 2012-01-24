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
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 */
public class AbsoluteTemplateNameRef extends SoyASTElement implements PsiNamedElement, TemplateMemberElement {

    public AbsoluteTemplateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @Override
    public String getTemplateName() {
        return getName();
    }

    @Override
    public String getNamespace() {
        String name = getName();
        int index = name.indexOf('.');
        return index > 1 ? name.substring(0, index - 1) : null;
    }
}
