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
object SocialConfig {

  var wxId: String? = null

  var wxSecret: String? = null

  var qqId: String? = null
  var weiboId: String? = null

  var weiboRedirectUrl = "https://api.weibo.com/oauth2/default.html"

  var weiboScope = ("email,direct_messages_read,direct_messages_write,"
      + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
      + "follow_app_official_microblog," + "invitation_write")

  var isDebug: Boolean = false

  var cacheFile: File? = null

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

  fun cacheFile(file: File): SocialConfig {
    cacheFile = file
    return this
  }
}