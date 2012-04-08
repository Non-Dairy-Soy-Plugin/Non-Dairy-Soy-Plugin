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
import net.venaglia.nondairy.soylang.elements.NamespaceMemberElement;
import net.venaglia.nondairy.soylang.elements.TreeNavigator;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
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
@NoMatchHanding(onStart = TraverseEmpty.CONTINUE)
public class SoyFileElementTraversalPredicate implements TraversalPredicate {

    private SoyFileElementTraversalPredicate() {}

    @NotNull
    @Override
    public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
        Set<Module> search = new HashSet<Module>();
        PsiElementCollection files = new PsiElementCollection();
        Set<NamespaceAndPackage> naps = new TinySet<NamespaceAndPackage>();
        for (PsiElement element : current) {
            if (element instanceof NamespaceMemberElement) {
                NamespaceMemberElement nme = (NamespaceMemberElement)element;
                String namespace = nme.getNamespace();
                if (namespace != null) {
                    naps.add(new NamespaceAndPackage(nme.getDelegatePackage(), namespace));
                    getModulesToSearch(element, search);
                    PsiFile containingFile = element.getContainingFile();
                    if (containingFile != null) {
                        files.add(containingFile);
                    }
                }
            }
        }
        traverseImpl(search, files, naps);
        return files;
    }

    void traverseImpl(@NotNull Set<Module> search,
                      @NotNull PsiElementCollection buffer,
                      @NotNull Collection<NamespaceAndPackage> naps) {
        for (Module module : search) {
            PsiManager manager = TreeNavigator.INSTANCE.getPsiManager(module.getProject());
            for (VirtualFile file : findFiles(module, naps)) {
                PsiFile psiFile = manager.findFile(file);
                if (psiFile != null) {
                    buffer.add(psiFile);
                }
            }
        }
    }

    void getModulesToSearch(@NotNull PsiElement element, @NotNull Set<Module> modules) {
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
    
    private void collectModuleDependencies (@NotNull Module start, @NotNull Set<Module> modules) {
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
    
    private Collection<VirtualFile> findFiles(@NotNull Module module,
                                              @NotNull Collection<NamespaceAndPackage> naps) {
        Collection<VirtualFile> files = new HashSet<VirtualFile>();
        for (TemplateCache cache : getTemplateCaches(module, naps)) {
            files.addAll(cache.getFiles());
        }
        return files;
    }

    private Set<TemplateCache> getTemplateCaches(@NotNull Module module,
                                                 @NotNull Collection<NamespaceAndPackage> naps) {
        DelegateCache delegateCache = DelegateCache.getDelegateCache(module);
        Set<TemplateCache> caches = new TinySet<TemplateCache>();
        for (NamespaceAndPackage nap : naps) {
            if (nap.delpackage != null) {
                caches.addAll(delegateCache.getTemplateCaches(nap.delpackage, nap.namespace));
            }
            caches.addAll(delegateCache.getTemplateCaches(nap.namespace));
        }
        return caches;
    }

    @Override
    public boolean test(PsiElement element) {
        return true; // filtering is handled by the index
    }

    @Override
    public String toString() {
        return "*." + SoyFileType.INSTANCE.getDefaultExtension();
    }

    private static class NamespaceAndPackage {
        @Nullable @NonNls final String delpackage;
        @NotNull @NonNls final String namespace;

        private NamespaceAndPackage(@Nullable @NonNls String delpackage,
                                    @NotNull @NonNls String namespace) {
            this.delpackage = delpackage;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NamespaceAndPackage that = (NamespaceAndPackage)o;

            if (delpackage != null ? !delpackage.equals(that.delpackage) : that.delpackage != null) return false;
            if (!namespace.equals(that.namespace)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = delpackage != null ? delpackage.hashCode() : 0;
            result = 31 * result + namespace.hashCode();
            return result;
        }

        @Override
        public String toString() {
            if (delpackage != null) {
                return delpackage + "::" + namespace;
            }
            return namespace;
        }
    }

    public static TraversalPredicate filesForNamespace(@NotNull @NonNls String namespace) {
        return filesForNamespace(namespace, null);
    }
    
    public static TraversalPredicate filesForNamespace(@NotNull @NonNls final String namespace,
                                                       @Nullable @NonNls final String delpackage) {
        return new SoyFileElementTraversalPredicate() {
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
                Set<NamespaceAndPackage> naps =
                        Collections.singleton(new NamespaceAndPackage(delpackage, namespace));
                traverseImpl(search, files, naps);
                return files;
            }
        };
    }

    public static TraversalPredicate filesStartingOnNamespaceElement() {
        return new SoyFileElementTraversalPredicate();
    }
}
