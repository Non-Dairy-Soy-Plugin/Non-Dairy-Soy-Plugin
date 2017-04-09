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

import java.util.Comparator;
import java.util.SortedMap;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:33 AM
 *
 * Lightweight wrapper class to make a {@link SortedMap} and its contents
 * immutable.
 */
public class ImmutableSortedMap<K,V> extends ImmutableMap<K,V> implements SortedMap<K,V> {

    private final SortedMap<K,V> m;

    public ImmutableSortedMap(SortedMap<K,V> m, Immuter<V> immuter) {
        super(m, immuter);
        this.m = m;
    }

    @Override
    public Comparator<? super K> comparator() {
        return m.comparator();
    }

    @Override
    public SortedMap<K,V> subMap(K i, K j) {
        return new ImmutableSortedMap<K,V>(m.subMap(i,j), immuter);
    }

    @Override
    public SortedMap<K,V> headMap(K k) {
        return new ImmutableSortedMap<K,V>(m.headMap(k), immuter);
    }

    @Override
    public SortedMap<K,V> tailMap(K k) {
        return new ImmutableSortedMap<K,V>(m.tailMap(k), immuter);
    }

    @Override
    public K firstKey() {
        return m.firstKey();
    }

    @Override
    public K lastKey() {
        return m.lastKey();
    }
}
