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

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:35 AM
 *
 * Lightweight wrapper class to make a {@link ConcurrentNavigableMap} and its
 * contents immutable.
 */
public class ImmutableConcurrentNavigableMap<K,V> extends ImmutableNavigableMap<K,V> implements ConcurrentNavigableMap<K,V> {

    private final ConcurrentNavigableMap<K,V> m;

    public ImmutableConcurrentNavigableMap(ConcurrentNavigableMap<K, V> m, Immuter<V> immuter) {
        super(m, immuter);
        this.m = m;
    }

    @Override
    public V putIfAbsent(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o, Object o1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K k, V v, V v1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConcurrentNavigableMap<K,V> subMap(K i, boolean a, K j, boolean b) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.subMap(i,a,j,b), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> headMap(K k, boolean b) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.headMap(k, b), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> tailMap(K k, boolean b) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.tailMap(k, b), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> subMap(K i, K j) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.subMap(i, j), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> headMap(K k) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.headMap(k), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> tailMap(K k) {
        return new ImmutableConcurrentNavigableMap<K,V>(m.tailMap(k), immuter);
    }

    @Override
    public ConcurrentNavigableMap<K,V> descendingMap() {
        return new ImmutableConcurrentNavigableMap<K,V>(m.descendingMap(), immuter);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new ImmutableNavigableSet<K>(m.navigableKeySet(), Immuter.<K>identity());
    }

    @Override
    public NavigableSet<K> keySet() {
        return new ImmutableNavigableSet<K>(m.keySet(), Immuter.<K>identity());
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new ImmutableNavigableSet<K>(m.descendingKeySet(), Immuter.<K>identity());
    }
}
