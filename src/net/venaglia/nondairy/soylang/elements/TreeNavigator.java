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
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.SharedImplUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NonNls;

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

    public abstract PsiElement getParent(ASTNode node);

    public abstract PsiElement getFirstChild(ASTNode node);

    public abstract PsiElement getLastChild(ASTNode node);

    public abstract PsiElement getNthChild(ASTNode node, int n);

    public abstract PsiElement[] getAllChildren(ASTNode node);

    public abstract PsiElement getNextSibling(ASTNode node);

    public abstract PsiElement getPrevSibling(ASTNode node);

    public abstract PsiManager getPsiManager(Project project);

    public abstract ProjectFileIndex getProjectFileIndex(Project project);

    public abstract Module[] getModules(Project project);

    public abstract Document getDocument(VirtualFile file);
    
    private static class DefaultTreeNavigator extends TreeNavigator {

        @Override
        public PsiElement getParent(ASTNode node) {
            return SharedImplUtil.getParent(node);
        }

        @Override
        public PsiElement getFirstChild(ASTNode node) {
            return SharedImplUtil.getFirstChild(node);
        }

        @Override
        public PsiElement getLastChild(ASTNode node) {
            return SharedImplUtil.getLastChild(node);
        }

        @Override
        public PsiElement getNthChild(ASTNode node, int n) {
            return SourceTreeToPsiMap.treeElementToPsi(node.findLeafElementAt(n));
        }

        @Override
        public PsiElement[] getAllChildren(ASTNode node) {
            PsiElement psiChild = getFirstChild(node);
            if (psiChild == null) return PsiElement.EMPTY_ARRAY;

            List<PsiElement> result = new ArrayList<PsiElement>();
            while (psiChild != null) {
                if (psiChild.getNode() instanceof CompositeElement) {
                    result.add(psiChild);
                }
                psiChild = psiChild.getNextSibling();
            }
            return PsiUtilCore.toPsiElementArray(result);
        }

        @Override
        public PsiElement getNextSibling(ASTNode node) {
            return SharedImplUtil.getNextSibling(node);
        }

        @Override
        public PsiElement getPrevSibling(ASTNode node) {
            return SharedImplUtil.getPrevSibling(node);
        }

        @Override
        public PsiManager getPsiManager(Project project) {
            return PsiManager.getInstance(project);
        }

        @Override
        public ProjectFileIndex getProjectFileIndex(Project project) {
            return ProjectRootManager.getInstance(project).getFileIndex();
        }

        @Override
        public Module[] getModules(Project project) {
            return ModuleManager.getInstance(project).getModules();
        }

        @Override
        public Document getDocument(VirtualFile file) {
            return FileDocumentManager.getInstance().getDocument(file);
        }
    }
}
