package io.github.wankey.mithril.share.handler

import android.app.Activity
import android.content.Intent
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.model.ShareModel

/**
 * 系统默认分享
 *
 * @author wankey
 *
 * create on 2018/7/30
 *
 */
class DefaultHandler(activity: Activity) : ShareHandler(activity) {
    override fun isInstall(): Boolean {
        return true
    }

    override fun share(target: SocialMedia, model: ShareModel) {
    }

  override fun handleShareResult(data: Intent?) {
    }

    override fun release() {
    }
}