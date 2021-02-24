package kr.co.petdoc.petdoc.activity

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppForegroundDetector
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionResult
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import java.util.*

/**
 * Petdoc
 * Class: BaseActivity
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description :
 */
open class BaseActivity : AppCompatActivity(), AppForegroundDetector.Listener {

    companion object {
        private const val SHOW_KEYBOARD_DELAY = 300L
        var mActivityList:MutableList<Activity> = mutableListOf()       // Activity 종료 시킬 리스트
    }

    private var KEY_PERMISSION = 0
    private var permissionResult: PermissionResult? = null
    private var permissionsAsk: Array<String>? = null

    private lateinit var mEventBus: EventBus
    protected lateinit var mApiClient: ApiClient

    protected lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)
    val scanner: Scanner by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = isInternetConnected()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mEventBus = EventBus.getDefault()
        mApiClient = PetdocApplication.application.apiClient

        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        PetdocApplication.addActivityLifeCycleListener(this)
    }

    override fun onStart() {
        super.onStart()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        mEventBus.unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }
    }

    override fun onDestroy() {
        if(mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        PetdocApplication.removeActivityLifeCycleListener(this)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode != KEY_PERMISSION) return

        val permissionDenied = LinkedList<String>()
        var granted = true

        for (i in grantResults.indices) {

            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                granted = false
                permissionDenied.add(permissions[i])
            }
        }

        if (permissionResult != null) {
            if (granted) {
                permissionResult?.permissionGranted()
            } else {
                for (s in permissionDenied) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, s)) {
                        permissionResult?.permissionForeverDenied()
                        return
                    }
                }
                permissionResult?.permissionDenied()
            }
        }
    }

    override fun onCreated(activityName: String) {}

    override fun onResumed(activityName: String?) {}

    override fun onStarted(activityName: String?) {}

    override fun onStopped(activityName: String?) {}

    override fun onDestroyed(activityName: String?) {}

    override fun onPaused(activityName: String?) {}

    @Subscribe
    fun onEventMainThread(apiErrorEvent: ApiErrorEvent) {

    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * Shows toast message.
     *
     * @param msg
     */
    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Check if the Application required Permission is granted.
     *
     * @param context
     * @param permission
     * @return
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the Application required Permissions are granted.
     *
     * @param context
     * @param permissions
     * @return
     */
    fun isPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true

        var granted = true
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context, permission
                ) != PackageManager.PERMISSION_GRANTED
            )
                granted = false
        }

        return granted
    }

    fun askCompactPermission(permission: String, permissionResult: PermissionResult) {
        KEY_PERMISSION = 200
        permissionsAsk = arrayOf(permission)
        this.permissionResult = permissionResult
        internalRequestPermission(permissionsAsk!!)
    }

    fun askCompactPermissions(permissions: Array<String>, permissionResult: PermissionResult) {
        KEY_PERMISSION = 200
        permissionsAsk = permissions
        this.permissionResult = permissionResult
        internalRequestPermission(permissionsAsk!!)
    }

    /**
     * Activity간 전환 시 여러 Activity 한번에 강제종료 시킨다.
     */
    fun activityFinish() {
        Logger.d("activity size : ${mActivityList.size}")
        for(i in mActivityList.indices) {
            mActivityList[i].finish()
        }
    }

    /**
     * Open KeyBoard
     *
     * @param v
     */
    protected fun showKeyBoardOnView(v: View) {
        v.requestFocus()
        Helper.showKeyBoard(this, v)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        v.requestFocus()
    }

    protected fun showKeyBoardOnViewDelay(v: View) {
        Handler().postDelayed( { showKeyBoardOnView(v) }, SHOW_KEYBOARD_DELAY)
    }

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    protected fun hideKeyboard(v: View) {
        Helper.hideKeyboard(this, v)
        v.clearFocus()
    }

    protected fun focusOnView(scrollView: ScrollView, v: View) {
        Handler().post { scrollView.smoothScrollTo(0, v.bottom) }
    }

    /**
     * Ask Permission to be granted.
     *
     * @param permissionAsk
     */
    private fun internalRequestPermission(permissionAsk: Array<String>) {
        var arrayPermissionNotGranted: Array<String>
        val permissionsNotGranted:MutableList<String> = mutableListOf()

        for (aPermissionAsk in permissionAsk) {
            if (!isPermissionGranted(this, aPermissionAsk)) {
                permissionsNotGranted.add(aPermissionAsk)
            }
        }

        if (permissionsNotGranted.isEmpty()) {

            if (permissionResult != null)
                permissionResult?.permissionGranted()

        } else {
            arrayPermissionNotGranted = permissionsNotGranted.toTypedArray()
            ActivityCompat.requestPermissions(
                this,
                arrayPermissionNotGranted, KEY_PERMISSION
            )
        }
    }

    fun retryIfNetworkAbsent(callback: () -> Unit) {
        val isScannerConnected = scanner.isConnected()
        val hasInternet = isNetworkAvailable.value?:true
        if (isScannerConnected && hasInternet.not()) {
            // 스캐너 연결 해제 및 네트웍 연결 확인 팝업
            TwoBtnDialog(this).apply {
                setTitle("펫닥 스캐너 연결 해제")
                setMessage("이용 가능한 네트워크가 없습니다.\n연결을 해제하시겠습니까?")
                setCancelable(false)
                setConfirmButton("해제", View.OnClickListener {
                    scanner.disconnect()
                    retryIfNetworkAbsent {
                        callback.invoke()
                    }
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        } else {
            if (hasInternet) {
                callback.invoke()
            } else {
                // 네트웍 연결 확인 팝업
                AlertDialog.Builder(this, R.style.DefaultAlertDialogStyle)
                    .setMessage("네트워크가 연결되어 있지 않습니다.\n확인 후 다시 시도해주세요.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.caption_retry) { _, _ ->
                        retryIfNetworkAbsent {
                            callback.invoke()
                        }
                    }.also {
                        it.show()
                    }
            }
        }
    }
}