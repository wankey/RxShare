 [![Download](https://api.bintray.com/packages/wankey/maven/share/images/download.svg) ](https://bintray.com/wankey/maven/share/_latestVersion)

#RxShare
使用RxJava代码风格的分享lib，支持分享到新浪微博、qq、qq空间、微信、微信朋友圈

##使用方式
1. 添加repository<br>
maven { url "https://dl.bintray.com/wankey/maven/" }<br>
//如需支持分享到新浪微博，请添加(option)<br>
maven { url "https://dl.bintray.com/thelasterstar/maven/" }<br>
2. 添加依赖<br>
implementation 'io.github.wankey.mithril:share:1.1.1'<br>
//如果需要分享到新浪微博，添加下面的依赖<br>
implementation 'com.sina.weibo.sdk:core:4.2.9:openDefaultRelease@aar'<br>
//如果需要分享到微信，添加下面的依赖<br>
implementation 'com.tencent.mm.opensdk:wechat-sdk-android-without-mta:5.1.4'<br>
//如果需要分享到qq，请将相关lib文件复制到libs目录下<br>
3. 在Application中初始化
4. (option)在AndroidManifest.xml中配置TENCENT_SCHEME<br>
```    <activity
        android:name="com.tencent.tauth.AuthActivity"
        android:noHistory="true">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data android:scheme="tencent123456" />
        </intent-filter>
    </activity>
```
5. 构造ShareModel,支持ShareImage、ShareText、ShareWeb 3种类型
6. 调用api<br>
6.1. 调用分享方法

```
RxShare.share(activity,SocialMedia.QQ,shareModel)
.subscribe(new Consumer<String>{
//输出分享结果
},new Consumer<Throwable>{})
```
6.2. 调用登录方法
```
RxAuth.login(activity,SocialMedia.WECHAT)
.subscribe(new Consumer<AuthResult>{
//判断AuthResult结果进行下一步操作
},new Consumer<Throwable>{})
```
