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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:28:39 AM
 * 
 * A collection that contains psi elements while navigating in a 
 * {@link PsiElementPath}.
 */
public class PsiElementCollection extends LinkedHashSet<PsiElement> {

    /**
     * An immutable, empty PsiElementCollection
     */
    public static final PsiElementCollection EMPTY = new PsiElementCollection() {
        @Override
        public Iterator<PsiElement> iterator() {
            return Collections.<PsiElement>emptySet().iterator();
        }

        @Override
        public boolean add(PsiElement element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends PsiElement> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
    };

    public PsiElementCollection() {
    }

    public PsiElementCollection(Collection<? extends PsiElement> c) {
        super(c);
    }

    public PsiElementCollection(int initialSize) {
        super(initialSize);
    }

    /**
     * Convenience method to apply an {@link ElementPredicate} to this
     * collection.
     * @param predicate The predicate to apply
     * @return A new PsiElementCollection that contains a filtered subset of
     *     this collection.
     */
    public PsiElementCollection applyPredicate(ElementPredicate predicate) {
        if (predicate instanceof ElementPredicate.AlwaysTrue) {
            return this;
        }
        PsiElementCollection buffer = new PsiElementCollection();
        for (PsiElement element : this) {
            if (predicate.test(element)) buffer.add(element);
        }
        return buffer;
    }

    /**
     * @return The first elelment of this collection, or null if this
     *     collection is empty.
     */
    public final @Nullable PsiElement oneOrNull() {
        return isEmpty() ? null : iterator().next();
    }

    /**
     * Transforms this collection into another collection by transforming each
     * element with the specified mapper.
     * @param mapper The mapper to transform elements with.
     * @param <T> The type to transform elements into.
     * @return A new collection containing transformed elements.
     */
    public <T> Collection<T> map(PsiElementMapper<T> mapper) {
        Collection<T> buffer = new ArrayList<T>(size());
        for (PsiElement element : this) {
            buffer.add(mapper.map(element));
        }
        return buffer;
    }

    public static PsiElementCollection castOrCopy(Collection<? extends PsiElement> c) {
        if (c instanceof PsiElementCollection) {
            return (PsiElementCollection)c;
        }
        return new PsiElementCollection(c);
    }
}
