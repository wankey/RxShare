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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
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
            val file = File(SocialConfig.cacheFile, "${System.currentTimeMillis()}.png")
            val os = BufferedOutputStream(FileOutputStream(file))
            it.bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
            return@Function Observable.just(file)
          } else if (it.imageRes != 0) {
            val bitmap = BitmapFactory.decodeResource(activity.applicationContext.resources, it.imageRes)
            val file = File(SocialConfig.cacheFile, "${System.currentTimeMillis()}.png")
            val os = BufferedOutputStream(FileOutputStream(file))
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
            return@Function Observable.just(file)
          } else if (it.file != null && it.file!!.exists()) {
            return@Function Observable.just(it.file)
          } else if (!it.url.isNullOrEmpty()) {
            Observable.just(it.url).map {
              val file = File(SocialConfig.cacheFile, "${System.currentTimeMillis()}.png")
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
        .switchMap {
          if (isImage(FileInputStream(it).readBytes())) {
            Observable.just(it)
          } else {
            Observable.error(NullPointerException("非法的图片格式"))
          }
        }.map {
          if (image.isThumb) {
            val file = File(SocialConfig.cacheFile, "${System.currentTimeMillis()}.png")
            val os = BufferedOutputStream(FileOutputStream(file))
            os.write(compress2Byte(it.absolutePath, 120, 32768))
            os.flush()
            os.close()

            return@map file
          } else {
            return@map Luban.with(activity).load(it).setTargetDir(SocialConfig.cacheFile?.absolutePath).get()[0]
          }
        }
        .subscribeOn(Schedulers.io())
  }

  fun compress2Byte(imagePath: String, size: Int, length: Int): ByteArray {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(imagePath, options)

    val outH = options.outHeight
    val outW = options.outWidth
    var inSampleSize = 1

    while (outH / inSampleSize > size || outW / inSampleSize > size) {
      inSampleSize *= 2
    }

    options.inSampleSize = inSampleSize
    options.inJustDecodeBounds = false

    val bitmap = BitmapFactory.decodeFile(imagePath, options)

    val result = ByteArrayOutputStream()
    var quality = 100
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
    if (result.size() > length) {
      result.reset()
      quality -= 10
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, result)
    }

    bitmap.recycle()
    return result.toByteArray()
  }

  private fun isImage(bytes: ByteArray): Boolean {
    if (isJPEG(bytes)) return true
    if (isGIF(bytes)) return true
    if (isPNG(bytes)) return true
    if (isBMP(bytes)) return true
    return false
  }

  private fun isJPEG(b: ByteArray): Boolean {
    return (b.size >= 2
        && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte())
  }

  private fun isGIF(b: ByteArray): Boolean {
    return (b.size >= 6
        && b[0] == 'G'.toByte() && b[1] == 'I'.toByte()
        && b[2] == 'F'.toByte() && b[3] == '8'.toByte()
        && (b[4] == '7'.toByte() || b[4] == '9'.toByte()) && b[5] == 'a'.toByte())
  }

  private fun isPNG(b: ByteArray): Boolean {
    return b.size >= 8 && (b[0] == 137.toByte() && b[1] == 80.toByte()
        && b[2] == 78.toByte() && b[3] == 71.toByte()
        && b[4] == 13.toByte() && b[5] == 10.toByte()
        && b[6] == 26.toByte() && b[7] == 10.toByte())
  }

  private fun isBMP(b: ByteArray): Boolean {
    return (b.size >= 2
        && b[0].toInt() == 0x42 && b[1].toInt() == 0x4d)
  }
}