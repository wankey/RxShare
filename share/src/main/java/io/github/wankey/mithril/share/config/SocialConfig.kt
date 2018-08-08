package io.github.wankey.mithril.share.config

import java.io.File

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class SocialConfig private constructor() {
    companion object {
        val instance: SocialConfig by lazy { SocialConfig() }
    }

    var wxId: String? = null
        private set

    var wxSecret: String? = null
        private set

    var qqId: String? = null
        private set

    var weiboId: String? = null
        private set

    var weiboRedirectUrl = "https://api.weibo.com/oauth2/default.html"
        private set

    var weiboScope = ("email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write")
        private set

    var isDebug: Boolean = false
        private set

    var cacheFile: File? = null
        private set

    fun wxId(id: String): SocialConfig {
        wxId = id
        return this
    }

    fun wxSecret(id: String): SocialConfig {
        wxSecret = id
        return this
    }

    fun qqId(id: String): SocialConfig {
        qqId = id
        return this
    }

    fun weiboId(id: String): SocialConfig {
        weiboId = id
        return this
    }

    fun weiboRedirectUrl(url: String): SocialConfig {
        weiboRedirectUrl = url
        return this
    }

    fun weiboScope(scope: String): SocialConfig {
        weiboScope = scope
        return this
    }

    fun debug(isDebug: Boolean): SocialConfig {
        this.isDebug = isDebug
        return this
    }

    fun tmpPath(file: File): SocialConfig {
        cacheFile = file
        return this
    }
}