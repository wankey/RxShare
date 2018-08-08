package io.github.wankey.mithril.share

import android.app.Activity
import android.content.Intent
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.handler.DefaultHandler
import io.github.wankey.mithril.share.handler.ShareHandler
import io.github.wankey.mithril.share.handler.qq.QQHandler
import io.github.wankey.mithril.share.handler.sina.WeiboHandler
import io.github.wankey.mithril.share.handler.wechat.WechatHandler
import io.github.wankey.mithril.share.model.ShareModel
import io.github.wankey.mithril.share.model.ShareResult
import io.github.wankey.mithril.share.util.BusUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.functions.Consumer

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class RxShare {

    private lateinit var target: SocialMedia
    private lateinit var model: ShareModel
    private lateinit var handler: ShareHandler

    fun share(activity: Activity, target: SocialMedia, model: ShareModel): Observable<String> {
        this.target = target
        this.model = model
        return Observable.create<String> { emitter: ObservableEmitter<String> ->
            if (emitter.isDisposed) {
                return@create
            }
            activity.startActivity(ShareActivity.createIntent(activity, ShareActivity.TYPE_SHARE))
            BusUtils.default.doSubscribe(ShareResult::class.java, next = Consumer {
                emitter.onNext(it.result)
            }, error = Consumer {
                emitter.onError(it)
                emitter.onComplete()
            })
        }

    }

    fun action(activity: Activity) {
        handler = when (target) {
            SocialMedia.QQ, SocialMedia.QZONE -> QQHandler(activity)
            SocialMedia.WECHAT, SocialMedia.WECHAT_MOMENT -> WechatHandler(activity)
            SocialMedia.WEIBO -> WeiboHandler(activity)
            SocialMedia.DEFAULT -> DefaultHandler(activity)
        }
        handler.share(target, model)
    }

    fun handleResult(data: Intent?) {
        handler.handleResult(data)

    }

    companion object {
        val instance: RxShare by lazy { RxShare() }

    }
}