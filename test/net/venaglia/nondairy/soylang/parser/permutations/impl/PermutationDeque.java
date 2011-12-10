/*
 * Copyright 2011 Ed Venaglia
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

package net.venaglia.nondairy.soylang.parser.permutations.impl;

import net.venaglia.nondairy.soylang.lexer.SoySymbol;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationConsumer;
import net.venaglia.nondairy.soylang.parser.permutations.PermutationProducer;
import org.jetbrains.annotations.NonNls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Base class for building permutations of symbol order
 */
class PermutationDeque implements PermutationConsumer, PermutationProducer {

    @NonNls
    private static final String HEX_DIGITS = "0123456789abcdef";

    private final BlockingDeque<Entry> queue = new LinkedBlockingDeque<Entry>(1);
    private final ThreadLocal<Map<String,int[]>> seq = new ThreadLocal<Map<String,int[]>>() {
        @Override
        protected Map<String,int[]> initialValue() {
            return new HashMap<String, int[]>() {
                @Override
                public int[] get(Object o) {
                    if (super.containsKey(o)) {
                        return super.get(o);
                    }
                    int[] value = {1};
                    super.put((String)o,value);
                    return value;
                }
            };
        }
    };
    private MessageDigest digest;
    private final Set<String> alreadyUsedDigests = new HashSet<String>();

    private final ThreadLocal<String> permutatorName = new ThreadLocal<String>();

    public PermutationDeque() {
        try {
            //noinspection HardCodedStringLiteral
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPermutatorName(String permutatorName) {
        this.permutatorName.set(permutatorName);
    }

    @Override
    public void add(Iterator<SoySymbol> iterator, CharSequence modifiedSource) {
        try {
            String name = permutatorName.get();
            int[] seq = this.seq.get().get(name);
            int mySeq = seq[0]++;
            testForDuplicate(name, mySeq, modifiedSource);
            queue.put(new Entry(iterator, modifiedSource, name, mySeq));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void testForDuplicate(String permutatorName, int seq, CharSequence modifiedSource) {
        byte[] digest = this.digest.digest(modifiedSource.toString().getBytes());
        char[] buffer = new char[digest.length << 1];
        for (int i = 0, j = 0, l = digest.length; i < l; ++i) {
            int b = digest[i];
            buffer[j++] = HEX_DIGITS.charAt((b & 0xF0) >> 4);
            buffer[j++] = HEX_DIGITS.charAt(b & 0x0F);
        }
        String sha1 = new String(buffer);
        if (!alreadyUsedDigests.add(sha1)) {
            //noinspection HardCodedStringLiteral
            throw new RuntimeException(String.format("Duplicate source, perm=%s, seq=%d: %s", permutatorName, seq, modifiedSource));
        }
    }

    public void close() {
        if (!closeSent) {
            closeSent = true;
            try {
                queue.put(closingEntry);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Entry closingEntry = new Entry(null,null,"ROF",Integer.MAX_VALUE);

    private boolean closeSent = false;
    private Entry current = null;
    private Entry next = null;

    @Override
    public boolean hasNext() {
        fetchNext();
        return (next != closingEntry);
    }

    private void fetchNext() {
        try {
            next = next == null ? queue.take() : next;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void next() {
        fetchNext();
        if (next != closingEntry) {
            current = next;
            next = null;
        }
        else {
            current = null;
            throw new NoSuchElementException();
        }
    }

    @Override
    public Iterator<SoySymbol> getIterator() {
        ensureCurrent("getIterator");
        return current.getIterator();
    }

    @Override
    public CharSequence getModifiedSource() {
        ensureCurrent("getModifiedSource");
        return current.getModifiedSource();
    }

    @Override
    public String getPermutatorName() {
        ensureCurrent("getPermutatorName");
        return current.getName();
    }

    @Override
    public int getSeq() {
        ensureCurrent("getSeq");
        return current.getSeq();
    }

    private void ensureCurrent(@NonNls String methodName) {
        if (current == null) {
            throw new IllegalStateException("Must call next() before calling " + methodName + "()");
        }
    }

    private static final class Entry {
        private final CharSequence modifiedSource;
        private final String name;
        private final int seq;

        private Iterator<SoySymbol> iterator;
        private Collection<SoySymbol> buffer;

        private Entry(Iterator<SoySymbol> iterator,
                      CharSequence modifiedSource,
                      @NonNls String name,
                      int seq) {
            this.iterator = iterator;
            this.modifiedSource = modifiedSource;
            this.name = name;
            this.seq = seq;
        }

        public Iterator<SoySymbol> getIterator() {
            if (buffer == null) {
                buffer = new LinkedList<SoySymbol>();
                while (iterator.hasNext()) buffer.add(iterator.next());
                iterator = null;
            }
            return buffer.iterator();
        }

        public CharSequence getModifiedSource() {
            return modifiedSource;
        }

        public String getName() {
            return name;
        }

        public int getSeq() {
            return seq;
        }
    }
}
