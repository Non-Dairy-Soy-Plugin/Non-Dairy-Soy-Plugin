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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import net.venaglia.nondairy.soylang.SoyFileType;
import net.venaglia.nondairy.soylang.cache.DelegateCache;
import net.venaglia.nondairy.soylang.cache.TemplateCache;
import net.venaglia.nondairy.soylang.elements.TreeNavigator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: ed
 * Date: 1/26/12
 * Time: 8:31 AM
 *
 * This predicate will find all file elements in the current project, for a
 * particular language.
 * 
 * This class makes use of the template cache maintained by classes in
 * {@link net.venaglia.nondairy.soylang.cache}
 */
public class SoyFileElementTraversalPredicate implements TraversalPredicate {

    private final String delpackage;
    private final String namespace;

    public SoyFileElementTraversalPredicate(@NotNull String namespace) {
        this.delpackage = null;
        this.namespace = namespace;
    }

    public SoyFileElementTraversalPredicate(@NotNull String delpackage, @NotNull String namespace) {
        this.delpackage = delpackage;
        this.namespace = namespace;
    }

    @NotNull
    @Override
    public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
        Set<Module> search = new HashSet<Module>();
        PsiElementCollection files = new PsiElementCollection();
        for (PsiElement element : current) {
            getModulesToSearch(element, search);
            PsiFile containingFile = element.getContainingFile();
            if (containingFile != null) {
                files.add(containingFile);
            }
        }
        for (Module module : search) {
            PsiManager manager = TreeNavigator.INSTANCE.getPsiManager(module.getProject());
            for (VirtualFile file : findFiles(module)) {
                PsiFile psiFile = manager.findFile(file);
                if (psiFile != null) {
                    files.add(psiFile);
                }
            }
        }
        return files;
    }

    private void getModulesToSearch(PsiElement element, Set<Module> modules) {
        ProjectFileIndex fileIndex = TreeNavigator.INSTANCE.getProjectFileIndex(element.getProject());
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile != null) {
            Module module = fileIndex.getModuleForFile(virtualFile);
            if (module != null && !modules.contains(module)) {
                modules.add(module);
                collectModuleDependencies(module, modules);
            }
        }
    }
    
    private void collectModuleDependencies (Module start, Set<Module> modules) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(start);
        if (moduleRootManager != null) {
            for (Module module : moduleRootManager.getDependencies()) {
                if (!modules.contains(module)) {
                    modules.add(module);
                    collectModuleDependencies(module, modules);
                }
            }
        }
    }
    
    private Collection<VirtualFile> findFiles(Module module) {
        Collection<VirtualFile> files = new HashSet<VirtualFile>();
        for (TemplateCache cache : getTemplateCaches(module)) {
            files.addAll(cache.getFiles());
        }
        return files;
    }

    private Set<TemplateCache> getTemplateCaches(Module module) {
        DelegateCache delegateCache = DelegateCache.getDelegateCache(module);
        if (delpackage == null) {
            return delegateCache.getTemplateCaches(namespace);
        } else {
            return delegateCache.getTemplateCaches(delpackage, namespace);
        }
    }

    @Override
    public boolean traverseAgainIfNoMatch() {
        return false;
    }

    @Override
    public boolean test(PsiElement element) {
        return true; // filtering is handled by the index
    }

    @Override
    public String toString() {
        return "*." + SoyFileType.INSTANCE.getDefaultExtension();
    }
}
