module io.reactivex.common {
    exports io.reactivex.common.annotations; //to io.reactivex.core;
    exports io.reactivex.common.disposables ;//to io.reactivex.core;
    exports io.reactivex.common.exceptions;// to io.reactivex.core;
    exports io.reactivex.common.functions ;//to io.reactivex.core;
    exports io.reactivex.common.internal.disposables;// to io.reactivex.core;
    exports io.reactivex.common.internal.functions ;//to io.reactivex.core;
    exports io.reactivex.common.internal.util ;//to io.reactivex.core;

    requires org.reactivestreams.tck;
    requires org.reactivestreams;
}