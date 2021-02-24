package kr.co.petdoc.petdoc.fragment.scanner

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scanner_connect.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.fragment.scanner.utils.WifiChecker
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ScannerConnectFragment
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
class ScannerConnectFragment : BaseFragment() {

    private lateinit var ani:AnimationDrawable
    private lateinit var dataModel:ConnectScannerDataModel

    private var mSSID = ""
    private var mPasswd = ""
    private var mEncrypt = ""

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Logger.p(throwable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.care_scanner_close_title))
                setMessage(Helper.readStringRes(R.string.care_scanner_close_desc))
                setConfirmButton(Helper.readStringRes(R.string.care_scanner_close_confirm), View.OnClickListener {
                    if (PetdocApplication.mLiteCamera != null) {
                        PetdocApplication.mLiteCamera!!.setCameraListener(null)
                        PetdocApplication.mLiteCamera!!.clear()
                        PetdocApplication.mLiteCamera = null
                    }

                    StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, false)

                    dismiss()
                    requireActivity().finish()
                })
                setCancelButton(Helper.readStringRes(R.string.care_scanner_close_cancel), View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        btnConfirm.setOnClickListener {
            requireActivity().finish()
        }

//        btnConnectAP.setOnClickListener {
//            ConnectScannerActivity.instance.queryScan(ConnectScannerActivity.QUERY_STATUS.QUERY_STATUS_AP_LIST)
//            Logger.d("주변 공유기를 검색하는 중입니다.")
//        }

        //================================================================================================
        mSSID = dataModel.ssid.value.toString()
        mPasswd = dataModel.password.value.toString()
        mEncrypt = "wpa2"

        ani = progressScanner.background as AnimationDrawable
        ani.start()

        Logger.d("ssid : $mSSID, password : $mPasswd, encrypt : $mEncrypt")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startDeviceConnectChecker(mSSID, mPasswd, mEncrypt)
//            val isOpen = ScanResultUtil.isOpen(dataModel.scanResult.value!!)
//            if (isOpen) {
//                startDeviceConnectChecker(mSSID, mPasswd, "none")
//            } else {
//                startDeviceConnectChecker(mSSID, mPasswd, mEncrypt)
//            }
        } else {
            scanner.tryConnect(mSSID, mPasswd)
        }
    }

    private fun startDeviceConnectChecker(ssid: String, password:String, encrypt:String) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val connectedDevice = async {
                    val checker = WifiChecker(requireContext(), ssid, password, encrypt)
                    val checkResult: Boolean = checker.startCheck()
                    Logger.d("checkReuslt : $checkResult")
                    if (checkResult) {
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_NAME, ssid)
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_PASSWORD, password)
                        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, true)
                    }

//                    ConnectScannerActivity.instance.startNameChecker(false)

                    if (checkResult) ssid else ""
                }.await()

                if (connectedDevice.isNotEmpty()) {
                    // withContext() 와 async 는 동일
                    withContext(Dispatchers.Main) {
                        dataModel.connectedDevice.value = connectedDevice
                    }
                    PetdocApplication.mLiteCamera?.tryConnect()
                } else {
                    ani.stop()

                    lifecycleScope.launch(Dispatchers.Main) {
                        OneBtnDialog(requireContext()).apply {
                            setTitle("WIFI SSID 인증 실패")
                            setMessage("WIFI SSID 인증 실패하였습니다. 아이디와 비밀번호를 확인하세요.")
                            setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                                dismiss()
                                requireActivity().finish()
                            })
                            show()
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event:ScannerEvent) {
        if (event.scannerStatus) {
            ani.stop()
            btnConfirm.visibility = View.VISIBLE
            textViewConnecting.text = Helper.readStringRes(R.string.care_scanner_connecting_complete)
            textViewConnectingGuide.text = Helper.readStringRes(R.string.care_scanner_connecting_ap_guide)
            progressScanner.setBackgroundResource(R.drawable.ic_connect_scanner_compeletion)

            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_NAME, mSSID)
            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_PASSWORD, mPasswd)
            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, true)
        } else if (event.apStatus) {
            findNavController().navigate(ScannerConnectFragmentDirections.actionScannerConnectFragmentToScannerAPListFragment())
        }
    }
}