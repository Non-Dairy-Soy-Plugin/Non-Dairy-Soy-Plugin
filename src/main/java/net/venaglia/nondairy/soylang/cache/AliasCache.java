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

package net.venaglia.nondairy.soylang.cache;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import net.venaglia.nondairy.soylang.ModuleRef;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: ed
 * Date: 10/18/13
 * Time: 8:41 AM
 *
 * The cache is used to find which files reference a particular namespace using
 * the alias command. The key is the namespace name. The value is a collection
 * of files.
 */
public class AliasCache extends AbstractCache<String,AliasCacheEntry> implements ModuleRef {

    private static final Key<AliasCache> ALIAS_CACHE_KEY = new Key<AliasCache>("non-dairy.alias-cache");
    private static final Key<Set<String>> ALIAS_CACHE_REF_KEY = new Key<Set<String>>("non-dairy.alias-cache-ref");

    @NotNull
    private final Module module;

    public AliasCache(@NotNull Module module) {
        this.module = module;
    }

    @NotNull
    @Override
    protected AliasCacheEntry create(String key) {
        return new AliasCacheEntry(this, key);
    }

    @Nullable
    @Override
    public String getKeyFor(@NotNull AliasCacheEntry value) {
        return value.getNamespace();
    }

    @NotNull
    @Override
    public Module getModule() {
        return module;
    }

    void addRef (AliasCacheEntry entry, VirtualFile file) {
        Set<String> refs = file.getUserData(ALIAS_CACHE_REF_KEY);
        if (refs == null) {
            refs = new TinySet<String>();
            file.putUserData(ALIAS_CACHE_REF_KEY, refs);
        }
        refs.add(getKeyFor(entry));
    }

    void removeRef (AliasCacheEntry entry, VirtualFile file) {
        Set<String> refs = file.getUserData(ALIAS_CACHE_REF_KEY);
        if (refs != null) {
            refs.remove(getKeyFor(entry));
        }
    }

    public void removeAllReferencingAliasCaches(VirtualFile file) {
        Set<String> refs = file.getUserData(ALIAS_CACHE_REF_KEY);
        if (refs != null) {
            for (String ref : new HashSet<String>(refs)) {
                AliasCacheEntry aliasCacheEntry = get(ref);
                if (aliasCacheEntry != null) {
                    aliasCacheEntry.remove(file);
                }
            }
        }
    }

    public Collection<AliasCacheEntry> getReferencingAliasCaches(VirtualFile file) {
        Set<String> refs = file.getUserData(ALIAS_CACHE_REF_KEY);
        if (refs == null) {
            refs = new TinySet<String>();
            file.putUserData(ALIAS_CACHE_REF_KEY, refs);
        }
        Set<AliasCacheEntry> aliasCacheEntries = new HashSet<AliasCacheEntry>(refs.size());
        for (String ref : refs) {
            AliasCacheEntry entry = get(ref);
            if (entry != null) {
                aliasCacheEntries.add(entry);
            }
        }
        return aliasCacheEntries;
    }

    @Override
    public AliasCache clone() {
        AliasCache clone = (AliasCache)super.clone();
        clone.clear();
        for (AliasCacheEntry entry : this.values()) {
            entry = entry.clone(this);
            clone.put(getKeyFor(entry), entry);
        }
        return clone;
    }

    @NotNull
    public static AliasCache getCache(@NotNull Module module) {
        AliasCache cache = module.getUserData(ALIAS_CACHE_KEY);
        if (cache == null) {
            cache = new AliasCache(module);
            module.putUserData(ALIAS_CACHE_KEY, cache);
        }
        return cache;
    }
}
