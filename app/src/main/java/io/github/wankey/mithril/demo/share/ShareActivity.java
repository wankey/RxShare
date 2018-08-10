package io.github.wankey.mithril.demo.share;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.core.content.PermissionChecker;
import io.github.wankey.mithril.share.RxShare;
import io.github.wankey.mithril.share.config.SocialMedia;
import io.github.wankey.mithril.share.model.ShareImage;
import io.github.wankey.mithril.share.model.ShareModel;
import io.github.wankey.mithril.share.model.ShareResult;
import io.github.wankey.mithril.share.model.ShareText;
import io.github.wankey.mithril.share.model.ShareWeb;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author wankey
 */
public class ShareActivity extends Activity {
  private RadioGroup rgType;
  private RadioGroup rgSource;
  private RadioGroup rgMedia;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_share);

    rgType = findViewById(R.id.rg_type);
    rgSource = findViewById(R.id.rg_source);
    rgMedia = findViewById(R.id.rg_media);
    rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (id == R.id.rb_image) {
          rgSource.setVisibility(View.VISIBLE);
        } else {
          rgSource.setVisibility(View.GONE);
        }
      }
    });
    findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        int result = PermissionChecker.checkSelfPermission(ShareActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PermissionChecker.PERMISSION_GRANTED) {
          share();
        }
      }
    });
  }

  private void share() {
    ShareModel model = prepareModel();
    Observable<ShareResult> observable = null;
    switch (rgMedia.getCheckedRadioButtonId()) {
      case R.id.rb_wx:
        observable = RxShare.Companion.getInstance().share(this, SocialMedia.WECHAT, model);
        break;
      case R.id.rb_circle:
        observable = RxShare.Companion.getInstance().share(this, SocialMedia.WECHAT_MOMENT, model);
        break;
      case R.id.rb_qq:
        observable = RxShare.Companion.getInstance().share(this, SocialMedia.QQ, model);
        break;
      case R.id.rb_qzone:
        observable = RxShare.Companion.getInstance().share(this, SocialMedia.QZONE, model);
        break;
      case R.id.rb_wb:
        RxShare.Companion.getInstance().share(this, SocialMedia.WEIBO, model);
        break;
      default:
    }
    if (observable != null) {
      Disposable disposable = observable.subscribe(new Consumer<ShareResult>() {
        @Override public void accept(ShareResult shareResult) {
          Toast.makeText(ShareActivity.this, shareResult.getMsg(), Toast.LENGTH_SHORT).show();
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(Throwable throwable) {
          Toast.makeText(ShareActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  private ShareModel prepareModel() {
    switch (rgType.getCheckedRadioButtonId()) {
      case R.id.rb_text:
        return new ShareText("这是一条纯文本分享");
      case R.id.rb_image:
        return prepareImage(false);
      case R.id.rb_web:
        return new ShareWeb("RxShare", "使用RxJava代码风格的分享lib，支持分享到新浪微博、qq、qq空间、微信、微信朋友圈", "https://github.com/wankey/RxShare", prepareImage(true));
      default:
        return new ShareText("这是一条纯文本分享");
    }
  }

  private ShareImage prepareImage(Boolean isThumb) {
    if (!isThumb) {
      return new ShareImage("http://g.hiphotos.baidu.com/image/pic/item/0df3d7ca7bcb0a468c3807fd6763f6246a60afd8.jpg", isThumb);
    } else {
      return new ShareImage(R.drawable.ic_launcher, isThumb);
    }
  }
}
