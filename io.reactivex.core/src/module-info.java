module io.reactivex.core {

    requires io.reactivex.common;

    exports io.reactivex.core;
    exports io.reactivex.core.flowables;
    exports io.reactivex.core.internal.fuseable;
    exports io.reactivex.core.internal.observers;

    exports io.reactivex.core.internal.operators.completable;
    exports io.reactivex.core.internal.operators.flowable;
    exports io.reactivex.core.internal.operators.maybe;
    exports io.reactivex.core.internal.operators.mixed;
    exports io.reactivex.core.internal.operators.observable;
    exports io.reactivex.core.internal.operators.parallel;
    exports io.reactivex.core.internal.operators.single;

    exports io.reactivex.core.internal.queue;
    exports io.reactivex.core.internal.schedulers;
    exports io.reactivex.core.internal.subscribers;
    exports io.reactivex.core.internal.subscriptions;
    exports io.reactivex.core.internal.util;

    exports io.reactivex.core.observables;
    exports io.reactivex.core.observers;
    exports io.reactivex.core.parallel;
    exports io.reactivex.core.plugins;
    exports io.reactivex.core.processors;
    exports io.reactivex.core.schedulers;
    exports io.reactivex.core.subjects;
    exports io.reactivex.core.subscribers;
    exports io.reactivex.core.disposables;
    exports io.reactivex.core.internal.disposables;

    requires org.reactivestreams.tck;
    requires org.reactivestreams;
}