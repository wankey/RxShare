package io.github.wankey.mithril.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.github.wankey.mithril.demo.login.KLoginActivity
import io.github.wankey.mithril.demo.login.LoginActivity
import io.github.wankey.mithril.demo.share.KShareActivity
import io.github.wankey.mithril.demo.share.R.layout
import io.github.wankey.mithril.demo.share.ShareActivity
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_main)
    setSupportActionBar(toolbar)

    }

  fun shareKotlin(v: View) {
    val intent = Intent(this, KShareActivity::class.java)
    startActivity(intent)
  }

  fun shareJava(v: View) {
    val intent = Intent(this, ShareActivity::class.java)
    startActivity(intent)
  }

  fun loginKotlin(v: View) {
    val intent = Intent(this, KLoginActivity::class.java)
    startActivity(intent)
  }

  fun loginJava(v: View) {
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
  }
}
