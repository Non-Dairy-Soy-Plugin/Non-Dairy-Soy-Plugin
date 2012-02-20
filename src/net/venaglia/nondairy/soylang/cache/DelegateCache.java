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

package net.venaglia.nondairy.soylang.cache;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:21 PM
 * 
 * Top-level cache object, associated with an IntelliJ module. This cache
 * stores a {@link NamespaceCache} object for each unique delpackage found in
 * the module.
 */
public class DelegateCache extends AbstractCache<String,NamespaceCache> {

    @NonNls
    public static final String DEFAULT_DELEGATE = "\u001a [del]";

    private static final Key<DelegateCache> DELEGATE_CACHE_KEY = new Key<DelegateCache>("non-dairy.delegate-cache");

    private final Module module;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private FlatCache flatCache = new FlatCache();

    public DelegateCache(@NotNull Module module) {
        this.module = module;
    }

    @NotNull
    @Override
    protected NamespaceCache create(String delegate) {
        return new NamespaceCache(this, delegate);
    }

    @NotNull
    public Set<TemplateCache> getTemplateCaches(String delegate, String namespace) {
        Set<TemplateCache> caches = new HashSet<TemplateCache>();
        NamespaceCache namespaceCache = get(delegate);
        if (namespaceCache != null) {
            caches.add(namespaceCache.get(namespace));
        }
        return caches;
    }

    @NotNull
    public Set<TemplateCache> getTemplateCaches(String namespace) {
        Set<TemplateCache> caches = new HashSet<TemplateCache>();
        for (NamespaceCache namespaceCache : values()) {
            TemplateCache templateCache = namespaceCache.get(namespace);
            if (templateCache != null) {
                caches.add(templateCache);
            }
        }
        return caches;
    }

    void added(Iterator<CacheEntry> iterator) {
        while (iterator.hasNext()) {
            CacheEntry ce = iterator.next();
            flatCache.getOrCreate(ce.getTemplate()).add(ce);
        }
    }

    void removed(Iterator<CacheEntry> iterator) {
        while (iterator.hasNext()) {
            CacheEntry ce = iterator.next();
            Collection<CacheEntry> cacheEntries = flatCache.get(ce.getTemplate());
            cacheEntries.remove(ce);
        }
    }

    @NotNull
    public Module getModule() {
        return module;
    }

    @Override
    @Nullable
    public String getKeyFor(@NotNull NamespaceCache value) {
        return value.getDelegate();
    }

    public ConcurrentNavigableMap<String,Collection<CacheEntry>> getFlatCache() {
        return flatCache.unmodifiable();
    }

    @Override
    public DelegateCache clone() {
        DelegateCache cache = (DelegateCache)super.clone();
        cache.flatCache = flatCache.clone();
        return cache;
    }

    @NotNull
    public static DelegateCache getDelegateCache(@NotNull Module module) {
        DelegateCache cache = module.getUserData(DELEGATE_CACHE_KEY);
        if (cache == null) {
            cache = new DelegateCache(module);
            module.putUserData(DELEGATE_CACHE_KEY, cache);
        }
        return cache;
    }

    /**
     * This cache object stores all active cache entries by their short template name.
     */
    static class FlatCache extends AbstractCache<String,Collection<CacheEntry>> {

        @NotNull
        @Override
        protected Collection<CacheEntry> create(String key) {
            return new TinySet<CacheEntry>();
        }

        @NotNull
        @Override
        protected Collection<CacheEntry> cloneValue(@NotNull Collection<CacheEntry> value) {
            return ((TinySet<CacheEntry>)value).clone();
        }

        @Override
        public FlatCache clone() {
            return (FlatCache)super.clone();
        }
    }
}
