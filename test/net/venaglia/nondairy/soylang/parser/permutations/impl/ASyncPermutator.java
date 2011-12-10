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
import net.venaglia.nondairy.soylang.parser.permutations.Permutator;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composite permutator that executes in a background thread
 */
public final class ASyncPermutator implements PermutationProducer {

    private static final AtomicInteger PRODUCER_SEQ = new AtomicInteger(1);

    private final PermutationDeque deque;
    private final CompositePermutator impl;

    private volatile boolean started = false;
    private volatile boolean running = false;
    private Runnable worker = null;
    private final String resrouceName;

    public ASyncPermutator(Collection<Permutator> permutators, String resourceName) {
        this.impl = new CompositePermutator(permutators);
        this.deque = new PermutationDeque();
        this.resrouceName = resourceName == null ? "" : resourceName.trim();
    }

    public ASyncPermutator(Collection<Permutator> permutators) {
        this(permutators, "");
    }

    public synchronized void permutate(final CharSequence source,
                                       @NonNls final String initialState) {
        if (worker != null) {
            throw new IllegalStateException("ASyncPermutator.permutate() may only be called once");
        }
        worker = new PermutationWorker(source, initialState, deque);
    }

    @Override
    public boolean hasNext() {
        ensureRunning();
        return deque.hasNext();
    }

    @Override
    public void next() {
        ensureRunning();
        deque.next();
    }

    @Override
    public Iterator<SoySymbol> getIterator() {
        ensureRunning();
        return deque.getIterator();
    }

    @Override
    public CharSequence getModifiedSource() {
        ensureRunning();
        return deque.getModifiedSource();
    }

    @Override
    public String getPermutatorName() {
        ensureRunning();
        return deque.getPermutatorName();
    }

    @Override
    public int getSeq() {
        ensureRunning();
        return deque.getSeq();
    }

    private void ensureRunning() {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    started = true;
                    start();
                }
            }
        }
    }

    private void start() {
        if (worker == null) {
            throw new IllegalStateException("ASyncPermutator.permutate() must be called before any other method");
        }
        running = true;
        String name = resrouceName.length() > 0 ? resrouceName + " - " : "";
        @NonNls String threadName = "Permutation Producer - " + PRODUCER_SEQ.getAndIncrement() + name;
        Thread thread = new Thread(worker, threadName);
        thread.start();
    }

    private class PermutationWorker implements Runnable {

        private final CharSequence source;
        private final String initialState;
        private final PermutationConsumer consumer;

        public PermutationWorker(CharSequence source, String initialState, PermutationConsumer consumer) {
            this.source = source;
            this.initialState = initialState;
            this.consumer = consumer;
        }

        public void run() {
            try {
                impl.permutate(source, initialState, consumer);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                deque.close();
            }
        }
    }
}
