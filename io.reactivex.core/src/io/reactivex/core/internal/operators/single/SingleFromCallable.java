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

package io.reactivex.core.internal.operators.single; import io.reactivex.core.*;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.core.disposables.Disposables;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.core.plugins.RxJavaPlugins;

import java.util.concurrent.Callable;

public final class SingleFromCallable<T> extends Single<T> {

    final Callable<? extends T> callable;

    public SingleFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        Disposable d = Disposables.empty();
        observer.onSubscribe(d);

        if (d.isDisposed()) {
            return;
        }
        T value;

        try {
            value = ObjectHelper.requireNonNull(callable.call(), "The callable returned a null value");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (!d.isDisposed()) {
                observer.onError(ex);
            } else {
                RxJavaPlugins.onError(ex);
            }
            return;
        }

        if (!d.isDisposed()) {
            observer.onSuccess(value);
        }
    }
}
