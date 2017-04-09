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

import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * User: ed
 * Date: 5/21/12
 * Time: 6:03 PM
 */
public abstract class AbstractChangeAwareCache<V> extends AbstractCache<String,V> {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    protected FlatCache flatCache = new FlatCache();

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

    public ConcurrentNavigableMap<String,Collection<CacheEntry>> getFlatCache() {
        return flatCache.unmodifiable();
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
