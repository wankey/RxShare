package io.github.wankey.mithril.share.handler.wechat

import android.app.Activity
import android.content.Intent
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import io.github.wankey.mithril.share.R
import io.github.wankey.mithril.share.R.string
import io.github.wankey.mithril.share.config.SocialConfig
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.handler.AuthHandler
import io.github.wankey.mithril.share.handler.ShareHandler
import io.github.wankey.mithril.share.model.AuthResult
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
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class WechatHandler(activity: Activity) : ShareHandler(activity), AuthHandler {

  private var api: IWXAPI = WXAPIFactory.createWXAPI(activity, SocialConfig.wxId, true)

  init {
    api.registerApp(SocialConfig.wxId)
  }

  override fun isInstall(): Boolean {
    return api.isWXAppInstalled
  }

  override fun share(target: SocialMedia, model: ShareModel) {
    if (!isInstall()) {
      BusUtils.default.post(ShareResult(ShareResult.ERROR, activity.getString(R.string.msg_wechat_client_not_found)))
      activity.finish()
      return
    }
    val dispose = Observable.just(model)
        .flatMap(Function<ShareModel, ObservableSource<WXMediaMessage>> {
          return@Function toWxMediaMessage(it)
        })
        .subscribeOn(Schedulers.io())
        .subscribe { sendMessage(target, it, buildTransaction(model)) }
  }

  override fun login(target: SocialMedia) {
    if (!isInstall()) {
      BusUtils.default.post(AuthResult(AuthResult.ERROR, activity.getString(string.msg_wechat_client_not_found)))
      activity.finish()
      return
    }

    val req = SendAuth.Req()
    req.scope = "snsapi_userinfo"
    req.state = "wx_login"
    val result = api.sendReq(req)
    if (!result) {
      BusUtils.default.post(AuthResult(AuthResult.ERROR, "sendReq checkArgs fail"))
    }
  }

  override fun handleLoginResult(data: Intent?) {
    api.handleIntent(data, object : IWXAPIEventHandler {
      override fun onResp(baseResp: BaseResp) {
        val result = when (baseResp.errCode) {
          BaseResp.ErrCode.ERR_OK -> {
            val authData = HashMap<String, String>()
            val sendResp = baseResp as SendAuth.Resp

            authData["code"] = sendResp.code
            authData["country"] = sendResp.country
            authData["lang"] = sendResp.lang
            authData["state"] = sendResp.state
            authData["url"] = sendResp.url

            AuthResult(AuthResult.OK, activity.getString(string.action_login_success), authData)
          }
          BaseResp.ErrCode.ERR_USER_CANCEL -> AuthResult(AuthResult.CANCEL,
              activity.getString(string.action_login_cancel))
          else -> AuthResult(AuthResult.ERROR, baseResp.errStr + ",code:" + baseResp.errCode)
        }
        BusUtils.default.post(result)
      }

      override fun onReq(baseResp: BaseReq) {
      }
    })
  }

  override fun handleShareResult(data: Intent?) {
    api.handleIntent(data, object : IWXAPIEventHandler {
      override fun onReq(baseReq: BaseReq) {}

      override fun onResp(baseResp: BaseResp) {
        val result = when (baseResp.errCode) {
          BaseResp.ErrCode.ERR_OK -> ShareResult(ShareResult.OK, activity.getString(R.string.action_share_success))
          BaseResp.ErrCode.ERR_USER_CANCEL -> ShareResult(ShareResult.CANCEL, activity.getString(R.string.action_share_cancel))
          else -> ShareResult(ShareResult.ERROR, baseResp.errStr + ",code:" + baseResp.errCode)
        }
        BusUtils.default.post(result)
      }
    })
  }

  private fun toWxMediaMessage(model: ShareModel): Observable<WXMediaMessage> {
    return Observable.just(model)
        .flatMap {
          val message = WXMediaMessage()
          when (it) {
            is ShareText -> {
              val textObject = WXTextObject(it.text)
              message.mediaObject = textObject
              message.title = it.text
              message.description = it.text
              Observable.just(message)
            }
            is ShareWeb -> {
              message.title = it.title
              message.description = it.description
              val webObject = WXWebpageObject()
              webObject.webpageUrl = it.target
              message.mediaObject = webObject
              Observable.zip(
                  Observable.just(message),
                  prepareImage(it.thumbnail),
                  BiFunction<WXMediaMessage, File, WXMediaMessage> { t1, t2 ->
                    t1.thumbData = t2.readBytes()
                    t1
                  }
              )
            }
            is ShareImage -> {
              Observable.zip(
                  Observable.just(message),
                  prepareImage(it),
                  BiFunction<WXMediaMessage, File, WXMediaMessage> { t1, t2 ->
                    val imageObject = WXImageObject(t2.readBytes())
                    message.mediaObject = imageObject
                    t1
                  }
              )
            }
          }
        }
        .subscribeOn(Schedulers.io())
  }

  private fun sendMessage(target: SocialMedia, mediaMessage: WXMediaMessage, transaction: String) {
    val req = SendMessageToWX.Req()
    req.transaction = transaction
    req.message = mediaMessage
    req.scene = if (target === SocialMedia.WECHAT_MOMENT)
      SendMessageToWX.Req.WXSceneTimeline
    else
      SendMessageToWX.Req.WXSceneSession
    val result = api.sendReq(req)
    if (!result) {
      BusUtils.default.post(ShareResult(ShareResult.ERROR, "sendReq checkArgs fail"))
      activity.finish()
    }
  }

  private fun buildTransaction(model: ShareModel): String {
    val type: String = when (model) {
      is ShareText -> {
        "text"
      }
      is ShareImage -> {
        "image"
      }
      is ShareWeb -> {
        "web"
      }
    }
    return String.format("%d%s", System.currentTimeMillis(), type)
  }

  override fun release() {
    api.detach()
  }
}