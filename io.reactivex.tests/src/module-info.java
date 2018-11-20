/**
 * 
 */
/**
 * @author shamalip
 *
 */
module io.reactivex.tests {
	exports io.reactivex.tests;
	exports io.reactivex.tests.disposables;
	exports io.reactivex.tests.subjects;

	requires io.reactivex.common;
	requires io.reactivex.core;
	requires java.management;
	requires mockito.core;
	requires org.reactivestreams;
	requires org.reactivestreams.tck;
	requires junit;
}