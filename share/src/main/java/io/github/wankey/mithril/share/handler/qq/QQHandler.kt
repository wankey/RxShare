package io.github.wankey.mithril.share.handler.qq

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzoneShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
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
import org.json.JSONObject
import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class QQHandler(activity: Activity) : ShareHandler(activity), AuthHandler {

  private var client: Tencent = Tencent.createInstance(SocialConfig.qqId, activity.applicationContext)

  private val shareListener: IUiListener = object : IUiListener {
    override fun onComplete(p0: Any?) {
      BusUtils.default.post(ShareResult(ShareResult.OK, "分享成功"))
    }

    override fun onCancel() {
      BusUtils.default.post(ShareResult(ShareResult.CANCEL, "取消分享"))
    }

    override fun onError(p0: UiError?) = when {
      p0?.errorMessage != null -> BusUtils.default.post(ShareResult(ShareResult.ERROR, p0.errorMessage))
      else -> BusUtils.default.post(ShareResult(ShareResult.ERROR, "分享失败"))
    }

  }

  private val loginListener: IUiListener = object : IUiListener {
    override fun onComplete(p0: Any?) {
      val obj = p0 as JSONObject
      val authData = HashMap<String, String>()
      authData["openid"] = obj.getString("openid")
      authData["access_token"] = obj.getString("access_token")
      BusUtils.default.post(AuthResult(AuthResult.OK, activity.getString(string.action_login_success), authData))
    }

    override fun onCancel() {
      BusUtils.default.post(AuthResult(AuthResult.CANCEL, activity.getString(string.action_login_cancel)))
    }

    override fun onError(p0: UiError?) = when {
      p0?.errorMessage != null -> BusUtils.default.post(AuthResult(AuthResult.ERROR, p0.errorMessage))
      else -> BusUtils.default.post(AuthResult(AuthResult.ERROR, activity.getString(string.action_login_failure)))
    }
  }

  override fun isInstall(): Boolean {
    return client.isQQInstalled(activity)
  }

  override fun login(target: SocialMedia) {
    client.login(activity, "all", loginListener)
  }

  override fun share(target: SocialMedia, model: ShareModel) {
    if (!isInstall()) {
      BusUtils.default.post(ShareResult(ShareResult.ERROR, activity.getString(R.string.msg_qq_client_not_found)))
      activity.finish()
      return
    }

    if (model is ShareText) {
      BusUtils.default.post(ShareResult(ShareResult.ERROR, activity.getString(R.string.msg_share_text_to_qq_not_support)))
      activity.finish()
      return
    }

    val dispose = Observable.just(model)
        .flatMap(Function<ShareModel, ObservableSource<Bundle>> {
          return@Function prepareBundle(it, target)
        })
        .subscribeOn(Schedulers.io())
        .subscribe {
          share(target, it)
        }
  }

  private fun share(target: SocialMedia, bundle: Bundle) {
    if (target == SocialMedia.QQ) {
      client.shareToQQ(activity, bundle, shareListener)
    } else if (target == SocialMedia.QZONE) {
      client.shareToQzone(activity, bundle, shareListener)
    }
  }

  private fun prepareBundle(model: ShareModel, target: SocialMedia): Observable<Bundle> {
    return Observable.just(model)
        .flatMap {
          val bundle = Bundle()
          when (it) {
            is ShareImage -> {
              Observable.zip(
                  Observable.just(bundle),
                  prepareImage(it),
                  BiFunction<Bundle, File, Bundle> { t1, t2 ->
                    if (target == SocialMedia.QQ) {
                      bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
                      bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, t2.absolutePath)
                      bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, t2.absolutePath)
                    } else if (target == SocialMedia.QZONE) {
                      bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE)
                      bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ArrayList<String>(Collections.singletonList(t2.absolutePath)))
                    }
                    t1
                  }
              )
            }
            is ShareWeb -> {
              Observable.zip(
                  Observable.just(bundle),
                  prepareImage(it.thumbnail),
                  BiFunction<Bundle, File, Bundle> { t1, t2 ->
                    if (target == SocialMedia.QQ) {
                      bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                      bundle.putString(QQShare.SHARE_TO_QQ_TITLE, it.title)
                      bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, it.description)
                      bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, it.target)
                      bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, t2.absolutePath)
                      bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, t2.absolutePath)
                    } else if (target == SocialMedia.QZONE) {
                      bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                      bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, it.title)
                      bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, it.description)
                      bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, it.target)
                      bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, ArrayList<String>(Collections.singletonList(t2.absolutePath)))
                    }
                    t1
                  }
              )
            }
            else -> {
              Observable.error(IllegalArgumentException(activity.getString(R.string.msg_share_text_to_qq_not_support)))
            }
          }
        }.observeOn(Schedulers.io())
  }

  override fun handleResult(data: Intent?) {
    Tencent.handleResultData(data, shareListener)
  }

  override fun release() {
    client.releaseResource()
  }
}
