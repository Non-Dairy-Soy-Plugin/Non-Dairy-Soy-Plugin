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

import net.venaglia.nondairy.util.immutable.ImmutableConcurrentNavigableMap;
import net.venaglia.nondairy.util.immutable.Immuter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * User: ed
 * Date: 2/3/12
 * Time: 8:03 PM
 *
 * Standardized base class for cache objects. This class provides full
 * navigation ability, concurrency support, auto-create for missing values, key
 * lookup, visitor support, and a light-weight read-only view.
 */
public abstract class AbstractCache<K,V> extends ConcurrentSkipListMap<K,V> {

    private WeakReference<ConcurrentNavigableMap<K,V>> readonly =
            new WeakReference<ConcurrentNavigableMap<K,V>>(null);

    /**
     * If this cache contains the specified key, its value is returned. 
     * Otherwise a new value is created and inserted for this key, then 
     * returned.
     * @param key The key in this cache object.
     * @return The existing value for the specified key, or a new value if one 
     *     did not already exist.
     * @throws UnsupportedOperationException if this implementation does not 
     *     support automatic value creation.
     */
    @NotNull
    public V getOrCreate(K key) {
        V value = get(key);
        if (value == null) {
            value = create(key);
            put(key, value);
        }
        return value;
    }

    /**
     * Factory method to build values. Subclasses should override this method
     * to create new values automatically in {@link #getOrCreate(Object)}.
     *
     * Implementing clases should <strong>not</strong> call
     * {@link #put(Object, Object)} to add the newly created value.
     * @param key The key of the new value.
     * @return A new value for the speified key.
     * @throws UnsupportedOperationException in the default implementation.
     */
    @NotNull
    protected  V create(K key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the entry associate with the passed value. If more than one such
     * entry contains an equivalent value, only one will be removed.
     * @param value The value to remove from this cache.
     * @return true if this cache was updated, false otherwise.
     */
    public boolean removeChild(V value) {
        K key = getKeyFor(value);
        if (key != null) {
            remove(key);
            return true;
        }
        return false;
    }

    /**
     * Searches this cache for the specified value, and returns its key. If
     * this cache contains multiple equivalent values, the key returned may
     * belong to any of these.
     *
     * The default implementation performs an inefficient sequential search.
     * Subclasses are encouraged to provide a more efficient implementation.
     * @param value The value to find the key for.
     * @return The key that references the passed value, or null if none is
     *     found.
     */
    @Nullable
    public K getKeyFor(@NotNull V value) {
        for (Entry<K,V> entry : entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Creates a light weight immutable view of this cache. Changes to this
     * cache are reflected in the view. While the object returned here may be
     * considered fully thread safe, it does not use synchronized operations or
     * locks, nor will it throw
     * {@link java.util.ConcurrentModificationException}. Results may not be
     * stable while iterating over its members if modified concurrently.
     * @return An view of this cache object that does not permit changes, while
     *     providing transparent read access to this cache.
     */
    public ConcurrentNavigableMap<K,V> unmodifiable() {
        ConcurrentNavigableMap<K,V> readonly = this.readonly.get();
        if (readonly == null) {
            Immuter<V> immuter = new Immuter<V>() {
                @NotNull
                @Override
                protected V makeImmutable(@NotNull V value) {
                    return unmodifiableValue(value);
                }
            };
            readonly = new ImmutableConcurrentNavigableMap<K,V>(this, immuter);
            this.readonly = new WeakReference<ConcurrentNavigableMap<K,V>>(readonly);
        }
        return readonly;
    }

    /**
     * Allows a value to be wrapped or converted to an immutable form when read
     * by an unmodifiable view of this cache.
     *
     * The default implementation of this method returns the passed value.
     * @param value The mutable value stored in this cahce.
     * @return An immutable form of the passed value.
     */
    @NotNull
    protected V unmodifiableValue(@NotNull V value) {
        return value;
    }

    @Override
    public AbstractCache<K,V> clone() {
        AbstractCache<K,V> clone = (AbstractCache<K,V>)super.clone();
        clone.clear();
        clone.putAll(this);
        return clone;
    }

    /**
     * Allows for customized deep cloning of values in this cache. If 
     * subclasses wish to support deep clone operations, they should override 
     * this method. The default implementation returns the passed value and is 
     * suitable for immutable objects.
     * @param value The value for clone
     * @return The newly cloned value.
     * @see Object#clone()
     */
    @NotNull
    protected V cloneValue(@NotNull V value) {
        return value;
    }
}
