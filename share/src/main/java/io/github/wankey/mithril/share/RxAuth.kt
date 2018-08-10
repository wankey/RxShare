package io.github.wankey.mithril.share

import android.app.Activity
import android.content.Intent
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.handler.AuthHandler
import io.github.wankey.mithril.share.handler.qq.QQHandler
import io.github.wankey.mithril.share.handler.wechat.WechatHandler
import io.github.wankey.mithril.share.model.AuthResult
import io.github.wankey.mithril.share.util.BusUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.functions.Consumer


/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/28
 *
 */
class RxAuth {

    private lateinit var target: SocialMedia
    private lateinit var handler: AuthHandler
    fun login(activity: Activity, target: SocialMedia): Observable<AuthResult> {
        this.target = target
        return Observable.create<AuthResult> { emitter: ObservableEmitter<AuthResult> ->
            if (emitter.isDisposed) {
                return@create
            }
            activity.startActivity(SocialActivity.createIntent(activity, SocialActivity.TYPE_LOGIN))
            BusUtils.default.doSubscribe(AuthResult::class.java, Consumer {
                emitter.onNext(it)
            }, Consumer {
                emitter.onError(it)
                emitter.onComplete()
            })
        }
    }

    fun action(activity: Activity) {
        handler = when (target) {
            SocialMedia.QQ -> QQHandler(activity)
            SocialMedia.WECHAT -> WechatHandler(activity)
            else -> return BusUtils.default.post(IllegalArgumentException("unsupported action"))
        }
        handler.login(target)
    }

    fun handleResult(data: Intent?) {
        handler.handleLoginResult(data)
    }

    companion object {
        val INSTANCE: RxAuth by lazy { RxAuth() }
    }
}