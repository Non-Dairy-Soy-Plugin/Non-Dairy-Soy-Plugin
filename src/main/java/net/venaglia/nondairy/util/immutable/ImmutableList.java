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

import java.util.Collection;
import java.util.List;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:40 AM
 *
 * Lightweight wrapper class to make a {@link List} and its contents immutable.
 */
public class ImmutableList<E> extends ImmutableCollection<E> implements List<E> {

    private final List<E> l;

    public ImmutableList(List<E> l, Immuter<E> immuter) {
        super(l, immuter);
        this.l = l;
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> es) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int i) {
        return l.get(i);
    }

    @Override
    public E set(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int i, E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return l.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return l.lastIndexOf(o);
    }

    @Override
    public ImmutableListIterator<E> listIterator() {
        return new ImmutableListIterator<E>(l.listIterator(), immuter);
    }

    @Override
    public ImmutableListIterator<E> listIterator(int i) {
        return new ImmutableListIterator<E>(l.listIterator(i), immuter);
    }

    @Override
    public List<E> subList(int i, int j) {
        return new ImmutableList<E>(l.subList(i, j), immuter);
    }
}
