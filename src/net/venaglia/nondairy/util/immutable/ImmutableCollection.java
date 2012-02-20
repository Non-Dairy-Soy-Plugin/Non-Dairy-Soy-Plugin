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

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:37 AM
 *
 * Lightweight wrapper class to make a {@link Collection} and its contents
 * immutable.
 */
public class ImmutableCollection<E> extends ImmutableIterable<E> implements Collection<E> {

    private final Collection<E> c;

    public ImmutableCollection(Collection<E> c, Immuter<E> immuter) {
        super(c, immuter);
        this.c = c;
    }

    @Override
    public int size() {
        return c.size();
    }

    @Override
    public boolean isEmpty() {
        return c.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return c.contains(o);
    }

    @Override
    public Object[] toArray() {
        return immuteArray(c.toArray());
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] toArray(T[] a) {
        return immuteArray(c.toArray(a));
    }

    @SuppressWarnings("unchecked")
    private <T> T[] immuteArray(T[] a) {
        for (int i = 0, l = a.length; i < l; ++i) {
            a[i] = (T)immuter.immute((E)a[i]);
        }
        return a;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return c.containsAll(objects);
    }

    @Override
    public boolean addAll(Collection<? extends E> es) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return c.equals(o);
    }

    @Override
    public int hashCode() {
        return c.hashCode();
    }
}
