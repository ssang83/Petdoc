package kr.co.petdoc.petdoc.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_two_button.buttonCancel
import kotlinx.android.synthetic.main.dialog_two_button.buttonConfirm
import kr.co.petdoc.petdoc.R

class FirmwareUpgradeCompleteDialog(
    context: Context
) : AppCompatDialog(context, false, null) {

    private var listener: FirmwareUpgradeDialogListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_firmware_upgrade_complete)

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
            listener?.onConnectScannerBtnClicked()
        }
        buttonCancel.setOnClickListener {
            listener?.onCancelClicked()
            dismiss()
        }
    }

    fun setFirmwareUpgradeCompleteDialogListener(listener: FirmwareUpgradeDialogListener) {
        this.listener = listener
    }

    interface FirmwareUpgradeDialogListener {
        fun onConnectScannerBtnClicked()
        fun onCancelClicked()
    }
}