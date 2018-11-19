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

package io.reactivex.core.internal.operators.flowable;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.core.Flowable;
import io.reactivex.core.FlowableSubscriber;
import io.reactivex.core.Single;
import io.reactivex.core.SingleObserver;
import io.reactivex.core.internal.fuseable.FuseToFlowable;
import io.reactivex.core.internal.subscriptions.SubscriptionHelper;
import io.reactivex.core.plugins.RxJavaPlugins;
import org.reactivestreams.Subscription;

import java.util.NoSuchElementException;

public final class FlowableElementAtSingle<T> extends Single<T> implements FuseToFlowable<T> {
    final Flowable<T> source;

    final long index;

    final T defaultValue;

    public FlowableElementAtSingle(Flowable<T> source, long index, T defaultValue) {
        this.source = source;
        this.index = index;
        this.defaultValue = defaultValue;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new ElementAtSubscriber<T>(observer, index, defaultValue));
    }

    @Override
    public Flowable<T> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableElementAt<T>(source, index, defaultValue, true));
    }

    static final class ElementAtSubscriber<T> implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final long index;
        final T defaultValue;

        Subscription upstream;

        long count;

        boolean done;

        ElementAtSubscriber(SingleObserver<? super T> actual, long index, T defaultValue) {
            this.downstream = actual;
            this.index = index;
            this.defaultValue = defaultValue;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long c = count;
            if (c == index) {
                done = true;
                upstream.cancel();
                upstream = SubscriptionHelper.CANCELLED;
                downstream.onSuccess(t);
                return;
            }
            count = c + 1;
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            if (!done) {
                done = true;

                T v = defaultValue;

                if (v != null) {
                    downstream.onSuccess(v);
                } else {
                    downstream.onError(new NoSuchElementException());
                }
            }
        }

        @Override
        public void dispose() {
            upstream.cancel();
            upstream = SubscriptionHelper.CANCELLED;
        }

        @Override
        public boolean isDisposed() {
            return upstream == SubscriptionHelper.CANCELLED;
        }
    }
}
