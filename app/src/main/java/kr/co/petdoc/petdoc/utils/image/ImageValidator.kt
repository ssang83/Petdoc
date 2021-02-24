package kr.co.petdoc.petdoc.utils.image

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import java.io.*

/**
 * Petdoc
 * Class: ImageValidator
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
object ImageValidator {

    private val LOGTAG = "ImageValidator"

    /**
     * Check if the Picture is valid for uploading. Currently we received this
     * information:<br></br>
     * Minimale Bildbreite 320 Pixel.<br></br>
     * Minimale Bildhöhe 107 Pixel.<br></br>
     * Das Seitenverhältnis muss zwischen 3:1 und 1:3 liegen
     *
     * @param filepath
     * @return
     */

    fun isPictureValidForUpload(filepath: String?): Boolean {
        if (filepath == null) {
            throw IllegalArgumentException("filepath cannot be null")
        }

        val opt = BitmapFactory.Options()
        opt.inJustDecodeBounds = true

        // if (filepath.startsWith("http://"))
        // // already uploaded images are considered valid
        // return true;

        if (filepath.startsWith("file://")) {
            BitmapFactory.decodeFile(filepath.replace("file://", ""), opt)
        } else {
            BitmapFactory.decodeFile(filepath, opt)
        }

        if (opt.outWidth < 320) {
            Log.e(LOGTAG, "width too small")
            return false
        }

        if (opt.outHeight < 107) {
            Log.e(LOGTAG, "height too small")
            return false
        }

        val aspect = (opt.outWidth / opt.outHeight).toDouble()
        if (aspect > 3 || aspect < 1 / 3) {
            Log.e(LOGTAG, "aspect ratio wrong")
            return false
        }

        return true
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // DocumentProvider
            if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null

                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            // MediaStore (and general)

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }

            var path = getDataColumn(context, uri, null, null)

            // if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // final int takeFlags = new Intent().getFlags()
            // & (Intent.FLAG_GRANT_READ_URI_PERMISSION
            // | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            // context.getContentResolver().takePersistableUriPermission(uri,
            // takeFlags);
            // }

            if (TextUtils.isEmpty(path)) {
                path = uri.toString()
            }

            if (File(path!!).exists())
                return path

            var bitmap: Bitmap? = null

            if (uri.toString() != null && (uri.toString().contains("docs.file") || isPicasaPhotoUri(uri))) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    bitmap = BitmapFactory.decodeStream(inputStream)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }

            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(path)
            }

            val dir = File(Environment.getExternalStorageDirectory().toString() + "//")

            if (!dir.exists()) {

                dir.mkdirs()
            }

            val newPath = dir.absolutePath + "/" + System.currentTimeMillis() + ".jpg"

            if (storeImage(bitmap, newPath)) {
                path = newPath
            }

            return path
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"// android.provider.MediaStore.Files.FileColumns.DATA
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri he Uri to check.
     * @return Whether the Uri authority is PicasaProvider.
     */
    fun isPicasaPhotoUri(uri: Uri): Boolean {
        return "com.sec.android.gallery3d.provider" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Save image to sdcard from Bitmap
     *
     * @param imageData
     * @param filePath
     * @return Whether saving Image is successful
     */
    fun storeImage(imageData: Bitmap?, filePath: String): Boolean {
        if (imageData == null) {
            return false
        }

        try {
            val fileOutputStream = FileOutputStream(filePath)
            val bos = BufferedOutputStream(fileOutputStream)
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            Log.e(LOGTAG, "Error saving image file: " + e.message)
            return false
        }

        return true
    }

}