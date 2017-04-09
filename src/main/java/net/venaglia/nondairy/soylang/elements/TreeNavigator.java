/*
 * Copyright 2010 - 2013 Ed Venaglia
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
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The TreeNavigator interface is used to close a gap the test framework.
 */
public abstract class TreeNavigator {

    /**
     * This property may be set to a class name to provide an alternate
     * implementation. If this property is being set programatically, it
     * must be set before the SoyPsiElement class is loaded.
     */
    @NonNls
    public static final String OVERRIDE_TREE_NAVIGATOR_PROPERTY = "net.venaglia.nondairy.tree-navigator";

    /**
     * The default implementation of the TreeNavigator provides the
     * production runtime logic needed within IntelliJ. This logic is not
     * compatible with the unit test environment.
     */
    public static final TreeNavigator INSTANCE;

    static {
        String className = System.getProperty(TreeNavigator.OVERRIDE_TREE_NAVIGATOR_PROPERTY);
        TreeNavigator instance = null;
        if (className != null) {
            try {
                instance = (TreeNavigator)Class.forName(className).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (instance == null) { 
            instance = new DefaultTreeNavigator();
        }
        INSTANCE = instance;
    }

    @Nullable
    public abstract PsiElement getParent(@NotNull ASTNode node);

    @Nullable
    public abstract PsiElement getFirstChild(@NotNull ASTNode node);

    @Nullable
    public abstract PsiElement getLastChild(@NotNull ASTNode node);

    @Nullable
    public abstract PsiElement getNthChild(@NotNull ASTNode node, int n);

    @NotNull
    public abstract PsiElement[] getAllChildren(@NotNull ASTNode node);

    @Nullable
    public abstract PsiElement getNextSibling(@NotNull ASTNode node);

    @Nullable
    public abstract PsiElement getPrevSibling(@NotNull ASTNode node);

    @NotNull
    public abstract PsiManager getPsiManager(@NotNull Project project);

    @NotNull
    public abstract ProjectFileIndex getProjectFileIndex(@NotNull Project project);

    @NotNull
    public abstract Module[] getModules(@NotNull Project project);

    @Nullable
    public abstract Document getDocument(@NotNull VirtualFile file);

    @Nullable
    public abstract VirtualFile getFile(@NotNull String fileUrl);

    private static class DefaultTreeNavigator extends TreeNavigator {

        @Nullable
        @Override
        public PsiElement getParent(@NotNull ASTNode node) {
            return SharedImplUtil.getParent(node);
        }

        @Nullable
        @Override
        public PsiElement getFirstChild(@NotNull ASTNode node) {
            return SharedImplUtil.getFirstChild(node);
        }

        @Nullable
        @Override
        public PsiElement getLastChild(@NotNull ASTNode node) {
            return SharedImplUtil.getLastChild(node);
        }

        @Nullable
        @Override
        public PsiElement getNthChild(@NotNull ASTNode node, int n) {
            return SourceTreeToPsiMap.treeElementToPsi(node.findLeafElementAt(n));
        }

        @NotNull
        @Override
        public PsiElement[] getAllChildren(@NotNull ASTNode node) {
            PsiElement psiChild = getFirstChild(node);
            if (psiChild == null || psiChild == PsiUtilCore.NULL_PSI_ELEMENT) {
                return PsiElement.EMPTY_ARRAY;
            }

            List<PsiElement> result = new ArrayList<PsiElement>();
            while (psiChild != null && psiChild != PsiUtilCore.NULL_PSI_ELEMENT) {
                if (psiChild.getNode() instanceof CompositeElement) {
                    result.add(psiChild);
                }
                psiChild = psiChild.getNextSibling();
            }
            return PsiUtilCore.toPsiElementArray(result);
        }

        @Nullable
        @Override
        public PsiElement getNextSibling(@NotNull ASTNode node) {
            return SharedImplUtil.getNextSibling(node);
        }

        @Nullable
        @Override
        public PsiElement getPrevSibling(@NotNull ASTNode node) {
            return SharedImplUtil.getPrevSibling(node);
        }

        @NotNull
        @Override
        public PsiManager getPsiManager(@NotNull Project project) {
            return PsiManager.getInstance(project);
        }

        @NotNull
        @Override
        public ProjectFileIndex getProjectFileIndex(@NotNull Project project) {
            return ProjectRootManager.getInstance(project).getFileIndex();
        }

        @NotNull
        @Override
        public Module[] getModules(@NotNull Project project) {
            return ModuleManager.getInstance(project).getModules();
        }

        @Nullable
        @Override
        public Document getDocument(@NotNull VirtualFile file) {
            return FileDocumentManager.getInstance().getDocument(file);
        }

        @Nullable
        @Override
        public VirtualFile getFile(@NotNull String fileUrl) {
            return VirtualFileManager.getInstance().findFileByUrl(fileUrl);
        }
    }
}
