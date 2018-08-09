package io.github.wankey.mithril.share.handler.sina

import android.app.Activity
import android.content.Intent
import com.sina.weibo.sdk.WbSdk
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.share.WbShareCallback
import com.sina.weibo.sdk.share.WbShareHandler
import io.github.wankey.mithril.share.R
import io.github.wankey.mithril.share.config.SocialConfig
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.handler.ShareHandler
import io.github.wankey.mithril.share.model.ShareImage
import io.github.wankey.mithril.share.model.ShareModel
import io.github.wankey.mithril.share.model.ShareResult
import io.github.wankey.mithril.share.model.ShareText
import io.github.wankey.mithril.share.model.ShareWeb
import io.github.wankey.mithril.share.util.BusUtils
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class WeiboHandler(activity: Activity) : ShareHandler(activity) {
  private var api: WbShareHandler

  init {
    WbSdk.install(activity, AuthInfo(activity, SocialConfig.weiboId, SocialConfig.weiboRedirectUrl, SocialConfig.weiboScope))
    api = WbShareHandler(activity)
    api.registerApp()
  }

  override fun isInstall(): Boolean {
    return WbSdk.isWbInstall(activity)
  }

  override fun share(target: SocialMedia, model: ShareModel) {
    if (!isInstall()) {
      BusUtils.default.post(ShareResult(ShareResult.ERROR, activity.getString(R.string.msg_weibo_client_not_found)))
      activity.finish()
      return
    }
    val dispose = Observable.just(model)
        .flatMap(Function<ShareModel, ObservableSource<WeiboMultiMessage>> {
          return@Function toWeiboMultiMessage(it)
        })
        .subscribeOn(Schedulers.io())
        .subscribe { api.shareMessage(it, false) }
  }

  private fun toWeiboMultiMessage(model: ShareModel): ObservableSource<WeiboMultiMessage>? {
    return Observable.just(model)
        .flatMap {
          val message = WeiboMultiMessage()
          when (it) {
            is ShareText -> {
              val textObject = TextObject()
              textObject.text = it.text
              message.mediaObject = textObject
              Observable.just(message)
            }
            is ShareWeb -> {
              val webObject = WebpageObject()
              webObject.actionUrl = it.target
              webObject.title = it.title
              webObject.description = it.description
              message.mediaObject = webObject
              Observable.zip(
                  Observable.just(message),
                  prepareImage(it.thumbnail),
                  BiFunction<WeiboMultiMessage, File, WeiboMultiMessage> { t1, t2 ->
                    t1.mediaObject.thumbData = t2.readBytes()
                    return@BiFunction t1
                  }
              )
            }
            is ShareImage -> {
              Observable.zip(
                  Observable.just(message),
                  prepareImage(it),
                  BiFunction<WeiboMultiMessage, File, WeiboMultiMessage> { t1, t2 ->
                    val imageObject = ImageObject()
                    imageObject.imageData = t2.readBytes()
                    message.mediaObject = imageObject
                    t1
                  }
              )
            }
          }
        }
        .subscribeOn(Schedulers.io())
  }

  override fun handleResult(data: Intent?) {
    api.doResultIntent(data, object : WbShareCallback {
      override fun onWbShareSuccess() {
        BusUtils.default.post(ShareResult(ShareResult.OK, activity.getString(R.string.action_share_success)))
      }

      override fun onWbShareCancel() {
        BusUtils.default.post(ShareResult(ShareResult.CANCEL, activity.getString(R.string.action_share_cancel)))
      }

      override fun onWbShareFail() {
        BusUtils.default.post(ShareResult(ShareResult.ERROR, activity.getString(R.string.action_share_failure)))
      }
    })
  }

  override fun release() {

  }
}