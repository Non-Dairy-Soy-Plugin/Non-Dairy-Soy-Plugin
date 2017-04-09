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

import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: ed
 * Date: 6/18/12
 * Time: 8:27 AM
 */
public class MultiThreader implements Executor {

    private final ThreadPoolExecutor exec;
    private final AtomicReference<Throwable> failure;

    public MultiThreader() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
        exec = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, queue, new ThreadFactory() {

            private final AtomicInteger seq = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread("MultiThreader-" + seq.getAndIncrement()) { //NON-NLS

                    @Override
                    public void run() {
                        try {
                            super.run();
                        } catch (Throwable t) {
                            failure.compareAndSet(null, t);
                        }
                    }
                };
            }
        });
        failure = new AtomicReference<Throwable>();
    }

    @Override
    public void execute(Runnable runnable) {
        checkForErrors();
        exec.execute(runnable);
        checkForErrors();
    }

    public void checkForErrors() {
        Throwable fail = failure.get();
        if (fail != null) {
            throw new RuntimeException(fail);
        }
    }

    public void flush() {
        checkForErrors();
        exec.purge();
        checkForErrors();
    }

    private static final AtomicReference<MultiThreader> MULTI_THREADER = new AtomicReference<MultiThreader>();

    public static void run(Runnable runnable) {
        MultiThreader multiThreader;
        synchronized (MULTI_THREADER) {
            multiThreader = MULTI_THREADER.get();
            if (multiThreader == null) {
                multiThreader = new MultiThreader();
                MULTI_THREADER.set(multiThreader);
            }
        }
        multiThreader.execute(runnable);
    }
}
