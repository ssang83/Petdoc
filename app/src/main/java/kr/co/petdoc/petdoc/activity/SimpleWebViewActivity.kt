package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_simple_webview.*
import kr.co.petdoc.petdoc.R

class SimpleWebViewActivity : AppCompatActivity() {

    companion object {
        const val KEY_URL = "url"
        const val KEY_TITLE = "title"
    }

    private val url by lazy {
        intent.getStringExtra(KEY_URL)
    }
    private val title by lazy {
        intent.getStringExtra(KEY_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_webview)

        this.title?.let {
            tvTitle.text = title
        }

        initWebView()
        btnClose.setOnClickListener { finish() }
    }

    private fun initWebView() {
        webView.loadUrl(url)
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
    }
}