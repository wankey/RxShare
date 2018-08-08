package io.github.wankey.mithril.share

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/28
 *
 */
data class AuthResult(val code: Int, val msg: String?, val data: HashMap<String, String>? = HashMap(2)) {
    companion object {
        const val OK = 1
        const val ERROR = -1
        const val CANCEL = -2
    }
}