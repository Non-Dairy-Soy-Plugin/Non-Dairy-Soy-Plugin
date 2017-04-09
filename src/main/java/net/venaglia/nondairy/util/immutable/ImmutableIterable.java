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

import java.lang.Iterable;
import java.util.Iterator;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:37 AM
 *
 * Lightweight wrapper class to make an {@link Iterable} and its contents
 * immutable.
 */
public class ImmutableIterable<E> implements Iterable<E> {

    private final Iterable<E> i;

    protected final Immuter<E> immuter;

    public ImmutableIterable(Iterable<E> i, Immuter<E> immuter) {
        this.i = i;
        this.immuter = immuter;
    }

    @Override
    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(i.iterator(), immuter);
    }
}
