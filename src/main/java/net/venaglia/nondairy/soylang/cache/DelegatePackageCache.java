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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:21 PM
 * 
 * Top-level cache object, associated with an IntelliJ module. This cache
 * stores a {@link NamespaceCache} object for each unique delpackage found in
 * the module.
 */
public class DelegatePackageCache extends AbstractChangeAwareCache<DelegateTemplateCache> {

    @NonNls
    public static final String DEFAULT_DELEGATE = "\u001a [del]";

    private static final Key<DelegatePackageCache> DELEGATE_CACHE_KEY = new Key<DelegatePackageCache>("non-dairy.delegate-cache");

    private final Module module;

    public DelegatePackageCache(@NotNull Module module) {
        this.module = module;
    }

    @NotNull
    @Override
    protected DelegateTemplateCache create(String delegate) {
        return new DelegateTemplateCache(this, delegate);
    }

    @NotNull
    public Module getModule() {
        return module;
    }

    @Override
    @Nullable
    public String getKeyFor(@NotNull DelegateTemplateCache value) {
        return value.getDelegatePackage();
    }

    @NotNull
    public static DelegatePackageCache getCache(@NotNull Module module) {
        DelegatePackageCache cache = module.getUserData(DELEGATE_CACHE_KEY);
        if (cache == null) {
            cache = new DelegatePackageCache(module);
            module.putUserData(DELEGATE_CACHE_KEY, cache);
        }
        return cache;
    }
}
