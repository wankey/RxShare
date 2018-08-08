package io.github.wankey.mithril.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sina.weibo.sdk.statistic.LogBuilder.KEY_TYPE

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
class ShareActivity : Activity() {
    private val KEY_TYPE = "type"
    private var isNew: Boolean = false
    private var fromUser: Boolean = false
    private lateinit var type: String

    companion object {
        const val KEY_IS_FROM_USER = "from_user"
        const val TYPE_SHARE = "share"
        const val TYPE_LOGIN = "login"
        fun createIntent(context: Context, type: String): Intent {
            val intent = Intent(context, ShareActivity::class.java)
            intent.putExtra(KEY_TYPE, type)
            intent.putExtra(KEY_IS_FROM_USER, true)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromUser = intent.getBooleanExtra(KEY_IS_FROM_USER, false)
        isNew = true
        type = intent.getStringExtra(KEY_TYPE)
        if (fromUser) {
            if (type == TYPE_SHARE) {
                RxShare.instance.action(this)
            } else if (type == TYPE_LOGIN) {
                RxAuth.INSTANCE.action(this)
            }
        } else {
            if (type == TYPE_SHARE) {
                RxShare.instance.handleResult(intent)
            } else {
                RxAuth.INSTANCE.handleResult(intent)
            }

            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isNew) {
            isNew = false
        } else {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (type == TYPE_SHARE) {
            RxShare.instance.handleResult(intent)
        } else {
            RxAuth.INSTANCE.handleResult(intent)
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (type == TYPE_SHARE) {
            RxShare.instance.handleResult(intent)
        } else {
            RxAuth.INSTANCE.handleResult(intent)
        }
        finish()
    }
}