package kr.co.petdoc.petdoc.fragment.scanner

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scanner_wifi_power.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import java.util.*

/**
 * Petdoc
 * Class: ScannePowerOnFragment
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
class ScannerPowerOnFragment : Fragment() {

    private lateinit var wifiManager:WifiManager
    private lateinit var dataModel:ConnectScannerDataModel

    private var timer:Timer? = null

    private var isRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_wifi_power, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnSingleClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.care_scanner_close_title))
                setMessage(Helper.readStringRes(R.string.care_scanner_close_desc))
                setConfirmButton(Helper.readStringRes(R.string.care_scanner_close_confirm), View.OnClickListener {
                    dismiss()
                    requireActivity().finish()
                })
                setCancelButton(Helper.readStringRes(R.string.care_scanner_close_cancel), View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        btnConfirm.setOnSingleClickListener {
            findNavController().navigate(R.id.action_scannePowerOnFragment_to_scannerInputPasswdFragment)
        }

        //===========================================================================================

        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            DeviceScanner().startDeviceScan()
//        } else {
//            DeviceScanner().scanSuccess()
//        }

        playGuideImg()
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
    }

    override fun onResume() {
        super.onResume()
        isRunning = true
    }

    private fun playGuideImg() {
        lifecycleScope.launchWhenResumed {
            while (isRunning) {
                delay(700)
                zoomLight.apply {
                    when {
                        visibility == View.VISIBLE -> visibility = View.GONE
                        else -> visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (isAdded) {
                if (success) {
                    DeviceScanner().scanSuccess()
                } else {
                    DeviceScanner().scanFailure()
                }
            }
        }
    }

    inner class DeviceScanner {

        fun startDeviceScan() {
            if (wifiManager.isWifiEnabled) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val filter = IntentFilter()
                    filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                    if (isAdded) {
                        requireActivity().registerReceiver(wifiScanReceiver, filter)
                    }

                    val success = wifiManager.startScan()
                    Logger.d("succes : $success")
                    if (!success) {
                        if (isAdded) {
                            requireActivity().unregisterReceiver(wifiScanReceiver)
                            scanFailure()
                        }
                    }
                }
            } else {
                val dialog =
                    AlertDialog.Builder(requireActivity())
                        .setMessage("Wifi 을 켜시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton(
                            android.R.string.yes
                        ) { dialog, which -> wifiManager.isWifiEnabled }
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                dialog.show()
            }
        }

        fun scanSuccess() {
            Logger.d("scanSuccess")

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                requireActivity().unregisterReceiver(wifiScanReceiver)
            }

            val results: List<ScanResult> = wifiManager.scanResults
            val bebescanResults = mutableListOf<ScanResult>()

            for (loop in results.indices) {
                val result = results[loop]
                Logger.d("result.SSID : $result.SSID")
                if (result.SSID.startsWith("PetdocScan")) {
                    bebescanResults.add(result)
                }
            }

            val lists = arrayOfNulls<String>(bebescanResults.size)
            for (loop in bebescanResults.indices) {
                val result = bebescanResults[loop]
                Logger.d("add list : ${result.SSID}, capa : ${result.capabilities}")
                lists[loop] = result.SSID
            }

            if (lists.size > 0) {
                cancelTimer()

                dataModel.scanResult.value = bebescanResults[0]
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val task = object : TimerTask() {
                        override fun run() {
                            DeviceScanner().startDeviceScan()
                        }
                    }

                    timer = Timer()
                    timer?.schedule(task, 5000, 5000)
                } else {
                    val task = object : TimerTask() {
                        override fun run() {
                            DeviceScanner().scanSuccess()
                        }
                    }

                    timer = Timer()
                    timer?.schedule(task, 5000, 5000)
                }
            }
        }

        fun scanFailure() {
            Logger.d("scanFailure")
        }
    }
}