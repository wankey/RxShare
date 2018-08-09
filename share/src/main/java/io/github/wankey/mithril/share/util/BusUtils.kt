package io.github.wankey.mithril.share.util

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.HashMap

/**
 * @author wankey
 */
class BusUtils {

  private var mSubscriptionMap: HashMap<String, CompositeDisposable>? = null
  private val mSubject: Subject<Any> = PublishSubject.create<Any>().toSerialized()

  fun post(o: Any) {
    mSubject.onNext(o)
  }

  fun error(e: Throwable) {
    mSubject.onError(e)
    mSubject.onComplete()
  }

  /**
   * 返回指定类型的带背压的Flowable实例
   *
   * @param <T>
   * @param type
   * @return
  </T> */
  private fun <T> toObservable(type: Class<T>): Flowable<T> {
    return mSubject.toFlowable(BackpressureStrategy.DROP).ofType(type)
  }

  /**
   * 一个默认的订阅方法
   *
   * @param <T>
   * @param type
   * @param next
   * @param error
   * @return
  </T> */
  fun <T> doSubscribe(type: Class<T>, next: Consumer<T>,
      error: Consumer<Throwable> = Consumer { },
      action: Action = Action { }): Disposable {
    return toObservable(type).subscribeOn(Schedulers.io()).subscribe(next, error, action)
  }

  /**
   * 是否已有观察者订阅
   *
   * @return
   */
  fun hasObservers(): Boolean {
    return mSubject.hasObservers()
  }

  /**
   * 保存订阅后的disposable
   *
   * @param o
   * @param disposable
   */
  fun addSubscription(o: Any, disposable: Disposable) {
    if (mSubscriptionMap == null) {
      mSubscriptionMap = HashMap()
    }
    val key = o.javaClass.name
    if (mSubscriptionMap!![key] != null) {
      mSubscriptionMap!![key]?.add(disposable)
    } else {
      //一次性容器,可以持有多个并提供 添加和移除。
      val disposables = CompositeDisposable()
      disposables.add(disposable)
      mSubscriptionMap!![key] = disposables
    }
  }

  /**
   * 取消订阅
   *
   * @param o
   */
  fun unSubscribe(o: Any) {
    if (mSubscriptionMap == null) {
      return
    }

    val key = o.javaClass.name
    if (!mSubscriptionMap!!.containsKey(key)) {
      return
    }
    if (mSubscriptionMap!![key] != null) {
      mSubscriptionMap!![key]?.dispose()
    }

    mSubscriptionMap!!.remove(key)
  }

  companion object {
    val default: BusUtils = BusUtils()
  }

}