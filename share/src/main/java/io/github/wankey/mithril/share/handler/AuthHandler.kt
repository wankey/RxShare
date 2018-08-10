package io.github.wankey.mithril.share.handler

import android.content.Intent
import io.github.wankey.mithril.share.config.SocialMedia

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
interface AuthHandler {

    fun isInstall(): Boolean

    fun login(target: SocialMedia)

    fun handleLoginResult(data: Intent?)

    fun release()

}