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

package io.reactivex.tests.internal.schedulers;

import static io.reactivex.core.Flowable.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.common.disposables.Disposable;
import io.reactivex.core.internal.schedulers.SchedulerWhen;
import io.reactivex.tests.TestHelper;
import org.junit.Test;

import io.reactivex.core.*;
import io.reactivex.core.Scheduler.Worker;
import io.reactivex.core.disposables.*;
import io.reactivex.tests.exceptions.TestException;
import io.reactivex.common.functions.Function;
import io.reactivex.core.internal.schedulers.SchedulerWhen.*;
import io.reactivex.core.observers.DisposableCompletableObserver;
import io.reactivex.core.processors.PublishProcessor;
import io.reactivex.core.schedulers.*;
import io.reactivex.core.subscribers.TestSubscriber;

public class SchedulerWhenTest {
    @Test
    public void testAsyncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        asyncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testAsyncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        asyncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testSyncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        syncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);

        // since all the work is synchronous nothing is blocked and its all done
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    @Test
    public void testSyncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();

        syncWork(sched).subscribe(tSub);

        tSub.assertValueCount(0);

        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(1);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);

        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();

        sched.dispose();
    }

    private Flowable<Long> asyncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {
            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.timer(1, SECONDS, sched);
            }
        });
    }

    private Flowable<Long> syncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {
            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.defer(new Callable<Flowable<Long>>() {
                    @Override
                    public Flowable<Long> call() {
                        return Flowable.just(0l);
                    }
                }).subscribeOn(sched);
            }
        });
    }

    private SchedulerWhen maxConcurrentScheduler(TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {
                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return Completable.concat(actions);
                    }
                });
                return Completable.merge(workers, 2);
            }
        }, tSched);
        return sched;
    }

    private SchedulerWhen throttleScheduler(final TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {
                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return Completable.concat(actions);
                    }
                });
                return Completable.concat(workers.map(new Function<Completable, Completable>() {
                    @Override
                    public Completable apply(Completable worker) {
                        return worker.delay(1, SECONDS, tSched);
                    }
                }));
            }
        }, tSched);
        return sched;
    }

    @Test(timeout = 1000)
    public void testRaceConditions() {
        Scheduler comp = Schedulers.computation();
        Scheduler limited = comp.when(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> t) {
                return Completable.merge(Flowable.merge(t, 10));
            }
        });

        merge(just(just(1).subscribeOn(limited).observeOn(comp)).repeat(1000)).blockingSubscribe();
    }

    @Test
    public void subscribedDisposable() {
        SchedulerWhen.SUBSCRIBED.dispose();
        assertFalse(SchedulerWhen.SUBSCRIBED.isDisposed());
    }

    @Test(expected = TestException.class)
    public void combineCrashInConstructor() {
        new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> v)
                    throws Exception {
                throw new TestException();
            }
        }, Schedulers.single());
    }

    @Test
    public void disposed() {
        SchedulerWhen sw = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {
            @Override
            public Completable apply(Flowable<Flowable<Completable>> v)
                    throws Exception {
                return Completable.never();
            }
        }, Schedulers.single());

        assertFalse(sw.isDisposed());

        sw.dispose();

        assertTrue(sw.isDisposed());
    }

    @Test
    public void scheduledActiondisposedSetRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final ScheduledAction sa = new ScheduledAction() {

                private static final long serialVersionUID = -672980251643733156L;

                @Override
                protected Disposable callActual(Worker actualWorker,
                                                CompletableObserver actionCompletable) {
                    return Disposables.empty();
                }

            };

            assertFalse(sa.isDisposed());

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    sa.dispose();
                }
            };

            TestHelper.race(r1, r1);

            assertTrue(sa.isDisposed());
        }
    }

    @Test
    public void scheduledActionStates() {
        final AtomicInteger count = new AtomicInteger();
        ScheduledAction sa = new ScheduledAction() {

            private static final long serialVersionUID = -672980251643733156L;

            @Override
            protected Disposable callActual(Worker actualWorker,
                    CompletableObserver actionCompletable) {
                count.incrementAndGet();
                return Disposables.empty();
            }

        };

        assertFalse(sa.isDisposed());

        sa.dispose();

        assertTrue(sa.isDisposed());

        sa.dispose();

        assertTrue(sa.isDisposed());

        // should not run when disposed
        sa.call(Schedulers.single().createWorker(), null);

        assertEquals(0, count.get());

        // should not run when already scheduled
        sa.set(Disposables.empty());

        sa.call(Schedulers.single().createWorker(), null);

        assertEquals(0, count.get());

        // disposed while in call
        sa = new ScheduledAction() {

            private static final long serialVersionUID = -672980251643733156L;

            @Override
            protected Disposable callActual(Worker actualWorker,
                    CompletableObserver actionCompletable) {
                count.incrementAndGet();
                dispose();
                return Disposables.empty();
            }

        };

        sa.call(Schedulers.single().createWorker(), null);

        assertEquals(1, count.get());
    }

    @Test
    public void onCompleteActionRunCrash() {
        final AtomicInteger count = new AtomicInteger();

        OnCompletedAction a = new OnCompletedAction(new Runnable() {
            @Override
            public void run() {
                throw new TestException();
            }
        }, new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                count.incrementAndGet();
            }

            @Override
            public void onError(Throwable e) {
                count.decrementAndGet();
                e.printStackTrace();
            }
        });

        try {
            a.run();
            fail("Should have thrown");
        } catch (TestException expected) {

        }

        assertEquals(1, count.get());
    }

    @Test
    public void queueWorkerDispose() {
        QueueWorker qw = new QueueWorker(PublishProcessor.<ScheduledAction>create(), Schedulers.single().createWorker());

        assertFalse(qw.isDisposed());

        qw.dispose();

        assertTrue(qw.isDisposed());

        qw.dispose();

        assertTrue(qw.isDisposed());
    }
}
