package kr.co.petdoc.petdoc.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_firmware_download.*
import kotlinx.android.synthetic.main.dialog_two_button.buttonCancel
import kotlinx.android.synthetic.main.dialog_two_button.buttonConfirm
import kotlinx.android.synthetic.main.dialog_two_button.message
import kotlinx.android.synthetic.main.dialog_two_button.title
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.scanner.FirmwareVersionState

class FirmwareDownloadDialog(
    context: Context,
    private var firmwareVersionState: FirmwareVersionState
) : AppCompatDialog(context, false, null) {

    private var listener: FirmwareDownloadDialogListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_firmware_download)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.setLayout(width, height)
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)

        buttonConfirm.setOnClickListener {
            if (firmwareVersionState == FirmwareVersionState.UPDATE_NEEDED) {
                listener?.onDownloadBtnClicked()
            } else {
                listener?.onInstallBtnClicked()
            }
        }
        buttonCancel.setOnClickListener {
            dismiss()
        }
        resetViewByState(firmwareVersionState)
    }

    fun resetViewByState(firmwareVersionState: FirmwareVersionState) {
        this.firmwareVersionState = firmwareVersionState
        when(firmwareVersionState) {
            FirmwareVersionState.UPDATE_NEEDED -> {
                ivIcon.setImageResource(R.drawable.ic_firmware_download)
                title.text = "펌웨어 다운로드"
                message.text = "신규 펌웨어를 발견했습니다.\n다운로드하시겠습니까?"
                buttonConfirm.text = "다운로드"
            }
            FirmwareVersionState.DOWNLOADED -> {
                ivIcon.setImageResource(R.drawable.ic_firmware_download_complete)
                title.text = "펌웨어 다운로드 완료"
                message.text = "펌웨어 다운로드를 완료했습니다.\n펌웨어를 설치하시겠습니까?"
                buttonConfirm.text = "펌웨어 설치"
            }
            else -> {}
        }
    }

    fun setFirmwareDownloadDialogListener(listener: FirmwareDownloadDialogListener) {
        this.listener = listener
    }

    interface FirmwareDownloadDialogListener {
        fun onDownloadBtnClicked()
        fun onInstallBtnClicked()
    }
}