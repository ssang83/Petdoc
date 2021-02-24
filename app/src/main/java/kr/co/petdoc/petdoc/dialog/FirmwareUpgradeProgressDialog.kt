package kr.co.petdoc.petdoc.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_firmware_upgrade_progress.*
import kr.co.petdoc.petdoc.R
import java.util.concurrent.TimeUnit

private const val MAX_FIRMWARE_PROGRESS = 190

class FirmwareUpgradeProgressDialog(
    context: Context,
    private val version: String
) : AppCompatDialog(context, false, null) {
    private var updateInterval: Disposable? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_firmware_upgrade_progress)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.setLayout(width, height)
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)

        updateInterval = Observable.interval(500L, TimeUnit.MILLISECONDS)
            .subscribe {
                updateProgress(it.toInt())
            }

        tvMessage.text = context.getString(R.string.firmware_progress_dialog_description, version)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (updateInterval?.isDisposed == true) {
            updateInterval?.dispose()
        }
    }

    fun setProgressMax() {
        updateProgress(MAX_FIRMWARE_PROGRESS)
    }

    private fun updateProgress(progress: Int) {
        progressBar.progress = progress
    }
}