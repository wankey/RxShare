package io.github.wankey.mithril.share.model

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/28
 *
 */
data class ShareResult(val code: Int, val msg: String) {
  companion object {
    const val OK = 1
    const val ERROR = -1
    const val CANCEL = -2
  }
}