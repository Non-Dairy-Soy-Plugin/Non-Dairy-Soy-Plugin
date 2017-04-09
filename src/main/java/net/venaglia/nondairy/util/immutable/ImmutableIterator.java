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

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:36 AM
 *
 * Lightweight wrapper class to make an {@link Iterator} and its contents
 * immutable.
 */
public class ImmutableIterator<E> implements Iterator<E> {

    private final Iterator<E> i;

    protected final Immuter<E> immuter;

    public ImmutableIterator(Iterator<E> i, Immuter<E> immuter) {
        this.i = i;
        this.immuter = immuter;
    }

    @Override
    public boolean hasNext() {
        return i.hasNext();
    }

    @Override
    public E next() {
        return immuter.immute(i.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
