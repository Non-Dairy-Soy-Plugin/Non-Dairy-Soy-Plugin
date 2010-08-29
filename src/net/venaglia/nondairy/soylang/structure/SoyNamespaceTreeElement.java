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

package net.venaglia.nondairy.soylang.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.NamespaceDefElement;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameDef;
import net.venaglia.nondairy.soylang.elements.path.PsiElementMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 27, 2010
 * Time: 9:33:10 PM
 */
public class SoyNamespaceTreeElement extends PsiTreeElementBase<NamespaceDefElement> {

    private final SoyFile file;

    public SoyNamespaceTreeElement(SoyFile file) {
        super(file.getNamespaceElement());
        this.file = file;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        return file.getTemplateElements().map(new PsiElementMapper<StructureViewTreeElement>() {
            @Override
            public StructureViewTreeElement map(PsiElement element) {
                return new SoyTemplateTreeElement((LocalTemplateNameDef)element);
            }
        });
    }

    @Override
    public String getPresentableText() {
        NamespaceDefElement element = getElement();
        return element == null ? null : element.getText();
    }
}
