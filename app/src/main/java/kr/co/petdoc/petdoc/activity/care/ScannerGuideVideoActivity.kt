package kr.co.petdoc.petdoc.activity.care

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_scanner_guide.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: ScannerGuideVideoActivity
 * Created by kimjoonsung on 2020/10/28.
 *
 * Description :
 */
class ScannerGuideVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_guide)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        val uri = intent?.getStringExtra("uri") ?: ""
        Logger.d("uri : $uri")

        btnClose.setOnClickListener { finish() }

        videoView.setVideoURI(Uri.parse(uri))
        videoView.setOnPreparedListener {
            it?.start()
        }
    }
}