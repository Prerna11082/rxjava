/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.core.internal.subscribers;

import io.reactivex.core.FlowableSubscriber;
import io.reactivex.core.internal.util.BlockingHelper;
import io.reactivex.core.internal.subscriptions.SubscriptionHelper;
import io.reactivex.core.plugins.RxJavaPlugins;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.reactivex.common.internal.util.ExceptionHelper.timeoutMessage;

/**
 * A Subscriber + Future that expects exactly one upstream value and provides it
 * via the (blocking) Future API.
 *
 * @param <T> the value type
 */
public final class FutureSubscriber<T> extends CountDownLatch
implements FlowableSubscriber<T>, Future<T>, Subscription {

    T value;
    Throwable error;

    final AtomicReference<Subscription> upstream;

    public FutureSubscriber() {
        super(1);
        this.upstream = new AtomicReference<Subscription>();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        for (;;) {
            Subscription a = upstream.get();
            if (a == this || a == SubscriptionHelper.CANCELLED) {
                return false;
            }

            if (upstream.compareAndSet(a, SubscriptionHelper.CANCELLED)) {
                if (a != null) {
                    a.cancel();
                }
                countDown();
                return true;
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return upstream.get() == SubscriptionHelper.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            await();
        }

        if (isCancelled()) {
            throw new CancellationException();
        }
        Throwable ex = error;
        if (ex != null) {
            throw new ExecutionException(ex);
        }
        return value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (getCount() != 0) {
            BlockingHelper.verifyNonBlocking();
            if (!await(timeout, unit)) {
                throw new TimeoutException(timeoutMessage(timeout, unit));
            }
        }

        if (isCancelled()) {
            throw new CancellationException();
        }

        Throwable ex = error;
        if (ex != null) {
            throw new ExecutionException(ex);
        }
        return value;
    }

    @Override
    public void onSubscribe(Subscription s) {
        SubscriptionHelper.setOnce(this.upstream, s, Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        if (value != null) {
            upstream.get().cancel();
            onError(new IndexOutOfBoundsException("More than one element received"));
            return;
        }
        value = t;
    }

    @Override
    public void onError(Throwable t) {
        for (;;) {
            Subscription a = upstream.get();
            if (a == this || a == SubscriptionHelper.CANCELLED) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            if (upstream.compareAndSet(a, this)) {
                countDown();
                return;
            }
        }
    }

    @Override
    public void onComplete() {
        if (value == null) {
            onError(new NoSuchElementException("The source is empty"));
            return;
        }
        for (;;) {
            Subscription a = upstream.get();
            if (a == this || a == SubscriptionHelper.CANCELLED) {
                return;
            }
            if (upstream.compareAndSet(a, this)) {
                countDown();
                return;
            }
        }
    }

    @Override
    public void cancel() {
        // ignoring as `this` means a finished Subscription only
    }

    @Override
    public void request(long n) {
        // ignoring as `this` means a finished Subscription only
    }
}