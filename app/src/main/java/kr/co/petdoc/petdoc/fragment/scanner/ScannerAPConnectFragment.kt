
package kr.co.petdoc.petdoc.fragment.scanner

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_scanner_ap_connect.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ConnectScannerActivity
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog

/**
 * Petdoc
 * Class: ScannerAPConnectFragment
 * Created by kimjoonsung on 2020/05/06.
 *
 * Description :
 */
class ScannerAPConnectFragment : Fragment() {

    private lateinit var dataModel:ConnectScannerDataModel
    private lateinit var ani: AnimationDrawable

    private var mSSID = ""
    private var mPasswd = ""
    private var mEncrypt = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_ap_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener {
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

        btnConfirm.setOnClickListener {
            requireActivity().finish()
        }

        //================================================================================================================
        mSSID = dataModel.selectedAPInfo.value?.ssid.toString()
        mPasswd = dataModel.wifiPassword.value.toString()
        mEncrypt = dataModel.selectedAPInfo.value?.encryption.toString()

        ani = connectStatus.background as AnimationDrawable
        ani.start()

        Handler().postDelayed({
            btnConfirm.visibility = View.VISIBLE
            textViewConnecting.text = Helper.readStringRes(R.string.care_scanner_ap_connecting_complete)
            textViewConnectingGuide.text = Helper.readStringRes(R.string.care_scanner_ap_connecting_complete_guide)
            connectStatus.setBackgroundResource(R.drawable.ic_connect_completion)
        }, 10000)

        ConnectScannerActivity.instance.wifiCheckAndConnect(mSSID, mPasswd, mEncrypt)
    }
}