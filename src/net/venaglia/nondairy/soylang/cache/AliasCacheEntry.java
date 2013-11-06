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

import com.intellij.openapi.vfs.VirtualFile;
import net.venaglia.nondairy.soylang.NamespaceRef;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * User: ed
 * Date: 10/18/13
 * Time: 8:54 AM
 *
 * The object contains a collection of files that reference a particular
 * namespace through an alias command.
 */
public class AliasCacheEntry extends TinySet<VirtualFile> {

    @NotNull
    private final AliasCache aliasCache;

    @NonNls
    @NotNull
    private final String namespace;

    public AliasCacheEntry(@NotNull AliasCache aliasCache, @NotNull String namespace) {
        this.aliasCache = aliasCache;
        this.namespace = namespace;
    }

    private AliasCacheEntry(@NotNull AliasCacheEntry that, @NotNull AliasCache parent) {
        super.addAll(that);
        this.aliasCache = parent;
        this.namespace = that.namespace;
    }

    @Override
    public boolean add(VirtualFile file) {
        if (super.add(file)) {
            aliasCache.addRef(this, file);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object file) {
        if (file instanceof VirtualFile && super.remove(file)) {
            aliasCache.removeRef(this, (VirtualFile)file);
            return true;
        }
        return false;
    }

    @NonNls
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    public AliasCacheEntry clone(AliasCache parent) {
        return new AliasCacheEntry(this, parent);
    }

    NavigableMap<String,VirtualFile> getDebugMap() {
        NavigableMap<String,VirtualFile> debugMap = new DebugMap(namespace);
        for (VirtualFile virtualFile : this) {
            debugMap.put(virtualFile.getCanonicalPath(), virtualFile);
        }
        return debugMap;
    }

    private static class DebugMap extends TreeMap<String,VirtualFile> implements NamespaceRef {

        private final String namespace;

        private DebugMap(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public String getNamespace() {
            return namespace;
        }
    }
}
