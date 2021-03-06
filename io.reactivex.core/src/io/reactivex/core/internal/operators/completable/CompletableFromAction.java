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

import io.reactivex.common.disposables.Disposable; import io.reactivex.core.*;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.functions.Action;
import io.reactivex.core.disposables.Disposables;

public final class CompletableFromAction extends Completable {

    final Action run;

    public CompletableFromAction(Action run) {
        this.run = run;
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        Disposable d = Disposables.empty();
        observer.onSubscribe(d);
        try {
            run.run();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            }
            return;
        }
        if (!d.isDisposed()) {
            observer.onComplete();
        }
    }

}
