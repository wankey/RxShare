package io.github.wankey.mithril.share.handler

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.github.wankey.mithril.share.config.SocialConfig
import io.github.wankey.mithril.share.config.SocialMedia
import io.github.wankey.mithril.share.model.ShareImage
import io.github.wankey.mithril.share.model.ShareModel
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import top.zibin.luban.Luban
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 *
 *
 * @author wankey
 *
 * create on 2018/7/24
 *
 */
abstract class ShareHandler(val activity: Activity) {

    abstract fun isInstall(): Boolean

    abstract fun share(target: SocialMedia, model: ShareModel)

    abstract fun handleResult(data: Intent?)

    abstract fun release()

    fun prepareImage(image: ShareImage): Observable<File> {
        return Observable.just(image)
                .switchMap(Function<ShareImage, Observable<File>> {
                    if (it.bitmap != null) {
                        val file = File(SocialConfig.instance.cacheFile, "${System.currentTimeMillis()}.png")
                        val os = BufferedOutputStream(FileOutputStream(file))
                        it.bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, os)
                        return@Function Observable.just(file)
                    } else if (it.imageRes != 0) {
                        val bitmap = it.imageRes?.let { it1 -> BitmapFactory.decodeResource(activity.resources, it1) }
                        val file = File(SocialConfig.instance.cacheFile, "${System.currentTimeMillis()}.png")
                        val os = BufferedOutputStream(FileOutputStream(file))
                        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, os)
                        return@Function Observable.just(file)
                    } else if (it.file != null && it.file!!.exists()) {
                        return@Function Observable.just(it.file)
                    } else if (!it.url.isNullOrEmpty()) {
                        Observable.just(it.url).map {
                            val file = File(SocialConfig.instance.cacheFile, "${System.currentTimeMillis()}.png")
                            val client = OkHttpClient()
                            val request = Request.Builder().url(it).build()
                            val response = client.newCall(request).execute()

                            if (response.isSuccessful) {
                                val sink = Okio.buffer(Okio.sink(file))
                                sink.writeAll(response.body()!!.source())
                                sink.close()
                                response.close()
                            }
                            return@map file
                        }
                    } else {
                        return@Function Observable.error(NullPointerException("image not found"))
                    }
                })
                .map {
                    return@map Luban.with(activity).load(it).setTargetDir(SocialConfig.instance.cacheFile?.absolutePath).get()[0]
                }
                .subscribeOn(Schedulers.io())
    }
}