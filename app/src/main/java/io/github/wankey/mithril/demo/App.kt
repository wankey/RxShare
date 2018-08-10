package io.github.wankey.mithril.demo

import android.app.Application
import android.os.Environment
import io.github.wankey.mithril.demo.share.BuildConfig
import io.github.wankey.mithril.share.config.SocialConfig

/**
 * @author wankey
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    SocialConfig.weiboId(BuildConfig.WEIBO_ID).wxId(BuildConfig.WX_ID).qqId(BuildConfig.QQ_ID).cacheFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
  }
}