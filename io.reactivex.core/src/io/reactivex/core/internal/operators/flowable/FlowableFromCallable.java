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

import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.core.Flowable;
import io.reactivex.core.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.core.plugins.RxJavaPlugins;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;

public final class FlowableFromCallable<T> extends Flowable<T> implements Callable<T> {
    final Callable<? extends T> callable;
    public FlowableFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        DeferredScalarSubscription<T> deferred = new DeferredScalarSubscription<T>(s);
        s.onSubscribe(deferred);

        T t;
        try {
            t = ObjectHelper.requireNonNull(callable.call(), "The callable returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (deferred.isCancelled()) {
                RxJavaPlugins.onError(ex);
            } else {
                s.onError(ex);
            }
            return;
        }

        deferred.complete(t);
    }

    @Override
    public T call() throws Exception {
        return ObjectHelper.requireNonNull(callable.call(), "The callable returned a null value");
    }
}
