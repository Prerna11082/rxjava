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

package io.reactivex.tests.internal.operators.completable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.tests.exceptions.TestException;
import org.junit.*;

import io.reactivex.core.*; import io.reactivex.tests.*;
import io.reactivex.core.disposables.*;
import io.reactivex.common.exceptions.*;
import io.reactivex.common.functions.*;
import io.reactivex.core.observers.TestObserver;
import io.reactivex.core.plugins.RxJavaPlugins;

public class CompletableDoOnTest {

    @Test
    public void successAcceptThrows() {
        Completable.complete().doOnEvent(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void errorAcceptThrows() {
        TestObserver<Void> to = Completable.error(new TestException("Outer")).doOnEvent(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        })
        .test()
        .assertFailure(CompositeException.class);

        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));

        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnDisposeCalled() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();

        assertFalse(atomicBoolean.get());

        Completable.complete()
            .doOnDispose(new Action() {
                @Override
                public void run() throws Exception {
                    atomicBoolean.set(true);
                }
            })
            .test()
            .assertResult()
            .dispose();

        assertTrue(atomicBoolean.get());
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable bs = Disposables.empty();

            new Completable() {
                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("First");
                }
            })
            .test()
            .assertFailureAndMessage(TestException.class, "First");

            assertTrue(bs.isDisposed());

            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
