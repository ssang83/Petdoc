package kr.co.petdoc.petdoc.utils

import android.graphics.*
import android.os.FileUtils
import android.util.Base64
import kr.co.petdoc.petdoc.log.Logger
import java.io.*

/**
 * Petdoc
 * Class: BitmapManager
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
object BitmapManager {

    fun drawString(
        bitmap: Bitmap?,
        text: String,
        x: Int,
        y: Int,
        lineHeight: Int
    ): Bitmap? {
        var y = y
        val canvas = Canvas(bitmap!!)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = 12f
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        val texts = text.split("\n".toRegex()).toTypedArray()
        for (line in texts) {
            canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
            y += lineHeight
        }
        return bitmap
    }

    fun base64ToBitmap(base64: String?): Bitmap? {
        val decodedString =
            Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    fun bitmapToBase64(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun byteToBitmap(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun bitmapToByte(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun fileToByte(file: File) : ByteArray {
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            Logger.p(e)
        } catch (e:IOException) {
            Logger.p(e)
        }

        return bytes
    }
}