package io.github.wankey.mithril.share.util

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject


/**
 * @author wankey
 */
object RxBus {

    private val publisher = PublishSubject.create<Any>().toSerialized()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun throwable(throwable: Throwable) {
        publisher.onError(throwable)
        publisher.onComplete()
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Flowable<T> = publisher.toFlowable(BackpressureStrategy.DROP).ofType(eventType)
}