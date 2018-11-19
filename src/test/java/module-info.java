/**
 * 
 */
/**
 * @author shamalip
 *
 */
module rxjava {
	exports io.reactivex.xmapz;
	exports io.reactivex.tests;
	exports io.reactivex.parallel;
	exports io.reactivex;
	exports io.reactivex.tests.disposables;

	requires io.reactivex.common;
	requires io.reactivex.core;
	requires java.management;
	requires jmh.core;
	requires mockito.core;
	requires org.reactivestreams;
	requires org.reactivestreams.tck;
}