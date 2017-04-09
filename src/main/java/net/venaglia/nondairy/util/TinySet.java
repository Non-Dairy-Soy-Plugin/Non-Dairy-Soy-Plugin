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

package net.venaglia.nondairy.util;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * User: ed
 * Date: 2/17/12
 * Time: 8:03 AM
 * 
 * This mutable {@link Set} implementation is memory optimized for long-lived
 * sets that are expected to contain zero or one element 90% of the time.
 */
public class TinySet<E> extends AbstractCollection<E> implements Set<E>, Cloneable {

    private static final Object[] EMPTY = {};
    
    private Set<E> delegate = null;
    private E single = null;
    private int mod = 0;

    @Override
    public int size() {
        return delegate != null ? delegate.size() : (single == null ? 0 : 1);
    }

    @Override
    public Iterator<E> iterator() {
        if (delegate != null) {
            return delegate.iterator();
        }
        return new Iterator<E>() {

            boolean ready = single != null;
            int expectedMod = mod;

            @Override
            public boolean hasNext() {
                if (mod != expectedMod) {
                    throw new ConcurrentModificationException();
                }
                return ready;
            }

            @Override
            public E next() {
                if (mod != expectedMod) {
                    throw new ConcurrentModificationException();
                }
                if (!ready) {
                    throw new NoSuchElementException();
                }
                ready = false;
                return single;
            }

            @Override
            public void remove() {
                if (mod != expectedMod) {
                    throw new ConcurrentModificationException();
                }
                if (ready || single == null) {
                    throw new IllegalStateException();
                }
                single = null;
                expectedMod = ++mod;
            }
        };
    }

    @Override
    public boolean add(E o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (delegate == null) {
            if (single == null) {
                single = o;
                mod++;
                return true;
            } else if (!single.equals(o)) {
                delegate = new HashSet<E>(4);
                delegate.add(single);
                delegate.add(o);
                mod++;
                return true;
            }
            return false;
        } else {
            return delegate.add(o);
        }
    }

    @Override
    public void clear() {
        if (delegate != null || single != null) {
            delegate = null;
            single = null;
            mod++;
        }
    }

    @Override
    public boolean isEmpty() {
        return delegate == null ? single == null : delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate == null ? single == null ? o == null : single.equals(o) : delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate == null ? single != null ? new Object[]{single} : EMPTY : delegate.toArray();
    }

    @SuppressWarnings({ "SuspiciousToArrayCall", "unchecked" })
    @Override
    public <T> T[] toArray(T[] a) {
        if (delegate != null) {
            return delegate.toArray(a);
        } else if (single == null) {
            return a;
        } else if (a.length == 0) {
            T[] aa = (T[])Array.newInstance(a.getClass().getComponentType(), 1);
            Array.set(aa, 0, single);
            return aa;
        } else {
            a[0] = (T)single;
            return a;
        }
    }

    @Override
    public boolean remove(Object o) {
        if (delegate != null) {
            boolean changed = delegate.remove(o);
            if (changed && delegate.isEmpty()) {
                single = null;
                delegate = null;
                mod++;
            }
            return changed;
        } else if (single != null && single.equals(o)) {
            single = null;
            mod++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        if (delegate != null) {
            return delegate.containsAll(objects);
        }
        if (objects.isEmpty()) {
            return true;
        } else if (single != null) {
            for (Object o : objects) {
                if (o == null || !o.equals(single)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> objects) {
        if (delegate != null) {
            if (objects.contains(null)) {
                throw new NullPointerException();
            }
            return delegate.addAll(objects);
        } else if (objects.isEmpty()) {
            return false;
        } else if (single == null) {
            if (objects.contains(null)) {
                throw new NullPointerException();
            } else if (objects.size() == 1) {
                single = objects.iterator().next();
                return true;
            } else {
                delegate = new HashSet<E>(objects);
                if (delegate.size() == 1) {
                    single = delegate.iterator().next();
                    delegate = null;
                }
                return true;
            }
        } else {
            delegate = new HashSet<E>(objects.size() + 1);
            delegate.add(single);
            delegate.addAll(objects);
            if (delegate.size() == 1) {
                delegate = null;
                return false;
            } else {
                single = null;
                return true;
            }
        }
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        if (delegate != null) {
            boolean b = delegate.removeAll(objects);
            if (delegate.size() == 1) {
                single = delegate.iterator().next();
                delegate = null;
            }
            return b;
        } else if (single == null) {
            return false;
        } else if (objects.contains(single)) {
            single = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        if (delegate != null) {
            boolean b = delegate.retainAll(objects);
            if (delegate.size() == 1) {
                single = delegate.iterator().next();
                delegate = null;
            }
            return b;
        } else if (single == null || objects.contains(single)) {
            return false;
        } else {
            single = null;
            return true;
        }
    }

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    public TinySet<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            TinySet<E> s = (TinySet<E>)super.clone();
            s.mod = 0;
            if (delegate != null) {
                s.delegate = new HashSet<E>(delegate);
            } else {
                s.single = single;
            }
            return s;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (delegate != null) {
            return delegate.toString();
        } else if (single != null) {
            return "[" + single + "]";
        } else {
            return "[]";
        }
    }
}
