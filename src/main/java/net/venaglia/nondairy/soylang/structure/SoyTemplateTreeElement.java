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

package net.venaglia.nondairy.soylang.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameDef;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * User: ed
 * Date: Aug 27, 2010
 * Time: 7:24:10 PM
 *
 * Structure tree element for template definitions.
 */
public class SoyTemplateTreeElement extends PsiTreeElementBase<LocalTemplateNameDef> {

    public SoyTemplateTreeElement(@NotNull LocalTemplateNameDef psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return Collections.emptySet();
    }

    @Override
    public String getPresentableText() {
        LocalTemplateNameDef templateNameDef = getElement();
        return templateNameDef == null ? "???" : templateNameDef.getText();
    }
}
