package kr.co.petdoc.petdoc.fragment.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_scanner_intro.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ConnectScannerActivity
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import java.net.InetAddress

/**
 * Petdoc
 * Class: ScannerIntroFragment
 * Created by kimjoonsung on 2020/04/29.
 *
 * Description :
 */
class ScannerIntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, fullscreen =  true)
        return inflater.inflate(R.layout.fragment_scanner_intro, container, false)
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

        btnNo.setOnSingleClickListener {
            AppToast(requireContext()).showToastMessage(
                "펫닥 스캐너 연결이 중단되었습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            requireActivity().onBackPressed()
        }
        btnYes.setOnSingleClickListener {
            findNavController().navigate(R.id.action_scannerIntroFragment_to_scannePowerOnFragment)
        }

        //============================================================================================

    }
}