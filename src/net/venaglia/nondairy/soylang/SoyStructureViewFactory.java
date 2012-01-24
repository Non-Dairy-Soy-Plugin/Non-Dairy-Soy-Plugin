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

package net.venaglia.nondairy.soylang;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.psi.PsiFile;
import net.venaglia.nondairy.soylang.structure.SoyRootTreeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 27, 2010
 * Time: 7:12:33 PM
 */
public class SoyStructureViewFactory implements PsiStructureViewFactory {

    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        ASTNode node = psiFile.getNode();
        if (node == null || node.getElementType() != SoyFileType.FILE || !(psiFile instanceof SoyFile)) {
            return null;
        }
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel() {
                return new TextEditorBasedStructureViewModel(psiFile) {
                    @NotNull
                    @Override
                    public StructureViewTreeElement getRoot() {
                        return new SoyRootTreeElement((SoyFile)psiFile);
                    }
                };
            }
        };
    }
}
