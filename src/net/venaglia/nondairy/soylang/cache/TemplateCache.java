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

import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:17 PM
 *
 * This cache stores a Collection of {@link CacheEntry} objects for each
 * template name. The {@link TinySet} inner class is optimized for single entries.
 */
public class TemplateCache extends AbstractCache<String,Set<CacheEntry>> {

    private static final Key<TemplateCache> TEMPLATE_CACHE_KEY =
            new Key<TemplateCache>("non-dairy.template-cache");

    private final NamespaceCache parent;
    private final String delegate;
    private final String namespace;
    private final Set<VirtualFile> files = new  TinySet<VirtualFile>();

    public TemplateCache(NamespaceCache parent, String delegate, String namespace) {
        this.parent = parent;
        this.delegate = delegate;
        this.namespace = namespace;
    }

    public String getDelegate() {
        return delegate;
    }

    public String getNamespace() {
        return namespace;
    }

    @NotNull
    @Override
    protected Set<CacheEntry> create(String key) {
        return new CacheSet(key);
    }

    void addFile(@NotNull VirtualFile file) {
        if (!files.contains(file)) {
            TemplateCache old = file.getUserData(TEMPLATE_CACHE_KEY);
            if (old != null) {
                old.files.remove(file);
            }
            file.replace(TEMPLATE_CACHE_KEY, old, this);
            files.add(file);
        }
    }

    void removeFile(@NotNull VirtualFile file) {
        if (files.contains(file)) {
            file.replace(TEMPLATE_CACHE_KEY, this, null);
            files.remove(file);
            if (files.isEmpty()) {
                parent.removeChild(this);
            } else {
                removeImpl(file);
            }
        }
    }

    private Set<CacheEntry> removeImpl(VirtualFile file) {
        Set<CacheEntry> removed = new HashSet<CacheEntry>();
        for (Iterator<Set<CacheEntry>> cesi = values().iterator(); cesi.hasNext(); ) {
            Set<CacheEntry> cec = cesi.next();
            for (Iterator<CacheEntry> cei = cec.iterator(); cei.hasNext(); ) {
                CacheEntry ce = cei.next();
                if (file.equals(ce.getFile())) {
                    cei.remove();
                    removed.add(ce);
                }
            }
            if (cec.isEmpty()) {
                cesi.remove();
            }
        }
        if (!removed.isEmpty()) {
            parent.removed(removed.iterator());
        }
        return removed;
    }

    @Override
    public boolean removeChild(Set<CacheEntry> value) {
        boolean b = super.removeChild(value);
        if (b && isEmpty()) {
            parent.removeChild(this);
        }
        return b;
    }

    @Override
    public String getKeyFor(@NotNull Set<CacheEntry> value) {
        if (value instanceof CacheSet) {
            return ((CacheSet)value).getTemplateName();
        }
        return null;
    }

    @NotNull
    public Set<VirtualFile> getFiles() {
        return Collections.unmodifiableSet(files);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return String.format("TemplateCache{namespace=\"%s\",templates=%d}", namespace, size());
    }

    @Nullable
    public static TemplateCache fromFile(VirtualFile file) {
        return file.getUserData(TEMPLATE_CACHE_KEY);
    }

    @NotNull
    @Override
    protected Set<CacheEntry> unmodifiableValue(@NotNull Set<CacheEntry> value) {
        return Collections.unmodifiableSet(value);
    }

    @NotNull
    @Override
    protected Set<CacheEntry> cloneValue(@NotNull Set<CacheEntry> value) {
        return ((CacheSet)value).clone();
    }

    public static class CacheSet extends TinySet<CacheEntry> {

        private final String templateName;

        public CacheSet(String templateName) {
            this.templateName = templateName;
        }

        public String getTemplateName() {
            return templateName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheSet)) return false;

            CacheSet cacheSet = (CacheSet)o;

            return !(templateName != null
                    ? !templateName.equals(cacheSet.templateName)
                    : cacheSet.templateName != null);
        }

        @Override
        public int hashCode() {
            return templateName != null ? templateName.hashCode() : 0;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append('[');
            boolean first = true;
            for (CacheEntry cacheEntry : this) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(',');
                }
                buffer.append(cacheEntry.getTemplate());
            }
            buffer.append(']');
            return buffer.toString();
        }
    }
}
