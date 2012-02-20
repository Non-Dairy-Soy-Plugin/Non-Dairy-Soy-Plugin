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

import java.util.Set;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:38 AM
 *
 * Lightweight wrapper class to make a {@link Set} and its contents immutable.
 */
public class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {

    public ImmutableSet(Set<E> c, Immuter<E> immuter) {
        super(c, immuter);
    }
}
