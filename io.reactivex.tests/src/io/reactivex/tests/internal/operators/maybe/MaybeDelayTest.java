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

package io.reactivex.tests.internal.operators.maybe;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import io.reactivex.tests.TestHelper;
import org.junit.Test;

import io.reactivex.core.*;
import io.reactivex.tests.exceptions.TestException;
import io.reactivex.common.functions.Function;
import io.reactivex.core.observers.TestObserver;
import io.reactivex.core.processors.PublishProcessor;
import io.reactivex.core.schedulers.TestScheduler;

public class MaybeDelayTest {

    @Test
    public void success() {
        Maybe.just(1).delay(100, TimeUnit.MILLISECONDS)
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(1);
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).delay(100, TimeUnit.MILLISECONDS)
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertFailure(TestException.class);
    }

    @Test
    public void complete() {
        Maybe.empty().delay(100, TimeUnit.MILLISECONDS)
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult();
    }

    @Test(expected = NullPointerException.class)
    public void nullUnit() {
        Maybe.just(1).delay(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullScheduler() {
        Maybe.just(1).delay(1, TimeUnit.MILLISECONDS, null);
    }

    @Test
    public void disposeDuringDelay() {
        TestScheduler scheduler = new TestScheduler();

        TestObserver<Integer> to = Maybe.just(1).delay(100, TimeUnit.MILLISECONDS, scheduler)
        .test();

        to.cancel();

        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);

        to.assertEmpty();
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestObserver<Integer> to = pp.singleElement().delay(100, TimeUnit.MILLISECONDS).test();

        assertTrue(pp.hasSubscribers());

        to.cancel();

        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestHelper.checkDisposed(pp.singleElement().delay(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {
            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.delay(100, TimeUnit.MILLISECONDS);
            }
        });
    }
}
