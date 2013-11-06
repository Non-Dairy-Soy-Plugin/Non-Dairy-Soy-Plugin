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
import net.venaglia.nondairy.soylang.ModuleRef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:19 PM
 *
 * This cache stores a {@link TemplateCache} object for each namespace.
 */
public class NamespaceCache extends AbstractChangeAwareCache<TemplateCache> implements ModuleRef {

    private static final Key<NamespaceCache> DELEGATE_CACHE_KEY = new Key<NamespaceCache>("non-dairy.namesapce-cache");

    @NonNls
    public static final String DEFAULT_NAMESPACE = "\u001a [ns]";

    @NotNull
    private final Module module;

    public NamespaceCache(@NotNull Module module) {
        this.module = module;
    }

    @NotNull
    @Override
    protected TemplateCache create(String namespace) {
        return new TemplateCache(this, namespace);
    }

    @Override
    @Nullable
    public String getKeyFor(@NotNull TemplateCache value) {
        return value.getNamespace();
    }

    @Override
    @NotNull
    public Module getModule() {
        return module;
    }

    @NotNull
    public static NamespaceCache getCache(@NotNull Module module) {
        NamespaceCache cache = module.getUserData(DELEGATE_CACHE_KEY);
        if (cache == null) {
            cache = new NamespaceCache(module);
            module.putUserData(DELEGATE_CACHE_KEY, cache);
        }
        return cache;
    }

    @Override
    public NamespaceCache clone() {
        NamespaceCache cache = (NamespaceCache)super.clone();
        cache.flatCache = flatCache.clone();
        return cache;
    }

}
