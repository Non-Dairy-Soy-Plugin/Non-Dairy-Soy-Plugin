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
import java.util.SortedSet;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:39 AM
 *
 * Lightweight wrapper class to make a {@link SortedSet} and its contents
 * immutable.
 */
public class ImmutableSortedSet<E> extends ImmutableSet<E> implements SortedSet<E> {

    private final SortedSet<E> s;

    public ImmutableSortedSet(SortedSet<E> s, Immuter<E> immuter) {
        super(s, immuter);
        this.s = s;
    }

    @Override
    public Comparator<? super E> comparator() {
        return s.comparator();
    }

    @Override
    public SortedSet<E> subSet(E i, E j) {
        return new ImmutableSortedSet<E>(s.subSet(i,j), immuter);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return new ImmutableSortedSet<E>(s.headSet(e), immuter);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return new ImmutableSortedSet<E>(s.tailSet(e), immuter);
    }

    @Override
    public E first() {
        return immuter.immute(s.first());
    }

    @Override
    public E last() {
        return immuter.immute(s.last());
    }
}
