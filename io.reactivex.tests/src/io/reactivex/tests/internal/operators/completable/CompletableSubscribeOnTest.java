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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.reactivex.core.*; import io.reactivex.tests.*;
import io.reactivex.common.functions.Function;
import io.reactivex.core.observers.TestObserver;
import io.reactivex.core.plugins.RxJavaPlugins;
import io.reactivex.core.schedulers.*;
import io.reactivex.core.subjects.PublishSubject;

public class CompletableSubscribeOnTest {

    @Test
    public void normal() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();

            TestObserver<Void> to = Completable.complete()
            .subscribeOn(scheduler)
            .test();

            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

            to.assertResult();

            assertTrue(list.toString(), list.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().ignoreElements().subscribeOn(new TestScheduler()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(new Function<Completable, CompletableSource>() {
            @Override
            public CompletableSource apply(Completable c) throws Exception {
                return c.subscribeOn(Schedulers.single());
            }
        });
    }
}
