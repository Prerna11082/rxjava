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

package io.reactivex.tests.internal.observers;

import static org.junit.Assert.*;

import java.util.List;

import io.reactivex.core.internal.observers.DisposableLambdaObserver;
import org.junit.Test;

import io.reactivex.tests.TestHelper;
import io.reactivex.core.disposables.Disposables;
import io.reactivex.tests.exceptions.TestException;
import io.reactivex.common.functions.Action;
import io.reactivex.core.internal.functions.Functions;
import io.reactivex.core.observers.TestObserver;
import io.reactivex.core.plugins.RxJavaPlugins;

public class DisposableLambdaObserverTest {

    @Test
    public void doubleOnSubscribe() {
        TestHelper.doubleOnSubscribe(new DisposableLambdaObserver<Integer>(
                new TestObserver<Integer>(), Functions.emptyConsumer(), Functions.EMPTY_ACTION
        ));
    }

    @Test
    public void disposeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            DisposableLambdaObserver<Integer> o = new DisposableLambdaObserver<Integer>(
                    new TestObserver<Integer>(), Functions.emptyConsumer(),
                    new Action() {
                        @Override
                        public void run() throws Exception {
                            throw new TestException();
                        }
                    }
            );

            o.onSubscribe(Disposables.empty());

            assertFalse(o.isDisposed());

            o.dispose();

            assertTrue(o.isDisposed());

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
