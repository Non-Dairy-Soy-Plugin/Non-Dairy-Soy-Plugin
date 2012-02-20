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

package net.venaglia.nondairy.soylang;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import net.venaglia.nondairy.soylang.cache.CacheEntry;
import net.venaglia.nondairy.soylang.cache.DelegateCache;
import net.venaglia.nondairy.soylang.cache.NamespaceCache;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.NamePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * User: ed
 * Date: 2/14/12
 * Time: 6:49 PM
 *
 * Class to expose support for IntelliJ's "go to symbol" feature to quickly
 * locate closure template definitions.
 */
public class SoyGoToSymbolProvider extends GoToSymbolProvider {

    private static final PsiElementPath PATH_TO_TEMPLATES =
            new PsiElementPath(new ElementTypePredicate(soy_file).onChildren(),
                               new ElementTypePredicate(template_tag_pair).onDescendants(1, 2),
                               new ElementTypePredicate(template_tag).onChildren(),
                               new ElementTypePredicate(tag_between_braces).onChildren(),
                               new ElementTypePredicate(template_name).onChildren()).debug("path_to_templates!goto-symbol");

    private static final Key<ConcurrentNavigableMap<String,Collection<CacheEntry>>> KEY_TO_FLAT_CACHE_ENTRIES =
            new Key<ConcurrentNavigableMap<String,Collection<CacheEntry>>>("non-dairy.flat-cache-entries");

    @Override
    protected void addNames(@NotNull Module module, final Set<String> result) {
        result.addAll(getFlatCacheNames(module).keySet());
    }

    private ConcurrentNavigableMap<String,Collection<CacheEntry>> getFlatCacheNames(Module module) {
        Project project = module.getProject();
        ConcurrentNavigableMap<String,Collection<CacheEntry>> cacheEntries =
                project.getUserData(KEY_TO_FLAT_CACHE_ENTRIES);
        if (cacheEntries == null) {
            DelegateCache delegateCache = DelegateCache.getDelegateCache(module);
            cacheEntries = delegateCache.getFlatCache();
            project.putUserData(KEY_TO_FLAT_CACHE_ENTRIES, cacheEntries);
        }
        return cacheEntries;
    }

    @Override
    protected void addItems(@NotNull Module module, String name, List<NavigationItem> results) {
        PsiManager psiManager = PsiManager.getInstance(module.getProject());
        Collection<CacheEntry> cacheEntries = getFlatCacheNames(module).get(name);
        if (cacheEntries != null) {
            for (CacheEntry cacheEntry : cacheEntries) {
                PsiElement psiElement = getPsiElement(psiManager, cacheEntry);
                if (psiElement != null) {
                    Icon icon = cacheEntry.isDeltemplate() ? SoyIcons.DELTEMPLATE : SoyIcons.TEMPLATE;
                    String fullName = NamespaceCache.DEFAULT_NAMESPACE.equals(cacheEntry.getNamespace())
                                      ? cacheEntry.getTemplate()
                                      : cacheEntry.getNamespace() + "." + cacheEntry.getTemplate();
                    results.add(new BaseNavigationItem(psiElement, fullName, icon));
                }
            }
        }
    }

    @Nullable
    private PsiElement getPsiElement(PsiManager psiManager, CacheEntry cacheEntry) {
        PsiFile psiFile = psiManager.findFile(cacheEntry.getFile());
        if (psiFile != null) {
            PsiElementPath path = PATH_TO_TEMPLATES.append(new NamePredicate(cacheEntry.getTemplate()));
            return path.navigate(psiFile).oneOrNull();
        }
        return null;
    }

    @Override
    protected boolean acceptModule(Module module) {
        return true;
    }
}
