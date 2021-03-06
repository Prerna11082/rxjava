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

public final class FlowableCountSingle<T> extends Single<Long> implements FuseToFlowable<Long> {

    final Flowable<T> source;

    public FlowableCountSingle(Flowable<T> source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super Long> observer) {
        source.subscribe(new CountSubscriber(observer));
    }

    @Override
    public Flowable<Long> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableCount<T>(source));
    }

    static final class CountSubscriber implements FlowableSubscriber<Object>, Disposable {

        final SingleObserver<? super Long> downstream;

        Subscription upstream;

        long count;

        CountSubscriber(SingleObserver<? super Long> downstream) {
            this.downstream = downstream;
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
        public void onNext(Object t) {
            count++;
        }

        @Override
        public void onError(Throwable t) {
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onSuccess(count);
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
