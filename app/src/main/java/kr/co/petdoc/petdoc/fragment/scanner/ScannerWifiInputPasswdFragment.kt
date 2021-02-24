package kr.co.petdoc.petdoc.fragment.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scanner_wifi_input_password.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog

/**
 * Petdoc
 * Class: ScannerWifiInputPasswdFragment
 * Created by kimjoonsung on 2020/05/06.
 *
 * Description :
 */
class ScannerWifiInputPasswdFragment : Fragment() {

    private lateinit var dataModel: ConnectScannerDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, fullscreen =  true)
        dataModel = ViewModelProvider(requireActivity()).get(ConnectScannerDataModel::class.java)
        return inflater.inflate(R.layout.fragment_scanner_wifi_input_password, container, false)
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

        btnNext.setOnClickListener {
            dataModel.wifiPassword.value = editTextPasswd.text.toString()
            findNavController().navigate(ScannerWifiInputPasswdFragmentDirections.actionScannerWifiInputPasswdFragmentToScannerAPConnectFragment())
        }

        //===========================================================================================
        editTextPasswd.requestFocus()
        showKeyBoardOnView(editTextPasswd)
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

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    protected fun hideKeyboard(v: View) {
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
    }
}