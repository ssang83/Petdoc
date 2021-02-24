package kr.co.petdoc.petdoc.utils.image

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import kr.co.petdoc.petdoc.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Petdoc
 * Class: ImageUtil
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class ImageUtil {

    companion object {

        private val LOGTAG = "ImageUtil"

        /**
         * The format for a timestamp in the form "yyyyMMdd_HHmmss". Used for creating image file
         * names.
         */
        val TIMESTAMP_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US)

        /**
         * The prefix for creating image file names.
         */
        val IMG_PREFIX = "img_"

        /**
         * The prefix for creating temporary image file names.
         */
        val TMP_IMG_PREFIX = "tmpimg_"

        /**
         * Folder name of the ad images which are stored the old way. Kept for deleting cached images
         * for existing ads.
         */
        private val IMAGES_PATH = "imgs"

        /**
         * Get the value of the data column for this Uri. This is useful for MediaStore Uris, and other
         * file-based ContentProviders.
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
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(
                        uri!!,
                        projection,
                        selection,
                        selectionArgs,
                        null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
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
         * Displays an GIF image from a URL in an ImageView.
         */
        fun displayGifImageFromUrl(
                context: Context,
                url: String,
                imageView: ImageView,
                thumbnailUrl: String
        ) {
            val myOptions = RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            if (thumbnailUrl != null) {
                Glide.with(context!!)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                    .into(imageView!!)
            } else {
                Glide.with(context!!)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView!!)
            }
        }

        /**
         * Displays an image from a URL in an ImageView.
         */
        fun displayGifImageFromUrl(context: Context, url: String, imageView: ImageView, listener: RequestListener<GifDrawable>?) {
            val myOptions = RequestOptions()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            if (listener != null) {
                Glide.with(context!!)
                        .asGif()
                        .load(url)
                        .apply(myOptions)
                        .listener(listener)
                        .into(imageView!!)
            } else {
                Glide.with(context!!)
                        .asGif()
                        .load(url)
                        .apply(myOptions)
                        .into(imageView!!)
            }
        }

        /**
         * Displays an image from a URL in an ImageView.
         * If the image is loading or nonexistent, displays the specified placeholder image instead.
         */
        fun displayImageFromUrlWithPlaceHolder(
                context: Context, url: String,
                imageView: ImageView,
                placeholderResId: Int
        ) {
            val myOptions = RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderResId)
            Glide.with(context!!)
                .load(url)
                .apply(myOptions)
                .into(imageView!!)
        }

        /**
         * Displays an image from a URL in an ImageView.
         */
        fun displayImageFromUrl(context: Context, url: String,
                                imageView: ImageView, listener: RequestListener<Drawable>?) {
            val myOptions = RequestOptions()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            if (listener != null) {
                Glide.with(context!!)
                        .load(url)
                        .apply(myOptions)
                        .listener(listener)
                        .into(imageView!!)
            } else {
                Glide.with(context!!)
                        .load(url)
                        .apply(myOptions)
                        .listener(listener)
                        .into(imageView!!)
            }
        }
    }

    private var appCtx: Context? = null
    private var targetImageWidth: Int = 0
    private var targetImageHeight: Int = 0

    /**
     * Creates a new ImageUtil.
     *
     * @param context           The Android context.
     * @param targetImageWidth  The minimum image width.
     * @param targetImageHeight The minimum image height.
     */
    constructor(context: Context, targetImageWidth: Int, targetImageHeight: Int) {
        this.appCtx = context.applicationContext
        this.targetImageWidth = targetImageWidth
        this.targetImageHeight = targetImageHeight
    }

    /**
     * Creates a new ImageUtil. Sets the default minimum image width and height used to downsize
     * images.
     */
    constructor(appCtx: Context) {
        this.appCtx = appCtx
        this.targetImageWidth = 600
        this.targetImageHeight = 200
    }

    /**
     * Create a File for saving an image.
     */
    val outputMediaFile: File?
        get() {
            if (!isExternalStorageWritable) {
                return null
            }

            val mediaStorageDir = mediaStorageDirectory

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(
                            LOGTAG,
                            "Failed to create directory " + appCtx!!.getString(R.string.app_name)
                    )
                    return null
                }
            }

            val timeStamp = TIMESTAMP_FORMAT.format(Date())
            val fileName = "$IMG_PREFIX$timeStamp.jpg"
            return File(mediaStorageDir.path + File.separator + fileName)
        }

    /**
     * Checks if external storage is available for read and write.
     */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /**
     * Opens a stream on to the content of the passed uri and validates the picture dimensions.
     *
     * @param imgUri The image uri.
     * @throws FileNotFoundException Thrown if the file with the passed uri does not exist.
     */
    @Throws(FileNotFoundException::class)
    fun isPictureValidForUpload(imgUri: Uri): Boolean {
        val opts = getImageDimensions(imgUri)
        val orientation = getOrientation(imgUri)
        return isPictureDimensionsValid(
                opts.outWidth.toDouble(),
                opts.outHeight.toDouble(),
                orientation
        )
    }

    /**
     * Does the same as [.isPictureValidForUpload] but takes a file path instead of an
     * Uri.
     *
     * @param filePath The path to the image.
     * @throws IOException Thrown if the file cannot be found.
     */
    @Throws(IOException::class)
    fun isPictureValidForUpload(filePath: String): Boolean {
        val opts = getImageDimensions(filePath)
        val exif = ExifInterface(filePath)
        var orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        )

        // Translate the orientation constant to degrees.
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> orientation = 90

            ExifInterface.ORIENTATION_ROTATE_270 -> orientation = 270

            else -> {
            }
        }

        return isPictureDimensionsValid(
                opts.outWidth.toDouble(),
                opts.outHeight.toDouble(),
                orientation
        )
    }

    fun deleteLocalImages() {
        val mediaStorageDir = mediaStorageDirectory
        if (mediaStorageDir.exists()) {
            deleteRecursive(mediaStorageDir)
        }

        if (appCtx!!.filesDir.exists()) {
            deleteRecursive(mediaStorageDir)
        }
    }

    /**
     * @return The orientation of the image with the passed uri.
     */
    fun getOrientation(uri: Uri): Int {
        val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cursor = appCtx!!.contentResolver.query(uri, columns, null, null, null)
        var orientation = 0
        if (cursor != null && cursor.moveToFirst()) {
            val colIndex = cursor.getColumnIndex(columns[0])
            if (colIndex != -1) {
                orientation = cursor.getInt(colIndex)
            }
            cursor.close()
        }
        return orientation
    }

    /**
     * Returns a downscaled and compressed version of the bitmap at the passed file path.
     *
     * @param filePath The path to the bitmap which is to be optimized.
     * @return The downscaled bitmap in form of a byte array stream.
     */
    fun getOptimizedImageForUpload(filePath: String): ByteArrayOutputStream? {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val uri = Uri.fromFile(file)
                val bitmap = getDownscaledBitmap(uri, false)
                val os = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, os)
                return os
            } else {
                return null
            }
        } catch (e: IOException) {
            return null
        }

    }

    /**
     * Deletes the temporary image files which were created when the user chose a picture from the
     * Storage Access Framework on the PictureSourceActivity.
     */
    fun deleteTempImages() {
        val imageFileNames = appCtx!!.filesDir.list()

        if (imageFileNames != null && imageFileNames.size > 0) {
            for (fileName in imageFileNames) {
                // Check if image is temp. If it is older than 3 hours, delete it.
                if (fileName.startsWith(ImageUtil.TMP_IMG_PREFIX)) {
                    val start = fileName.indexOf("_") + 1
                    val end = fileName.indexOf(".")
                    val timeStamp = fileName.substring(start, end)

                    if (isOlderThanThreeHours(timeStamp)) {
                        if (appCtx!!.deleteFile(fileName)) {
                            Log.d(LOGTAG, "Deleted temp image: $fileName")
                        }
                    }
                }
            }
        }
    }

    /**
     * Downscales the image and stores the downscaled version in a new file.
     *
     * @param uri            The image uri.
     * @param compressFormat E.g. Bitmap.CompressFormat.JPEG.
     * @param quality        The compress percentage, e.g. 85.
     * @param fixRotation    True if the rotation of the image should be fixed.
     * @return The path to the downscaled image.
     * @throws IOException If the image could not be found.
     */
    @Throws(IOException::class)
    fun createDownscaledImage(
            uri: Uri, compressFormat: Bitmap.CompressFormat, quality: Int,
            fixRotation: Boolean
    ): String {
        val timeStamp = TIMESTAMP_FORMAT.format(Date())
        val fileName = "$TMP_IMG_PREFIX$timeStamp.jpg"
        val bitmap = getDownscaledBitmap(uri, fixRotation)
        return saveBitmap(bitmap!!, fileName, compressFormat, quality)
    }

    /**
     * Downscales the image to fit in a box with width and height which were used to initialize this
     * ImageUtil.
     *
     * @param uri         The image uri.
     * @param fixRotation True if the rotation of the image should be fixed.
     * @return A downscaled bitmap.
     * @throws IOException If the image could not be found.
     */
    @Throws(IOException::class)
    fun getDownscaledBitmap(uri: Uri, fixRotation: Boolean): Bitmap? {
        var bitmap: Bitmap? = null
        val opts = getImageDimensions(uri)
        if (opts.outHeight > targetImageHeight || opts.outWidth > targetImageWidth) {
            val scale = Math.max(
                    opts.outHeight / targetImageHeight,
                    opts.outWidth / targetImageWidth
            )
            try {
                bitmap = Glide.with(appCtx!!)
                    .asBitmap()
                    .load(uri)
                    .submit(opts.outWidth / scale, opts.outHeight / scale)
                    .get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

        } else {
            try {
                bitmap = Glide.with(appCtx!!)
                    .asBitmap()
                    .load(uri)
                    .submit(opts.outWidth, opts.outHeight)
                    .get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

        }
        if (fixRotation) {
            val orientation = getOrientation(uri)
            if (orientation > 0) {
                bitmap = fixImageRotation(orientation, bitmap)
            }
        }
        return bitmap
    }


    /**
     * Downscales the image and stores the downscaled version in a new file.
     *
     * @param uri            The image uri.
     * @param compressFormat E.g. Bitmap.CompressFormat.JPEG.
     * @param quality        The compress percentage, e.g. 85.
     * @param fixRotation    True if the rotation of the image should be fixed.
     * @return The path to the downscaled image.
     * @throws IOException If the image could not be found.
     */
    @Throws(IOException::class)
    fun createImageWithDimension(
            uri: Uri,
            compressFormat: Bitmap.CompressFormat,
            quality: Int,
            fixRotation: Boolean,
            targetImageWidth: Int,
            targetImageHeight: Int
    ): String {
        val timeStamp = TIMESTAMP_FORMAT.format(Date())
        val fileName = "$TMP_IMG_PREFIX$timeStamp.jpg"
        val bitmap = getBitmapWithDimension(
                uri, fixRotation, targetImageWidth,
                targetImageHeight
        )
        return saveBitmap(bitmap!!, fileName, compressFormat, quality)
    }

    /**
     * Downscales the image to fit in a box with width and height which were used to initialize this
     * ImageUtil.
     *
     * @param uri         The image uri.
     * @param fixRotation True if the rotation of the image should be fixed.
     * @return A downscaled bitmap.
     * @throws IOException If the image could not be found.
     */
    @Throws(IOException::class)
    fun getBitmapWithDimension(
            uri: Uri,
            fixRotation: Boolean,
            targetImageWidth: Int,
            targetImageHeight: Int
    ): Bitmap? {
        var bitmap: Bitmap? = null
        val opts = getImageDimensions(uri)
        if (opts.outHeight > targetImageHeight || opts.outWidth > targetImageWidth) {
            val scale = Math.max(
                    opts.outHeight / targetImageHeight,
                    opts.outWidth / targetImageWidth
            )
            try {
                bitmap = Glide.with(appCtx!!)
                    .asBitmap()
                    .load(uri)
                    .submit(opts.outWidth / scale, opts.outHeight / scale)
                    .get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

        } else {
            try {
                bitmap = Glide.with(appCtx!!)
                    .asBitmap()
                    .load(uri)
                    .submit(opts.outWidth, opts.outHeight)
                    .get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

        }
        if (fixRotation) {
            val orientation = getOrientation(uri)
            if (orientation > 0) {
                bitmap = fixImageRotation(orientation, bitmap)
            }
        }
        return bitmap
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access Framework
     * Documents, as well as the _data field for the MediaStore and other file-based
     * ContentProviders.
     *
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    fun getPath(uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(appCtx, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )

                return getDataColumn(appCtx!!, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(appCtx!!, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            return getDataColumn(appCtx!!, uri, null, null)
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    private val mediaStorageDirectory: File
        get() = File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ), appCtx!!.getString(R.string.app_name)
        )

    /**
     * Check if the passed picture has valid size and aspect ratio. Minimum width is 320 px, minimum
     * height 107 px. The aspect ratio must be between 3:1 and 1:3.
     *
     * @param width       The width of the image.
     * @param height      The height of the image.
     * @param orientation The orientation of the image.
     * @return True if the dimensions are valid, false otherwise.
     */
    private fun isPictureDimensionsValid(width: Double, height: Double, orientation: Int): Boolean {
        var width = width
        var height = height
        if (orientation == 90 || orientation == 270) {
            // The image will be rotated later and width and height will be swapped.
            val temp = width

            width = height
            height = temp
        }

        if (width < 320 || height < 107) {
            return false
        }

        val aspect = width / height
        return !(aspect > 3 || aspect < 1 / 3)
    }

    /**
     * @param imgUri The image uri.
     * @return The dimensions of the passed image wrapped in the BitmapFactory.Options object.
     * @throws FileNotFoundException Thrown if the file with the passed uri does not exist.
     */
    @Throws(FileNotFoundException::class)
    private fun getImageDimensions(imgUri: Uri): BitmapFactory.Options {
        val `is` = appCtx!!.contentResolver.openInputStream(imgUri)
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeStream(`is`, null, opts)
        if (`is` != null) {
            try {
                `is`.close()
            } catch (e: IOException) {
                // Do not care.
            }

        }
        return opts
    }

    /**
     * Does the same as [.getImageDimensions] but takes a file path instead of an Uri.
     *
     * @param filePath The file path of the image.
     */
    @Throws(FileNotFoundException::class)
    private fun getImageDimensions(filePath: String): BitmapFactory.Options {
        val opts = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, opts)
        return opts
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles()!!)
                deleteRecursive(child)

        fileOrDirectory.delete()
    }

    /**
     * Saves the passed bitmap in the app internal folder using the passed file name.
     *
     * @param bitmap   The bitmap to save.
     * @param fileName The file name for the bitmap.
     * @return The path to the saved bitmap.
     * @throws IOException Thrown if writing to a stream failed.
     */
    @Throws(IOException::class)
    private fun saveBitmap(
            bitmap: Bitmap,
            fileName: String,
            compressFormat: Bitmap.CompressFormat,
            quality: Int
    ): String {
        val bitmapOut = ByteArrayOutputStream()
        bitmap.compress(compressFormat, quality, bitmapOut)
        val fileOut = appCtx!!.openFileOutput(fileName, Context.MODE_PRIVATE)
        bitmapOut.writeTo(fileOut)
        bitmapOut.close()
        fileOut.close()
        return File(appCtx!!.filesDir, fileName).path
    }

    /**
     * Rotates the image to the right orientation if needed. Then saves it. This is needed for
     * images chosen via the gallery app on some devices.
     *
     * @param orientation The current orientation of the image.
     * @param source      The image to rotate.
     * @throws IOException Thrown if writing to a stream failed.
     */
    @Throws(IOException::class)
    private fun fixImageRotation(orientation: Int, source: Bitmap?): Bitmap? {
        when (orientation) {
            90 -> return rotateImage(source, 90f)

            180 -> return rotateImage(source, 180f)

            270 -> return rotateImage(source, 270f)

            else -> return source
        }
    }

    /**
     * Rotates the passed bitmap using the passed angle.
     *
     * @param source The image to rotate.
     * @param angle  The angle to use for the rotation.
     * @throws IOException Thrown if writing to a stream failed.
     */
    @Throws(IOException::class)
    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
                source!!, 0, 0, source.width, source.height, matrix, true
        )
    }

    /**
     * Parses the passed time stamp and checks if it is more than three hours in the past.
     *
     * @param timeStamp The time stamp to check.
     * @return True if the time stamp is more then three hours in the past, false otherwise.
     */
    private fun isOlderThanThreeHours(timeStamp: String): Boolean {
        try {
            val date = ImageUtil.TIMESTAMP_FORMAT.parse(timeStamp)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, -3)
            val threeHoursBefore = calendar.time
            return date.before(threeHoursBefore)
        } catch (e: ParseException) {
            return false
        }

    }

    private fun deleteCachedImageIfExists(imageFolderPath: String, pic: String?) {
        if (pic != null && pic.startsWith("file://$imageFolderPath")) {
            // image was taken only for this ad -> can be deleted
            val f = File(pic.substring("file://".length))
            if (f.exists() && f.isFile) {

                f.delete()
            }
        }
    }
}