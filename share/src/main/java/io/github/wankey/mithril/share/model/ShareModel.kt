package io.github.wankey.mithril.share.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
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
data class ShareImage(var url: String? = null, @DrawableRes var imageRes: Int = 0, var bitmap: Bitmap? = null, var file: File? = null, var isThumb: Boolean = false) : ShareModel() {
  /**
   * 网络图片的构造函数
   * @param url 图片地址
   * @param isThumb 是否是缩略图
   */
  constructor(url: String, isThumb: Boolean = false) : this(url, 0, null, null, isThumb)

  /**
   * 资源图片的构造函数
   * @param url 图片地址
   * @param isThumb 是否是缩略图
   */
  constructor(@DrawableRes imageRes: Int, isThumb: Boolean = false) : this(null, imageRes, null, null, isThumb)

  /**
   * Bitmap的构造函数
   * @param bitmap bitmap对象
   * @param isThumb 是否是缩略图
   */
  constructor(bitmap: Bitmap, isThumb: Boolean = false) : this(null, 0, bitmap, null, isThumb)

  /**
   * 本地文件的构造函数
   * @param file 本地图片文件
   * @param isThumb 是否是缩略图
   */
  constructor(file: File, isThumb: Boolean = false) : this(null, 0, null, file, isThumb)
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