package io.github.wankey.mithril.demo.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.wankey.mithril.demo.share.R
import io.github.wankey.mithril.share.RxAuth
import io.github.wankey.mithril.share.config.SocialMedia.QQ
import io.github.wankey.mithril.share.config.SocialMedia.WECHAT
import io.github.wankey.mithril.share.model.AuthResult
import kotlinx.android.synthetic.main.activity_login.btn_qq
import kotlinx.android.synthetic.main.activity_login.btn_wx

/**
 * @author wankey
 */
class KLoginActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    btn_wx.setOnClickListener { loginWithWx() }
    btn_qq.setOnClickListener { loginWithQq() }
  }

  private fun loginWithQq() {
    val d = RxAuth.INSTANCE.login(this, QQ)
        .subscribe({
          when (it.code) {
            AuthResult.OK -> {
              Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
              printAuthInfo(it.data)
            }
            else -> {
              Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
            }

          }
        }, {
          Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })
  }

  private fun printAuthInfo(data: HashMap<String, String>?) {
    if (data == null) {
      return
    }

    for ((key, value) in data) {
      Log.i("Auth Info", "$key:$value")
    }
  }

  private fun loginWithWx() {
    val d = RxAuth.INSTANCE.login(this, WECHAT)
        .subscribe({
          when (it.code) {
            AuthResult.OK -> {
              printAuthInfo(it.data)
            }
            else -> {
              Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show()
            }

          }
        }, {
          Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })
  }
}