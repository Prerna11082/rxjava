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

package io.reactivex.tests.processors;

import io.reactivex.core.processors.FlowableProcessor;
import io.reactivex.core.processors.*;
import org.junit.Test;
import org.junit.*;
import static org.junit.Assert.*;

public abstract class FlowableProcessorTest<T> {

    protected abstract FlowableProcessor<T> create();

    @Ignore("Abstract")
        @Test
        public void onNextNull() {
        FlowableProcessor<T> p = create();

        try {
            p.onNext(null);
            fail("No NullPointerException thrown");
        } catch (NullPointerException ex) {
            assertEquals("onNext called with null. Null values are generally not allowed in 2.x operators and sources.", ex.getMessage());
        }

        p.test().assertEmpty().cancel();
    }

    @Ignore("Abstract")
    @Test
    public void onErrorNull() {
        FlowableProcessor<T> p = create();

        try {
            p.onError(null);
            fail("No NullPointerException thrown");
        } catch (NullPointerException ex) {
            assertEquals("onError called with null. Null values are generally not allowed in 2.x operators and sources.", ex.getMessage());
        }

        p.test().assertEmpty().cancel();
    }
}
