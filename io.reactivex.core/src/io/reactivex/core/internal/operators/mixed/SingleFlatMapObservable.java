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

package io.reactivex.core.internal.operators.mixed;

import java.util.concurrent.atomic.*;

import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;

/**
 * Maps the success value of a Single onto an ObservableSource and
 * relays its signals to the downstream observer.
 * 
 * @param <T> the success value type of the Single source
 * @param <R> the result type of the ObservableSource and this operator
 * @since 2.1.15
 */
public final class SingleFlatMapObservable<T, R> extends Observable<R> {

    final SingleSource<T> source;

    final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

    public SingleFlatMapObservable(SingleSource<T> source,
            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        FlatMapObserver<T, R> parent = new FlatMapObserver<T, R>(observer, mapper);
        observer.onSubscribe(parent);
        source.subscribe(parent);
    }

    static final class FlatMapObserver<T, R>
    extends AtomicReference<Disposable>
    implements Observer<R>, SingleObserver<T>, Disposable {

        private static final long serialVersionUID = -8948264376121066672L;

        final Observer<? super R> downstream;

        final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

        FlatMapObserver(Observer<? super R> downstream, Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
            this.downstream = downstream;
            this.mapper = mapper;
        }

        @Override
        public void onNext(R t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.replace(this, d);
        }

        @Override
        public void onSuccess(T t) {
            ObservableSource<? extends R> o;

            try {
                o = ObjectHelper.requireNonNull(mapper.apply(t), "The mapper returned a null Publisher");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            o.subscribe(this);
        }

    }
}
