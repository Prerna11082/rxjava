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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.reactivestreams.Subscription;

import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.common.internal.disposables.EmptyDisposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.FuseToFlowable;
import io.reactivex.core.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.ArrayListSupplier;
import io.reactivex.plugins.RxJavaPlugins;

public final class FlowableToListSingle<T, U extends Collection<? super T>> extends Single<U> implements FuseToFlowable<U> {

    final Flowable<T> source;

    final Callable<U> collectionSupplier;

    @SuppressWarnings("unchecked")
    public FlowableToListSingle(Flowable<T> source) {
        this(source, (Callable<U>)ArrayListSupplier.asCallable());
    }

    public FlowableToListSingle(Flowable<T> source, Callable<U> collectionSupplier) {
        this.source = source;
        this.collectionSupplier = collectionSupplier;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super U> observer) {
        U coll;
        try {
            coll = ObjectHelper.requireNonNull(collectionSupplier.call(), "The collectionSupplier returned a null collection. Null values are generally not allowed in 2.x operators and sources.");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, observer);
            return;
        }
        source.subscribe(new ToListSubscriber<T, U>(observer, coll));
    }

    @Override
    public Flowable<U> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableToList<T, U>(source, collectionSupplier));
    }

    static final class ToListSubscriber<T, U extends Collection<? super T>>
    implements FlowableSubscriber<T>, Disposable {

        final SingleObserver<? super U> downstream;

        Subscription upstream;

        U value;

        ToListSubscriber(SingleObserver<? super U> actual, U collection) {
            this.downstream = actual;
            this.value = collection;
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
            value.add(t);
        }

        @Override
        public void onError(Throwable t) {
            value = null;
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            upstream = SubscriptionHelper.CANCELLED;
            downstream.onSuccess(value);
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
