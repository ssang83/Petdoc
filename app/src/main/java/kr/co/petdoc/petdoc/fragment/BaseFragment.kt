package kr.co.petdoc.petdoc.fragment

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionResult
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import java.util.*

/**
 * Petdoc
 * Class: BaseFragment
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description :
 */
open class BaseFragment : Fragment() {

    companion object {
        private const val SHOW_KEYBOARD_DELAY = 300L
    }

    private var KEY_PERMISSION = 0
    private var permissionResult: PermissionResult? = null
    private var permissionsAsk: Array<String>? = null

    private lateinit var mEventBus: EventBus
    private lateinit var mContext: Context
    protected lateinit var mApiClient: ApiClient

    private var mDialog: Dialog? = null
    private var mHandler: Handler? = null
    private var mView: View? = null

    protected lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)
    val scanner: Scanner by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(this, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = requireContext().isInternetConnected()

        mEventBus = EventBus.getDefault()
        mApiClient = PetdocApplication.application.apiClient

        if(!mEventBus.isRegistered(this)) {
            try {
                mEventBus.register(this)
            } catch (e: Exception) {}
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mEventBus.isRegistered(this)) {
            try {
                mEventBus.register(this)
            } catch (e: Exception) {}
        }
    }

    override fun onStop() {
        mEventBus.unregister(this)
        dismissProgress()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (!mEventBus.isRegistered(this)) {
            try {
                mEventBus.register(this)
            } catch (e: Exception) {}
        }
    }

    override fun onDestroy() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        dismissProgress()
        super.onDestroy()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != KEY_PERMISSION) return

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
                    if (!shouldShowRequestPermissionRationale(s)) {
                        permissionResult?.permissionForeverDenied()
                        return
                    }
                }
                permissionResult?.permissionDenied()
            }
        }
    }

    /**
     * Shows toast message.
     *
     * @param msg
     */
    fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    fun showSnackBar(layout: View, msg: String) {
        Snackbar.make(layout, msg, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Check if the Application required Permission is granted.
     *
     * @param context
     * @param permission
     * @return
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(
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
            if (ContextCompat.checkSelfPermission(
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
     * Initialize Loading Dialog
     */
    protected fun initDialog(context: Context) {
        this.mContext = context
        mDialog = Dialog(mContext) // this or YourActivity
        mDialog?.setCanceledOnTouchOutside(false)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.getWindow()!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        mDialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        LayoutInflater.from(mContext).apply {
            mView = inflate(R.layout.loader_layout, null, false)
        }

//        val lottieAnimationView = mView?.findViewById<LottieAnimationView>(R.id.animation_view)
//        lottieAnimationView?.setAnimation(R.raw.loading)

        mDialog?.setContentView(mView!!)

        mHandler = Handler()
    }

    protected fun dismissProgress() {
        if (mHandler != null && mDialog != null) {
            mHandler?.post { mDialog?.dismiss() }
        }
    }

    protected fun showProgress() {
        if (mHandler != null && mDialog != null) {

            mHandler?.post {
                if (!mDialog?.isShowing()!!) {
                    mDialog?.show()
                }
                //        hideKeyboard(edt);
            }
        }
    }

    /**
     * Open KeyBoard
     *
     * @param v
     */
    protected fun showKeyBoardOnView(v: View) {
        v.requestFocus()
        Helper.showKeyBoard(requireActivity(), v)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
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
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
    }

    /**
     * Ask Permission to be granted.
     *
     * @param permissionAsk
     */
    private fun internalRequestPermission(permissionAsk: Array<String>) {
        var arrayPermissionNotGranted: Array<String>
        val permissionsNotGranted = ArrayList<String>()

        for (aPermissionAsk in permissionAsk) {
            if (!isPermissionGranted(requireContext(), aPermissionAsk)) {
                permissionsNotGranted.add(aPermissionAsk)
            }
        }

        if (permissionsNotGranted.isEmpty()) {

            if (permissionResult != null)
                permissionResult?.permissionGranted()

        } else {
            arrayPermissionNotGranted = permissionsNotGranted.toTypedArray()
            requestPermissions(arrayPermissionNotGranted, KEY_PERMISSION)
        }
    }

    fun retryIfNetworkAbsent(invokeIfNetworkExist: Boolean = true, callback: () -> Unit) {
        val isScannerConnected = scanner.isConnected()
        val hasInternet = isNetworkAvailable.value?:true
        if (isScannerConnected && hasInternet.not()) {
            // 스캐너 연결 해제 및 네트웍 연결 확인 팝업
            TwoBtnDialog(requireContext()).apply {
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
                if (invokeIfNetworkExist) {
                    callback.invoke()
                }
            } else {
                // 네트웍 연결 확인 팝업
                AlertDialog.Builder(requireContext(), R.style.DefaultAlertDialogStyle)
                    .setMessage("네트워크가 연결되어 있지 않습니다.\n확인 후 앱을 재 실행해주세요.")
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