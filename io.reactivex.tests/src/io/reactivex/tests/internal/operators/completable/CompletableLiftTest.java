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

import org.junit.Test;

import io.reactivex.core.*; import io.reactivex.tests.*;
import io.reactivex.tests.exceptions.TestException;
import io.reactivex.core.plugins.RxJavaPlugins;

public class CompletableLiftTest {

    @Test
    public void callbackThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable.complete()
            .lift(new CompletableOperator() {
                @Override
                public CompletableObserver apply(CompletableObserver o) throws Exception {
                    throw new TestException();
                }
            })
            .test();

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
