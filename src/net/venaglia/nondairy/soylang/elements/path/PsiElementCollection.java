/*
 * Copyright 2010 Ed Venaglia
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
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:28:39 AM
 */
public class PsiElementCollection extends LinkedHashSet<PsiElement> {

    public static final PsiElementCollection EMPTY = new PsiElementCollection() {
        @Override
        public Iterator<PsiElement> iterator() {
            final Iterator<PsiElement> iterator = super.iterator();
            return new Iterator<PsiElement>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public PsiElement next() {
                    return iterator.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
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

    public PsiElementCollection applyPredicate(ElementPredicate predicate) {
        PsiElementCollection buffer = new PsiElementCollection();
        for (PsiElement element : this) {
            if (predicate.test(element)) buffer.add(element);
        }
        return buffer;
    }

    public final @Nullable PsiElement oneOrNull() {
        return isEmpty() ? null : iterator().next();
    }

    public <T> Collection<T> map(PsiElementMapper<T> mapper) {
        Collection<T> buffer = new ArrayList<T>(size());
        for (PsiElement element : this) {
            buffer.add(mapper.map(element));
        }
        return buffer;
    }
}
