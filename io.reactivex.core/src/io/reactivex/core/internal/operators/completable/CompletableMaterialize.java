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

package io.reactivex.core.internal.operators.completable;

import io.reactivex.common.annotations.Experimental;
import io.reactivex.core.internal.operators.mixed.MaterializeSingleObserver;
import io.reactivex.core.*;

/**
 * Turn the signal types of a Completable source into a single Notification of
 * equal kind.
 *
 * @param <T> the element type of the source
 * @since 2.2.4 - experimental
 */
@Experimental
public final class CompletableMaterialize<T> extends Single<Notification<T>> {

    final Completable source;

    public CompletableMaterialize(Completable source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super Notification<T>> observer) {
        source.subscribe(new MaterializeSingleObserver<T>(observer));
    }
}
