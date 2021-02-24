package kr.co.petdoc.petdoc.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Petdoc
 * Class: Helper
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
object Helper {

    private val LOGTAG = "Helper"

    val LOCATION_TIMEOUT = (15 * 60 * 1000).toLong()

    internal val DATE_TEMPLATE = "EEE, d MMM yyyy HH:mm:ss Z"

    private val ONE_DAY = 86400L
    val FOUR_WEEKS = 28L * ONE_DAY * 1000L

    private val FORMATTER1 = SimpleDateFormat(
        "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH
    )

    val CHAT_FORMAT = SimpleDateFormat("HH:mm")
    val validationDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val validationTimeFormat = SimpleDateFormat("HH:mm:ss")
    val validationDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val FORMATTER2 = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val FORMATTER3 = SimpleDateFormat("HH:mm", Locale.getDefault())
    val ZULU_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())

    /**
     * Check for active internet connection
     *
     * @param ctx
     * @return
     */
    fun isInternetActive(ctx: Context): Boolean {
        val conMgr = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = conMgr.activeNetworkInfo

        return info != null && info.isConnected
    }


    fun isEmpty(str: String): Boolean {
        return TextUtils.isEmpty(str)
    }


    fun closeCursor(c: Cursor?) {
        if (c != null && !c.isClosed) {
            c.close()
        }
    }

    fun changeDateFormat(
        sourceFormat: DateFormat, targetFormat: SimpleDateFormat,
        dateStr: String
    ): String? {
        val date: Date
        try {
            date = sourceFormat.parse(dateStr)
            return targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    fun getFormattedDate(milliSeconds: Long, dateFormat: SimpleDateFormat): String {
        // Create a calendar object that will convert the date and time value in milliseconds to
        // date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return dateFormat.format(calendar.time)
    }


    @Throws(ParseException::class)
    fun getValidationDate(adDate: String): String {
        return "" + getDaysBetweenDates(FORMATTER1.parse(adDate), Date())
    }

    /**
     * @param date1 -past date
     * @param date2 -future date than date1
     * @return total days between Dates
     */
    private fun getDaysBetweenDates(date1: Date, date2: Date): Int {
        return ((date2.time - date1.time) / (1000 * 60 * 60 * 24L)).toInt()
    }


    fun getCurrentDate(): String {
        return FORMATTER1.format(Date())
    }

    /**
     * Returns a semicolon separated list built from the passed list of String.
     *
     * @param list A list of strings.
     * @return A semicolon separated list in one String object or an empty string if the passed list
     * is null.
     */
    private fun createSemicolonSeparatedList(list: List<String>?): String {
        if (list == null)
            return ""

        var semicolonSeparatedList = ""
        var i = 0

        for (item in list) {
            semicolonSeparatedList = semicolonSeparatedList + item
            if (list.size > i + 1) {
                semicolonSeparatedList += ";"
            }
            i++
        }

        return semicolonSeparatedList
    }

    fun getFilenameFromPath(path: String?): String {
        if (path == null) {
            throw IllegalArgumentException("path cannot be null")
        }
        val lastIndexOf = path.lastIndexOf("/")
        return if (lastIndexOf == -1) {
            path
        } else path.substring(lastIndexOf + 1, path.length)
    }

    fun showKeyBoard(context: Context?, view: View?) {
        if (context == null) {
            throw IllegalArgumentException("context cannot be null")
        }
        if (view == null) {
            throw IllegalArgumentException("view cannot be null")
        }

        val imm = context.getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(context: Context?, view: View?) {
        if (context == null) {
            throw IllegalArgumentException("context cannot be null")
        }
        if (view == null) {
            throw IllegalArgumentException("view cannot be null")
        }
        val mgr = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isClickedOutsideOf(me: MotionEvent, v: View): Boolean {
        val location = intArrayOf(0, 0)

        v.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]

        if (me.x < x) {
            return true
        } else if (me.y < y) {
            return true
        } else if (me.x > x + v.measuredWidth) {
            return true
        } else if (me.y > y + v.measuredHeight) {
            return true
        }
        return false
    }

    fun getLocationTextForDetail(country: String, cityzip: String, city: String): String {
        return "$country-$cityzip $city"
    }

    private fun isOnline(ctx: Context): Boolean {
        val cm = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetworkInfo
        return info != null && info.isConnected
    }

    /**
     * Check if roaming connection active.
     *
     * @return
     */
    private fun isConnectionRoaming(ctx: Context): Boolean {
        val cm = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo

        return ni != null && ni.isRoaming

    }

    /**
     * Is Roaming enabled.
     *
     * @return
     */
    private fun isRoamingEnabled(ctx: Context): Boolean {
        val cr = ctx.contentResolver
        var result = 0 // 0 is false
        var check = false

        try {
            result = Settings.Secure.getInt(cr, Settings.Secure.DATA_ROAMING)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        if (result == 1) {
            check = true
        }

        return check
    }

    /**
     * Check if network connection is allowed.
     *
     * @return
     */
    fun isNetworkConnectionAllowed(ctx: Context): Boolean {
        val result: Boolean
        result = isOnline(ctx) && (!isConnectionRoaming(ctx) || isRoamingEnabled(ctx))

        if (!result) {
            Log.w(LOGTAG, "no network connection allowed")
        }

        return result
    }

    fun isAppInstalled(uri: String, context: Context): Boolean {
        val pm = context.packageManager
        var app_installed: Boolean

        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            app_installed = true
        } catch (e: PackageManager.NameNotFoundException) {
            app_installed = false
        }

        return app_installed
    }

    /**
     * Writes long strings to the logcat.
     *
     * @param str The string to write to the logcat.
     */
    fun logLongStrings(logTag: String, str: String) {
        if (str.length > 4000) {
            Log.d(logTag, str.substring(0, 4000))
            logLongStrings(logTag, str.substring(4000))
        } else {
            Log.d(logTag, str)
        }
    }

    fun getMd5Checksum(fileContentBaseEncoded: String): StringBuffer {
        val bytesOfMessage = fileContentBaseEncoded.toByteArray()
        return getMd5Checksum(bytesOfMessage)
    }

    fun getMd5Checksum(bytesOfMessage: ByteArray): StringBuffer {
        val md5sum: ByteArray

        try {
            val md = MessageDigest.getInstance("MD5")
            md5sum = md.digest(bytesOfMessage)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e.message)
        }

        val md5checksum = StringBuffer()

        for (aMd5sum in md5sum) {
            md5checksum.append(((aMd5sum and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }

        return md5checksum
    }

    /**
     * Returns the current app version code.
     *
     * @param ctx The Android application context.
     * @return Application's version code from the [PackageManager].
     */
    fun getAppVersion(ctx: Context): Int {
        try {
            val packageInfo = ctx.packageManager.getPackageInfo(
                ctx.packageName,
                0
            )

            return packageInfo.versionCode

        } catch (e: PackageManager.NameNotFoundException) {
            // should never happen
            throw RuntimeException("Could not get package name: $e")
        }

    }

    /**
     * @param ctx The Android application context.
     * @return Application name
     */
    fun getAppName(ctx: Context): String {
        val packageManager = ctx.packageManager
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo = packageManager
                .getApplicationInfo(ctx.applicationInfo.packageName, 0)
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return (if (applicationInfo != null)
            packageManager.getApplicationLabel(applicationInfo)
        else
            "Unknown") as String
    }

    /**
     * Tries to open the app store by using the passed storeAppUri. If this fails, opens the store
     * website.
     *
     * @param ctx             The Android context.
     * @param storeAppUri     The app store uri.
     * @param storeWebsiteUri The store website.
     */
    fun openAppStore(ctx: Context, storeAppUri: Uri, storeWebsiteUri: Uri) {
        var marketIntent: Intent

        try {
            marketIntent = Intent(Intent.ACTION_VIEW, storeAppUri)
            marketIntent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY //or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
            )
            ctx.startActivity(marketIntent)

        } catch (anfe: android.content.ActivityNotFoundException) {
            marketIntent = Intent(Intent.ACTION_VIEW, storeWebsiteUri)
            marketIntent.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY //or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
            )
            ctx.startActivity(marketIntent)
        }

    }

    /**
     * Show the activity over the lockscreen and wake up the device. If you launched the app
     * manually both of these conditions are already true. If you deployed from the IDE, however,
     * this will save you from hundreds of power button presses and pattern swiping per day!
     */
    @SuppressLint("InvalidWakeLockTag")
    fun riseAndShine(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        val power = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        val lock = power.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "wakeup!"
        )

        lock.acquire(100000)
        lock.release()
    }


    fun getToolbarHeight(context: Context): Int {
        val styledAttributes = context.theme.obtainStyledAttributes(
            intArrayOf(R.attr.actionBarSize)
        )
        val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()

        return toolbarHeight
    }


    //sungmin added 20200403 :  statusbar mode -----------------------------------------------------
    fun statusBarColorChange(activity:Activity, lightmode:Boolean, background:IntArray= intArrayOf(255,255,255), alpha:Int=255, fullscreen:Boolean = false){

        activity.window?.decorView?.systemUiVisibility = when{
            lightmode -> when(fullscreen){
                true -> View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                false -> View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            else-> when(fullscreen){
                true ->  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                false -> View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }

        activity.window?.statusBarColor = Color.argb(alpha, background[0],background[1],background[2])
    }

    // sungmin added 20200403 : Dp to Pixel,  DPResource To Px -------------------------------------
    fun convertDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun convertDPResourceToPx(context: Context, res:Int) : Int {
        val dp = context.resources.getDimension(res) / context.resources.displayMetrics.density
        return convertDpToPx(context, dp).toInt()
    }

    fun convertPxToDp(context: Context, px: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(px / (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }

    //sungmin added 20200406 :  statusbar height ---------------------------------------------------
    fun getStatusBarHeight(activity:Activity):Int{      //window: Window?):Int{
        var idStatusBarHeight = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return activity.resources.getDimensionPixelSize(idStatusBarHeight)
    }

    //sungmin added 20200406 :  get resource color SDK compat --------------------------------------
    fun readColorRes(res:Int):Int{

        val answer : Int
        val mContext: Context = PetdocApplication.application.applicationContext

        answer = when{
            Build.VERSION.SDK_INT >= 23 -> mContext.resources.getColor(res, null)
            else -> mContext.resources.getColor(res)
        }

        return answer
    }

    fun readStringRes(res:Int):String{
        var answer = ""
        val mContext: Context = PetdocApplication.application.applicationContext
        answer = mContext.resources.getString(res)

        return if(answer.isNullOrEmpty()) "" else answer
    }

    //sungmin added 20200407 :  get screen size using activity --------------------------------------
    fun screenSize(activity:Activity):IntArray{
        val result = IntArray(2)

        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)

        result[0] = dm.widthPixels
        result[1] = dm.heightPixels

        return result
    }

    fun permissionCheck(context:Activity, permissionList:Array<String>, event:PermissionCallback?, requestAuto:Boolean = true){

        for(permission in permissionList){
            val granted = ContextCompat.checkSelfPermission(context, permission)
            if(granted != PackageManager.PERMISSION_GRANTED){

                event?.callback(PermissionStatus.DENIED_SOME)
                if(requestAuto){
                    permissionRequest(context, permissionList, event)
                }
                return
            }
        }

        event?.callback(PermissionStatus.ALL_GRANTED)
    }

    fun permissionRequest(context:Activity, permissionList:Array<String>, event:PermissionCallback?){
        ActivityCompat.requestPermissions(context, permissionList,200)
    }
    // ---------------------------------------------------------------------------------------------

    fun ToPriceFormat(num: Int): String {
        val df = DecimalFormat("#,###")
        return df.format(num.toLong())
    }

    fun trimSpannable(spannable: SpannableStringBuilder): SpannableStringBuilder {
        checkNotNull(spannable)
        var trimStart = 0
        var trimEnd = 0

        var text = spannable.toString()

        while (text.length > 0 && text.startsWith("\n")) {
            text = text.substring(1)
            trimStart += 1
        }

        while (text.length > 0 && text.endsWith("\n")) {
            text = text.substring(0, text.length - 1)
            trimEnd += 1
        }

        return spannable.delete(0, trimStart)
            .delete(spannable.length - trimEnd, spannable.length)
    }


    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if `reference` is null
     */
    fun <T> checkNotNull(reference: T?): T {
        if (reference == null) {
            throw NullPointerException()
        }
        return reference
    }

    fun getDatePart(date: Date): Calendar {
        val cal = Calendar.getInstance()       // get calendar instance
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
        cal.set(Calendar.MINUTE, 0)                 // set minute in hour
        cal.set(Calendar.SECOND, 0)                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0)            // set millisecond in second

        return cal                                  // return the date part
    }

    /**
     * This method assumes endDate >= startDate
     */
    fun getDaysBetween(startDate: Date, endDate: Date): Long {
        val sDate = getDatePart(startDate)
        val eDate = getDatePart(endDate)

        var daysBetween: Long = 0
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1)
            daysBetween++
        }
        return daysBetween
    }


    fun getJSONData(`object`: Any?): String? {
        if (`object` != null) {
            val gson = Gson()
            return gson.toJson(`object`)
        } else {
            return null
        }
    }

    fun isValidEmail(target: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target.trim { it <= ' ' }).matches()
    }

    fun bodyToString(request: RequestBody?): String {
        try {
            val buffer = Buffer()
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            return "did not work"
        }

    }

    fun getCompleteWordByJongsung(name: String, firstValue: String, secondValue: String):String {
        if(name.isEmpty()) return ""

        val lastName = name.last()

        // 한글의 제일 처음과 끝의 범위밖일 경우는 오류
        if (lastName < 0xAC00.toChar() || lastName > 0xD7A3.toChar()) {
            return name
        }

        val selectedValue = if((lastName - 0xAC00.toChar()).rem(28) > 0) {
            firstValue
        } else {
            secondValue
        }

        return name+selectedValue
    }
}



enum class PermissionStatus{
    ALL_GRANTED,
    DENIED_SOME
}

interface PermissionCallback{
    fun callback(status:PermissionStatus)
}