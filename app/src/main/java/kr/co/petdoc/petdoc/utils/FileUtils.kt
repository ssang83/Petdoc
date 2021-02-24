package kr.co.petdoc.petdoc.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.log.Logger
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: FileUtils
 * Created by kimjoonsung on 2020/05/22.
 *
 * Description :
 */
suspend fun Activity.compressImageFile(
        path: String,
        shouldOverride: Boolean = true,
        uri: Uri
): String {
    return withContext(Dispatchers.IO) {
        var scaledBitmap: Bitmap? = null

        try {
            val (hgt, wdt) = getImageHgtWdt(uri)
            try {
                val bm = getBitmapFromUri(uri)
                Logger.d("original bitmap height${bm?.height} width${bm?.width}")
                Logger.d("Dynamic height$hgt width$wdt")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Part 1: Decode image
            val unscaledBitmap = decodeFile(this@compressImageFile, uri, wdt, hgt, ScalingLogic.FIT)
            if (unscaledBitmap != null) {
                if (!(unscaledBitmap.width <= 800 && unscaledBitmap.height <= 800)) {
                    // Part 2: Scale image
                    scaledBitmap = createScaledBitmap(unscaledBitmap, wdt, hgt, ScalingLogic.FIT)
                } else {
                    scaledBitmap = unscaledBitmap
                }
            }


            // Store to tmp file
            val mFolder = File("$filesDir/Images")
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }

            val tmpFile = File(mFolder.absolutePath, "IMG_${getTimestampString()}.png")

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                scaledBitmap?.compress(
                        Bitmap.CompressFormat.PNG,
                        getImageQualityPercent(tmpFile),
                        fos
                )
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            var compressedPath = ""
            if (tmpFile.exists() && tmpFile.length() > 0) {
                compressedPath = tmpFile.absolutePath
                if (shouldOverride) {
                    val srcFile = File(path)
                    val result = tmpFile.copyTo(srcFile, true)
                    Logger.d("copied file ${result.absolutePath}")
                    Logger.d("Delete temp file ${tmpFile.delete()}")
                }
            }

            scaledBitmap?.recycle()

            return@withContext if (shouldOverride) path else compressedPath
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return@withContext ""
    }

}

@Throws(IOException::class)
fun Context.getBitmapFromUri(uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
    val image: Bitmap? = if (options != null) {
        BitmapFactory.decodeFileDescriptor(parcelFileDescriptor?.fileDescriptor, null, options)
    } else {
        BitmapFactory.decodeFileDescriptor(parcelFileDescriptor?.fileDescriptor)
    }

    val orientation = if (fileDescriptor != null) {
        val exifInterface = ExifInterface(fileDescriptor)
        exifInterface.getAttributeInt(TAG_ORIENTATION, 1)
    } else {
        0
    }

    parcelFileDescriptor?.close()

    if (orientation > 0 && image != null) {
        val matrix = Matrix()
        val rotate = when (orientation) {
            ORIENTATION_ROTATE_90 -> 90f
            ORIENTATION_ROTATE_180 -> 180f
            ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        matrix.postRotate(rotate)
        return Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
    }

    return image
}

fun File.getMediaDuration(context: Context, path: String): Long {
    if(!exists()) return 0
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, Uri.parse(path))
    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()

    return duration.toLongOrNull() ?: 0
}

fun getTimestampString(): String {
    val date = Calendar.getInstance()
    return SimpleDateFormat("yyyy MM dd hh mm ss", Locale.US).format(date.time).replace(" ", "")
}

/**
 * Converts byte value to String.
 */
fun getReadableFileSize(size: Long): String {
    if (size <= 0) return "0KB"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                    1024.0,
                    digitGroups.toDouble()
            )
    ) + " " + units[digitGroups]
}

/**
 * Downloads a file using DownloadManager.
 */
fun Context.downloadFile(url: String, fileName: String) {
    val downloadRequest = DownloadManager.Request(Uri.parse(url))
    downloadRequest.setTitle(fileName)

    // in order for this if to run, you must use the android 3.2 to compile your app
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        downloadRequest.allowScanningByMediaScanner()
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    }

    downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(downloadRequest)
}

fun Context.getFileInfo(uri: Uri): Hashtable<String, Any>? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    try {
        val mime = contentResolver.getType(uri)
        val file = File(cacheDir.absolutePath, "sendbird")

        val inputPFD = contentResolver.openFileDescriptor(uri, "r")
        var fd:FileDescriptor? = null

        if (inputPFD != null) {
            fd = inputPFD.fileDescriptor
        }

        val inputStream = FileInputStream(fd)
        file.createNewFile()
        val outputStream = FileOutputStream(file)

        var read = 0
        val bytes = ByteArray(1024)
        while (inputStream.read(bytes).also { read = it } != -1) {
            outputStream.write(bytes, 0, read)
        }

        if (cursor != null) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val value = Hashtable<String, Any>()

            if (cursor.moveToFirst()) {
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex).toInt()

                value.put("path", file.path)
                value.put("size", size)
                value.put("mime", mime)
                value.put("name", name)
            }

            return value
        }

    } catch (e: Exception) {
        Logger.p(e)
        Logger.d("File not found.")
        return null
    } finally {
        if (cursor != null) {
            cursor.close()
        }
    }

    return null
}