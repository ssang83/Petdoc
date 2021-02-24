package kr.co.petdoc.petdoc.activity

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseActivity
import kr.co.petdoc.petdoc.databinding.ActivityFirmwareUpgradeBinding
import kr.co.petdoc.petdoc.dialog.FirmwareUpgradeCompleteDialog
import kr.co.petdoc.petdoc.dialog.FirmwareUpgradeProgressDialog
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FirmwareUpgradeViewModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.koin.android.viewmodel.ext.android.viewModel

class FirmwareUpgradeActivity
    : PetdocBaseActivity<ActivityFirmwareUpgradeBinding, FirmwareUpgradeViewModel>() {

    private val viewModel: FirmwareUpgradeViewModel by viewModel()

    override fun getTargetViewModel() = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId() = R.layout.activity_firmware_upgrade

    private var curDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)

        with(viewModel) {
            restart()

            isChargerConnected.observe(this@FirmwareUpgradeActivity, Observer { isChargerConnected ->
                if (!isChargerConnected) {
                    showConnectChargerAlertDialog()
                }
            })

            showUpgradeProgressDialog.observe(this@FirmwareUpgradeActivity, Observer { version ->
                curDialog = FirmwareUpgradeProgressDialog(this@FirmwareUpgradeActivity, version).also { it.show() }
            })

            showUpgradeCompleteDialog.observe(this@FirmwareUpgradeActivity, Observer { isSuccess ->
                curDialog?.dismiss()
                curDialog = FirmwareUpgradeCompleteDialog(this@FirmwareUpgradeActivity).apply {
                    setFirmwareUpgradeCompleteDialogListener(object: FirmwareUpgradeCompleteDialog.FirmwareUpgradeDialogListener {
                        override fun onConnectScannerBtnClicked() {
                            startActivity<ScannerRebootActivity>()
                            finish()
                        }

                        override fun onCancelClicked() {
                            finish()
                        }
                    })
                }.also { it.show() }
            })
        }
    }

    private fun showConnectChargerAlertDialog() {
        TwoBtnDialog(this).apply {
            setTitle("펌웨어 업데이트")
            setMessage("충전기가 연결된 상태에서만 업그레이드가 가능합니다.")
            setCancelable(false)
            setConfirmButton("연결하기", View.OnClickListener {
                viewModel.restart()
                dismiss()
            })
            setCancelButton("다음에 하기", View.OnClickListener {
                finish()
                dismiss()
            })
        }.show()
    }
}