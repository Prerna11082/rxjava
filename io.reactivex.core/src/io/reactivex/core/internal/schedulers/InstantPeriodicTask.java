/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.core.internal.schedulers; import io.reactivex.core.*;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.core.internal.functions.Functions;
import io.reactivex.core.plugins.RxJavaPlugins;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper for a regular task that gets immediately rescheduled when the task completed.
 */
public final class InstantPeriodicTask implements Callable<Void>, Disposable {

    final Runnable task;

    final AtomicReference<Future<?>> rest;

    final AtomicReference<Future<?>> first;

    final ExecutorService executor;

    public Thread runner;

    static final FutureTask<Void> CANCELLED = new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null);

    public InstantPeriodicTask(Runnable task, ExecutorService executor) {
        super();
        this.task = task;
        this.first = new AtomicReference<Future<?>>();
        this.rest = new AtomicReference<Future<?>>();
        this.executor = executor;
    }

    @Override
    public Void call() throws Exception {
        runner = Thread.currentThread();
        try {
            task.run();
            setRest(executor.submit(this));
            runner = null;
        } catch (Throwable ex) {
            runner = null;
            RxJavaPlugins.onError(ex);
        }
        return null;
    }

    @Override
    public void dispose() {
        Future<?> current = first.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
        current = rest.getAndSet(CANCELLED);
        if (current != null && current != CANCELLED) {
            current.cancel(runner != Thread.currentThread());
        }
    }

    @Override
    public boolean isDisposed() {
        return first.get() == CANCELLED;
    }

    public void setFirst(Future<?> f) {
        for (;;) {
            Future<?> current = first.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
                return;
            }
            if (first.compareAndSet(current, f)) {
                return;
            }
        }
    }

    public void setRest(Future<?> f) {
        for (;;) {
            Future<?> current = rest.get();
            if (current == CANCELLED) {
                f.cancel(runner != Thread.currentThread());
                return;
            }
            if (rest.compareAndSet(current, f)) {
                return;
            }
        }
    }
}
