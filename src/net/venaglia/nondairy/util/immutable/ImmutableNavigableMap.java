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

import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:34 AM
 *
 * Lightweight wrapper class to make a {@link NavigableMap} and its contents
 * immutable.
 */
public class ImmutableNavigableMap<K,V> extends ImmutableSortedMap<K,V> implements NavigableMap<K,V> {

    private final NavigableMap<K,V> m;

    public ImmutableNavigableMap(NavigableMap<K,V> m, Immuter<V> immuter) {
        super(m, immuter);
        this.m = m;
    }

    @Override
    public Map.Entry<K,V> lowerEntry(K k) {
        return new ImmutableEntry(m.lowerEntry(k));
    }

    @Override
    public K lowerKey(K k) {
        return m.lowerKey(k);
    }

    @Override
    public Map.Entry<K,V> floorEntry(K k) {
        return new ImmutableEntry(m.floorEntry(k));
    }

    @Override
    public K floorKey(K k) {
        return m.floorKey(k);
    }

    @Override
    public Map.Entry<K,V> ceilingEntry(K k) {
        return new ImmutableEntry(m.ceilingEntry(k));
    }

    @Override
    public K ceilingKey(K k) {
        return m.ceilingKey(k);
    }

    @Override
    public Map.Entry<K,V> higherEntry(K k) {
        return new ImmutableEntry(m.higherEntry(k));
    }

    @Override
    public K higherKey(K k) {
        return m.higherKey(k);
    }

    @Override
    public Map.Entry<K,V> firstEntry() {
        return new ImmutableEntry(m.firstEntry());
    }

    @Override
    public Map.Entry<K,V> lastEntry() {
        return new ImmutableEntry(m.lastEntry());
    }

    @Override
    public Map.Entry<K,V> pollFirstEntry() {
        return new ImmutableEntry(m.pollFirstEntry());
    }

    @Override
    public Map.Entry<K,V> pollLastEntry() {
        return new ImmutableEntry(m.pollLastEntry());
    }

    @Override
    public NavigableMap<K,V> descendingMap() {
        return new ImmutableNavigableMap<K,V>(m.descendingMap(), immuter);
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new ImmutableNavigableSet<K>(m.navigableKeySet(), Immuter.<K>identity());
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new ImmutableNavigableSet<K>(m.descendingKeySet(), Immuter.<K>identity());
    }

    @Override
    public NavigableMap<K,V> subMap(K i, boolean a, K j, boolean b) {
        return new ImmutableNavigableMap<K,V>(m.subMap(i,a,j,b), immuter);
    }

    @Override
    public NavigableMap<K, V> headMap(K k, boolean b) {
        return new ImmutableNavigableMap<K,V>(m.headMap(k,b), immuter);
    }

    @Override
    public NavigableMap<K, V> tailMap(K k, boolean b) {
        return new ImmutableNavigableMap<K,V>(m.tailMap(k, b), immuter);
    }
}
