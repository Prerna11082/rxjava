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

package io.reactivex.core.internal.observers;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.common.exceptions.OnErrorNotImplementedException;
import io.reactivex.core.CompletableObserver;
import io.reactivex.core.internal.disposables.DisposableHelper;
import io.reactivex.core.observers.LambdaConsumerIntrospection;
import io.reactivex.core.plugins.RxJavaPlugins;

import java.util.concurrent.atomic.AtomicReference;

public final class EmptyCompletableObserver
extends AtomicReference<Disposable>
implements CompletableObserver, Disposable, LambdaConsumerIntrospection {

    private static final long serialVersionUID = -7545121636549663526L;

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }

    @Override
    public void onComplete() {
        // no-op
        lazySet(DisposableHelper.DISPOSED);
    }

    @Override
    public void onError(Throwable e) {
        lazySet(DisposableHelper.DISPOSED);
        RxJavaPlugins.onError(new OnErrorNotImplementedException(e));
    }

    @Override
    public void onSubscribe(Disposable d) {
        DisposableHelper.setOnce(this, d);
    }

    @Override
    public boolean hasCustomOnError() {
        return false;
    }
}
