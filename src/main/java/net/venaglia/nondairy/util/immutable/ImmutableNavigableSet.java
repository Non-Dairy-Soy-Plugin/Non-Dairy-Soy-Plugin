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

import java.util.Iterator;
import java.util.NavigableSet;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:40 AM
 *
 * Lightweight wrapper class to make a {@link NavigableSet} and its contents
 * immutable.
 */
public class ImmutableNavigableSet<E> extends ImmutableSortedSet<E> implements NavigableSet<E> {

    private final NavigableSet<E> s;

    public ImmutableNavigableSet(NavigableSet<E> s, Immuter<E> immuter) {
        super(s, immuter);
        this.s = s;
    }

    @Override
    public E lower(E e) {
        return immuter.immute(s.lower(e));
    }

    @Override
    public E floor(E e) {
        return immuter.immute(s.floor(e));
    }

    @Override
    public E ceiling(E e) {
        return immuter.immute(s.ceiling(e));
    }

    @Override
    public E higher(E e) {
        return immuter.immute(s.higher(e));
    }

    @Override
    public E pollFirst() {
        return immuter.immute(s.pollFirst());
    }

    @Override
    public E pollLast() {
        return immuter.immute(s.pollLast());
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ImmutableNavigableSet<E>(s.descendingSet(), immuter);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ImmutableIterator<E>(s.descendingIterator(), immuter);
    }

    @Override
    public NavigableSet<E> subSet(E i, boolean a, E j, boolean b) {
        return new ImmutableNavigableSet<E>(s.subSet(i,a,j,b), immuter);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        return new ImmutableNavigableSet<E>(s.headSet(e, b), immuter);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        return new ImmutableNavigableSet<E>(s.tailSet(e, b), immuter);
    }
}
