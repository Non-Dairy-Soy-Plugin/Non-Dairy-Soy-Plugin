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

import java.util.ListIterator;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:36 AM
 *
 * Lightweight wrapper class to make a {@link ListIterator} and its contents
 * immutable.
 */
public class ImmutableListIterator<E> extends ImmutableIterator<E> implements ListIterator<E> {

    private final ListIterator<E> i;

    public ImmutableListIterator(ListIterator<E> i, Immuter<E> immuter) {
        super(i, immuter);
        this.i = i;
    }

    @Override
    public boolean hasPrevious() {
        return i.hasPrevious();
    }

    @Override
    public E previous() {
        return immuter.immute(i.previous());
    }

    @Override
    public int nextIndex() {
        return i.nextIndex();
    }

    @Override
    public int previousIndex() {
        return i.previousIndex();
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }
}
