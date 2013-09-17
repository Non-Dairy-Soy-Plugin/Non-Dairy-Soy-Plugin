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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User: ed
 * Date: 5/21/12
 * Time: 6:06 PM
 */
public abstract class AbstractTemplateCache<ME extends AbstractTemplateCache<ME>> extends AbstractCache<String,Set<CacheEntry>> {

    private final Set<VirtualFile> files = new TinySet<VirtualFile>();

    protected final AbstractChangeAwareCache<ME> parent;
    protected final String place;

    public AbstractTemplateCache(AbstractChangeAwareCache<ME> parent, String place) {
        this.parent = parent;
        this.place = place;
    }

    public String getPlace() {
        return place;
    }

    @NotNull
    @Override
    protected Set<CacheEntry> create(String key) {
        return new CacheSet(key);
    }

    @SuppressWarnings("unchecked")
    private ME self() {
        return (ME)this;
    }

    abstract Key<ME> getCacheKey();

    void addFile(@NotNull VirtualFile file) {
        if (!files.contains(file)) {
            ME old = file.getUserData(getCacheKey());
            if (old != null) {
                old.removeFile(file);
            }
            file.replace(getCacheKey(), old, self());
            files.add(file);
        }
    }

    void removeFile(@NotNull VirtualFile file) {
        if (files.contains(file)) {
            file.replace(getCacheKey(), self(), null);
            files.remove(file);
            if (files.isEmpty()) {
                parent.removeChild(self());
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
            parent.removeChild(self());
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
        return String.format("%s{namespace=\"%s\",templates=%d}", getClass().getSimpleName(), place, size());
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
