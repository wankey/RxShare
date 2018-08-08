package io.github.wankey.mithril.share.model

import android.graphics.Bitmap
import java.io.File

/**
 * 分享对象基类
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
sealed class ShareModel

/**
 * 分享图片
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
data class ShareImage(var url: String? = null, var imageRes: Int? = 0, var bitmap: Bitmap?, var file: File?, var isThumb: Boolean) : ShareModel() {
  constructor(isThumb: Boolean) : this(null, 0, null, null, isThumb)
}

/**
 * 分享纯文本
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
data class ShareText(val text: String) : ShareModel()

/**
 * 分享网页
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
data class ShareWeb(val title: String, val description: String?, val target: String, val thumbnail: ShareImage) : ShareModel()