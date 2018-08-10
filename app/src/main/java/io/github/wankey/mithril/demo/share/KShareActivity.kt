package io.github.wankey.mithril.demo.share

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.PermissionChecker
import io.github.wankey.mithril.share.RxShare
import io.github.wankey.mithril.share.config.SocialMedia.QQ
import io.github.wankey.mithril.share.config.SocialMedia.QZONE
import io.github.wankey.mithril.share.config.SocialMedia.WECHAT
import io.github.wankey.mithril.share.config.SocialMedia.WECHAT_MOMENT
import io.github.wankey.mithril.share.config.SocialMedia.WEIBO
import io.github.wankey.mithril.share.model.ShareImage
import io.github.wankey.mithril.share.model.ShareModel
import io.github.wankey.mithril.share.model.ShareText
import io.github.wankey.mithril.share.model.ShareWeb
import kotlinx.android.synthetic.main.activity_share.btn_share
import kotlinx.android.synthetic.main.activity_share.rg_media
import kotlinx.android.synthetic.main.activity_share.rg_source
import kotlinx.android.synthetic.main.activity_share.rg_type

/**
 * @author wankey
 */
class KShareActivity : Activity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_share)

    btn_share.setOnClickListener {
      val result = PermissionChecker.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
      if (result == PermissionChecker.PERMISSION_GRANTED) {
        share()
      }
    }

    rg_type.setOnCheckedChangeListener { _, id ->
      run {
        when (id) {
          R.id.rb_image -> {
            rg_source.visibility = View.VISIBLE
          }
          else -> {
            rg_source.visibility = View.GONE
          }
        }
      }
    }

  }

  private fun share() {
    val model = prepareModel()
    val result = when (rg_media.checkedRadioButtonId) {
      R.id.rb_wx -> RxShare.instance.share(this, WECHAT, model)
      R.id.rb_circle -> RxShare.instance.share(this, WECHAT_MOMENT, model)
      R.id.rb_qq -> RxShare.instance.share(this, QQ, model)
      R.id.rb_qzone -> RxShare.instance.share(this, QZONE, model)
      R.id.rb_wb -> RxShare.instance.share(this, WEIBO, model)
      else -> null
    }
    val disponse = result?.subscribe(
        { Toast.makeText(this, it.msg, Toast.LENGTH_SHORT).show() },
        { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() })
  }

  private fun prepareModel(): ShareModel = when (rg_type.checkedRadioButtonId) {
    R.id.rb_text -> ShareText("这是一条纯文本分享")
    R.id.rb_image -> prepareImage(false)
    R.id.rb_web -> ShareWeb("RxShare", "使用RxJava代码风格的分享lib，支持分享到新浪微博、qq、qq空间、微信、微信朋友圈", "https://github.com/wankey/RxShare", prepareImage(true))
    else -> ShareText("这是一条纯文本分享")
  }

  private fun prepareImage(isThumb: Boolean): ShareImage {
    return if (!isThumb) {
      ShareImage("http://g.hiphotos.baidu.com/image/pic/item/0df3d7ca7bcb0a468c3807fd6763f6246a60afd8.jpg", isThumb)
    } else {
      ShareImage(R.drawable.ic_launcher, isThumb)
    }
  }

}