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

package io.reactivex.core;

import io.reactivex.common.annotations.*;
import io.reactivex.common.disposables.Disposable;
import io.reactivex.common.functions.Cancellable;

/**
 * Abstraction over a Reactive Streams {@link org.reactivestreams.Subscriber} that allows associating
 * a resource with it and exposes the current number of downstream
 * requested amount.
 * <p>
 * The {@link #onNext(Object)}, {@link #onError(Throwable)}, {@link #tryOnError(Throwable)}
 * and {@link #onComplete()} methods should be called in a sequential manner, just like
 * the {@link org.reactivestreams.Subscriber Subscriber}'s methods.
 * Use the {@code FlowableEmitter} the {@link #serialize()} method returns instead of the original
 * {@code FlowableEmitter} instance provided by the generator routine if you want to ensure this.
 * The other methods are thread-safe.
 * <p>
 * The emitter allows the registration of a single resource, in the form of a {@link Disposable}
 * or {@link Cancellable} via {@link #setDisposable(Disposable)} or {@link #setCancellable(Cancellable)}
 * respectively. The emitter implementations will dispose/cancel this instance when the
 * downstream cancels the flow or after the event generator logic calls {@link #onError(Throwable)},
 * {@link #onComplete()} or when {@link #tryOnError(Throwable)} succeeds.
 * <p>
 * Only one {@code Disposable} or {@code Cancellable} object can be associated with the emitter at
 * a time. Calling either {@code set} method will dispose/cancel any previous object. If there
 * is a need for handling multiple resources, one can create a {@link io.reactivex.common.disposables.CompositeDisposable}
 * and associate that with the emitter instead.
 * <p>
 * The {@link Cancellable} is logically equivalent to {@code Disposable} but allows using cleanup logic that can
 * throw a checked exception (such as many {@code close()} methods on Java IO components). Since
 * the release of resources happens after the terminal events have been delivered or the sequence gets
 * cancelled, exceptions throw within {@code Cancellable} are routed to the global error handler via
 * {@link io.reactivex.core.plugins.RxJavaPlugins#onError(Throwable)}.
 *
 * @param <T> the value type to emit
 */
public interface FlowableEmitter<T> extends Emitter<T> {

    /**
     * Sets a Disposable on this emitter; any previous {@link Disposable}
     * or {@link Cancellable} will be disposed/cancelled.
     * @param d the disposable, null is allowed
     */
    void setDisposable(@Nullable Disposable d);

    /**
     * Sets a Cancellable on this emitter; any previous {@link Disposable}
     * or {@link Cancellable} will be disposed/cancelled.
     * @param c the cancellable resource, null is allowed
     */
    void setCancellable(@Nullable Cancellable c);

    /**
     * The current outstanding request amount.
     * <p>This method is thread-safe.
     * @return the current outstanding request amount
     */
    long requested();

    /**
     * Returns true if the downstream cancelled the sequence or the
     * emitter was terminated via {@link #onError(Throwable)}, {@link #onComplete} or a
     * successful {@link #tryOnError(Throwable)}.
     * <p>This method is thread-safe.
     * @return true if the downstream cancelled the sequence or the emitter was terminated
     */
    boolean isCancelled();

    /**
     * Ensures that calls to onNext, onError and onComplete are properly serialized.
     * @return the serialized FlowableEmitter
     */
    @NonNull
    FlowableEmitter<T> serialize();

    /**
     * Attempts to emit the specified {@code Throwable} error if the downstream
     * hasn't cancelled the sequence or is otherwise terminated, returning false
     * if the emission is not allowed to happen due to lifecycle restrictions.
     * <p>
     * Unlike {@link #onError(Throwable)}, the {@code RxJavaPlugins.onError} is not called
     * if the error could not be delivered.
     * <p>History: 2.1.1 - experimental
     * @param t the throwable error to signal if possible
     * @return true if successful, false if the downstream is not able to accept further
     * events
     * @since 2.2
     */
    boolean tryOnError(@NonNull Throwable t);
}
