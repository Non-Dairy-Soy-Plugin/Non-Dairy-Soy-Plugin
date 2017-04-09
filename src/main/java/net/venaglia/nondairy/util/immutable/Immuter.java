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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;

/**
 * User: ed
 * Date: 2/15/12
 * Time: 7:41 AM
 *
 * Abstract class used by the immutable classes in this package to transform
 * contained elements to immutable forms.
 */
public abstract class Immuter<T> {

    private static Immuter<?> IDENTITY = new Immuter<Object>() {
        @NotNull
        @Override
        protected Object makeImmutable(@NotNull Object value) {
            return value;
        }
    };

    /**
     * Transforms the passed value into an immutable one.
     * @param value The value to make immutable.
     * @return An immutable for of the passed value.
     */
    public @Nullable T immute(@Nullable T value) {
        if (value == null) {
            return null;
        }
        return makeImmutable(value);
    }

    /**
     * Transforms the passed value into an immutable one. Implementation
     * classes should override this method rather than {@link #immute(Object)}.
     * @param value The value to make immutable.
     * @return An immutable for of the passed value.
     */
    @NotNull
    protected abstract T makeImmutable(@NotNull T value);

    /**
     * Returns an Immuter suitable for collections classes the contain
     * immutable elements.
     * @param <T> The type of the contained elements.
     * @return The singleton Immuter instance that always returns the passed
     *     value.
     */
    @SuppressWarnings("unchecked")
    public static <T> Immuter<T> identity() {
        return (Immuter<T>)IDENTITY;
    }

    public static <E> Iterable<E> immute(Iterable<E> i, Immuter<E> immuter) {
        return new ImmutableIterable<E>(i, immuter);
    }

    public static <E> Iterable<E> immute(Iterable<E> i) {
        return new ImmutableIterable<E>(i, Immuter.<E>identity());
    }
    
    public static <E> Collection<E> immute(Collection<E> c, Immuter<E> immuter) {
        return new ImmutableCollection<E>(c, immuter);
    }

    public static <E> Set<E> immute(Set<E> s, Immuter<E> immuter) {
        return new ImmutableSet<E>(s, immuter);
    }

    public static <E> SortedSet<E> immute(SortedSet<E> s, Immuter<E> immuter) {
        return new ImmutableSortedSet<E>(s, immuter);
    }

    public static <E> NavigableSet<E> immute(NavigableSet<E> s, Immuter<E> immuter) {
        return new ImmutableNavigableSet<E>(s, immuter);
    }

    public static <E> List<E> immute(List<E> l, Immuter<E> immuter) {
        return new ImmutableList<E>(l, immuter);
    }

    public static <K,V> Map<K,V> immute(Map<K,V> m, Immuter<V> immuter) {
        return new ImmutableMap<K,V>(m, immuter);
    }

    public static <K,V> Map<K,V> immute(Map<K,V> m) {
        return new ImmutableMap<K,V>(m, Immuter.<V>identity());
    }

    public static <K,V> SortedMap<K,V> immute(SortedMap<K,V> m, Immuter<V> immuter) {
        return new ImmutableSortedMap<K,V>(m, immuter);
    }

    public static <K,V> SortedMap<K,V> immute(SortedMap<K,V> m) {
        return new ImmutableSortedMap<K,V>(m, Immuter.<V>identity());
    }

    public static <K,V> NavigableMap<K,V> immute(NavigableMap<K,V> m, Immuter<V> immuter) {
        return new ImmutableNavigableMap<K,V>(m, immuter);
    }

    public static <K,V> NavigableMap<K,V> immute(NavigableMap<K,V> m) {
        return new ImmutableNavigableMap<K,V>(m, Immuter.<V>identity());
    }

    public static <K,V> ConcurrentNavigableMap<K,V> immute(ConcurrentNavigableMap<K,V> m, Immuter<V> immuter) {
        return new ImmutableConcurrentNavigableMap<K,V>(m, immuter);
    }

    public static <K,V> ConcurrentNavigableMap<K,V> immute(ConcurrentNavigableMap<K,V> m) {
        return new ImmutableConcurrentNavigableMap<K,V>(m, Immuter.<V>identity());
    }
}
