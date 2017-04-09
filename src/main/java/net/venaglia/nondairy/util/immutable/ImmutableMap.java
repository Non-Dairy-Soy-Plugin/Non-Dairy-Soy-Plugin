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

package net.venaglia.nondairy.util.immutable;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:32 AM
 *
 * Lightweight wrapper class to make a {@link Map} and its contents immutable.
 */
public class ImmutableMap<K,V> implements Map<K,V> {

    private final Map<K,V> m;

    protected final Immuter<V> immuter;

    public ImmutableMap(Map<K,V> m, Immuter<V> immuter) {
        this.m = m;
        this.immuter = immuter;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return m.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return m.containsValue(o);
    }

    @Override
    public V get(Object o) {
        V value = m.get(o);
        if (value != null) value = immuter.immute(value);
        return value;
    }

    @Override
    public V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return new ImmutableSet<K>(m.keySet(), Immuter.<K>identity());
    }

    @Override
    public Collection<V> values() {
        return new ImmutableCollection<V>(m.values(), immuter);
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return new ImmutableSet<Entry<K,V>>(m.entrySet(), new EntryImmuter());
    }

    protected class ImmutableEntry implements Map.Entry<K,V> {

        private final Map.Entry<K,V> e;

        public ImmutableEntry(Map.Entry<K, V> e) {
            this.e = e;
        }

        @Override
        public K getKey() {
            return e.getKey();
        }

        @Override
        public V getValue() {
            return immuter.immute(e.getValue());
        }

        @Override
        public V setValue(V v) {
            throw new UnsupportedOperationException();
        }
    }

    protected class EntryImmuter extends Immuter<Entry<K,V>> {

        @NotNull
        @Override
        protected Map.Entry<K, V> makeImmutable(@NotNull Map.Entry<K,V> value) {
            return new ImmutableEntry(value);
        }
    }
}
