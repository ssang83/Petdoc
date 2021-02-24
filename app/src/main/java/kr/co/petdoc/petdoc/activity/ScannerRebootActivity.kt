package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scanner_reboot.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.ReConnectScannerActivity
import kr.co.petdoc.petdoc.extensions.startActivity

class ScannerRebootActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_reboot)
        setUpViews()
    }

    private fun setUpViews() {
        btnConfirm.setOnClickListener {
            handleFinish()
        }
        btnBack.setOnClickListener {
            handleFinish()
        }
    }

    private fun handleFinish() {
        finish()
        startActivity<ReConnectScannerActivity>()
    }
}