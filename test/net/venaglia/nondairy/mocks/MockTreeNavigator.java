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

package net.venaglia.nondairy.mocks;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import net.venaglia.nondairy.soylang.elements.TreeNavigator;
import net.venaglia.nondairy.util.SourceTuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.junit.Assert.*;

/**
 * TreeNavigator implementation that is compatible with the unit test
 * framework. Using this TreeNavigator in conjunction with MockTreeNode,
 * results in fewer dependencies in unit tests that navigate psi trees.
 */
public class MockTreeNavigator extends TreeNavigator {

    @Nullable
    @Override
    public PsiElement getParent(@NotNull ASTNode node) {
        ASTNode child = node.getTreeParent();
        return child == null ? null : child.getPsi();
    }

    @Nullable
    @Override
    public PsiElement getFirstChild(@NotNull ASTNode node) {
        ASTNode child = node.getFirstChildNode();
        return child == null ? null : child.getPsi();
    }

    @Nullable
    @Override
    public PsiElement getLastChild(@NotNull ASTNode node) {
        ASTNode child = node.getLastChildNode();
        return child == null ? null : child.getPsi();
    }

    @Nullable
    @Override
    public PsiElement getNthChild(@NotNull ASTNode node, int n) {
        ASTNode child = node.findLeafElementAt(n);
        return child == null ? null : child.getPsi();
    }

    @NotNull
    @Override
    public PsiElement[] getAllChildren(@NotNull ASTNode node) {
        ASTNode[] childNodes = node.getChildren(null);
        if (childNodes.length == 0) {
            return PsiElement.EMPTY_ARRAY;
        }
        PsiElement[] children = new PsiElement[childNodes.length];
        for (int i = 0; i < childNodes.length; i++) {
            children[i] = childNodes[i].getPsi();
        }
        return children;
    }

    @Nullable
    @Override
    public PsiElement getNextSibling(@NotNull ASTNode node) {
        ASTNode sibling = node.getTreeNext();
        return sibling == null ? null : sibling.getPsi();
    }

    @Nullable
    @Override
    public PsiElement getPrevSibling(@NotNull ASTNode node) {
        ASTNode sibling = node.getTreePrev();
        return sibling == null ? null : sibling.getPsi();
    }

    @NotNull
    @Override
    public PsiManager getPsiManager(@NotNull Project project) {
        return MockProjectEnvironment.getUnitTestPsiManager();
    }

    @NotNull
    @Override
    public ProjectFileIndex getProjectFileIndex(@NotNull Project project) {
        return MockProjectEnvironment.getUnitTestProjectFileIndex();
    }

    @NotNull
    @Override
    public Module[] getModules(@NotNull Project project) {
        if (MockProjectEnvironment.getUnitTestProject() == project) {
            return new Module[]{MockProjectEnvironment.getUnitTestModule()};
        }
        return new Module[0];
    }

    @Nullable
    @Override
    public Document getDocument(@NotNull VirtualFile file) {
        SourceTuple tuple = MockProjectEnvironment.getTuple(file);
        assertNotNull(tuple);
        return tuple.document;
    }

    @Nullable
    @Override
    public VirtualFile getFile(@NotNull String fileUrl) {
        return MockProjectEnvironment.findVirtualFile(fileUrl);
    }
}
