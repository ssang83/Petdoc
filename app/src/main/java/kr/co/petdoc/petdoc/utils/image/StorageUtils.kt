package kr.co.petdoc.petdoc.utils.image

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Petdoc
 * Class: StorageUtils
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
object StorageUtils {

    private val LOGTAG = "StorageUtils"


    private val EXTERNAL_STORAGE_PERMISSION = "android.permission" + ".WRITE_EXTERNAL_STORAGE"
    private val INDIVIDUAL_DIR_NAME = "uil-images"

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* if card is mounted and app has appropriate
     * permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache [directory][File].<br></br> **NOTE:** Can be null in some unpredictable
     * cases (if SD card is unmounted and [Context.getCacheDir()][Context.getCacheDir]
     * returns null).
     */
    fun getCacheDirectory(context: Context): File {
        return getCacheDirectory(context, true)
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* (if card is mounted and app has appropriate
     * permission) or on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache [directory][File].<br></br> **NOTE:** Can be null in some unpredictable
     * cases (if SD card is unmounted and [Context.getCacheDir()][Context.getCacheDir]
     * returns null).
     */
    fun getCacheDirectory(context: Context, preferExternal: Boolean): File {
        var appCacheDir: File? = null
        var externalStorageState: String
        try {
            externalStorageState = Environment.getExternalStorageState()
        } catch (e: NullPointerException) { // (sh)it happens (Issue #660)
            externalStorageState = ""
        } catch (e: IncompatibleClassChangeError) {
            externalStorageState = ""
        }

        if (preferExternal && Environment.MEDIA_MOUNTED == externalStorageState && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            appCacheDir = context.cacheDir
        }
        if (appCacheDir == null) {
            val cacheDirPath = "/data/data/" + context.packageName + "/cache/"
            Log.w(
                LOGTAG, "Can't define system cache directory! '%s' will be used.",
                Throwable(cacheDirPath)
            )
            appCacheDir = File(cacheDirPath)
        }
        return appCacheDir
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader).
     * Cache directory will be created on SD card *
     * ("/Android/data/[app_package_name]/cache/uil-images")
     * *  if card is mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @param context Application context
     * @return Cache [directory][File]
     */
    fun getIndividualCacheDirectory(context: Context): File {
        return getIndividualCacheDirectory(context, INDIVIDUAL_DIR_NAME)
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader).
     * Cache directory will be created on SD card *
     * ("/Android/data/[app_package_name]/cache/uil-images")
     * *  if card is mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache [directory][File]
     */
    fun getIndividualCacheDirectory(context: Context, cacheDir: String): File {
        val appCacheDir = getCacheDirectory(context)
        var individualCacheDir = File(appCacheDir, cacheDir)
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir
            }
        }
        return individualCacheDir
    }

    /**
     * Returns specified application cache directory. Cache directory will be created on SD card by
     * defined path if card is mounted and app has appropriate permission. Else - Android defines
     * cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache [directory][File]
     */
    fun getOwnCacheDirectory(context: Context, cacheDir: String): File? {
        var appCacheDir: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(context)) {
            appCacheDir = File(Environment.getExternalStorageDirectory(), cacheDir)
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.cacheDir
        }
        return appCacheDir
    }

    /**
     * Returns specified application cache directory. Cache directory will be created on SD card by
     * defined path if card is mounted and app has appropriate permission. Else - Android defines
     * cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache [directory][File]
     */
    fun getOwnCacheDirectory(
        context: Context, cacheDir: String,
        preferExternal: Boolean
    ): File? {
        var appCacheDir: File? = null
        if (preferExternal && Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(
                context
            )
        ) {
            appCacheDir = File(Environment.getExternalStorageDirectory(), cacheDir)
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.cacheDir
        }
        return appCacheDir
    }

    private fun getExternalCacheDir(context: Context): File? {
        val dataDir = File(
            File(Environment.getExternalStorageDirectory(), "Android"),
            "data"
        )
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(LOGTAG, "Unable to create external cache directory")
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                Log.i(
                    LOGTAG,
                    "Can't create \".nomedia\" file in application external cache directory"
                )
            }

        }
        return appCacheDir
    }

    private fun hasExternalStoragePermission(context: Context): Boolean {
        val perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION)
        return perm == PackageManager.PERMISSION_GRANTED
    }


    // ---------------------------------------------------------------------------------------------
    /**
     * Petdoc
     * Created by sungminKim on 2020/04/02
     * Description : SharePrefrence Control Part
     */
    private var sharedPreferences : SharedPreferences? = null
    private val PRENAME = "petdocpreference"

    fun writeValueInPreference(mContext: Context?, key:String, value:String){
        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        sharedPreferences?.edit()?.apply{
            putString(key, value)
            apply()
        }
    }

    fun loadValueFromPreference(mContext:Context?, key:String, base:String):String{

        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        return sharedPreferences?.getString(key, base) ?: base
    }

    fun removeKeyInPreference(mContext:Context?, key:String){
        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        sharedPreferences?.edit()?.remove(key)?.apply()
    }

    fun writeBooleanValueInPreference(mContext: Context?, key:String, value:Boolean){
        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        sharedPreferences?.edit()?.apply{
            putBoolean(key, value)
            apply()
        }
    }

    fun loadBooleanValueFromPreference(mContext:Context?, key:String): Boolean {

        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        return sharedPreferences?.getBoolean(key, false)!!
    }

    fun writeIntValueInPreference(mContext: Context?, key:String, value:Int){
        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        sharedPreferences?.edit()?.apply{
            putInt(key, value)
            apply()
        }
    }

    fun loadIntValueFromPreference(mContext:Context?, key:String): Int {

        if(sharedPreferences==null){
            sharedPreferences = mContext?.getSharedPreferences(PRENAME, Context.MODE_PRIVATE)
        }

        return sharedPreferences?.getInt(key, -1)!!
    }

    // ---------------------------------------------------------------------------------------------
}