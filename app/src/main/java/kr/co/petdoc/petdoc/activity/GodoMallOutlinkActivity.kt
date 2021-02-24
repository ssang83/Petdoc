package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_godomall_outlink.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: GodoMallOutlinkActivity
 * Created by kimjoonsung on 12/3/20.
 *
 * Description :
 */
class GodoMallOutlinkActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_KEY_URL = "url"
    }

    private var loadUrl = ""
    private var siteUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha=0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_godomall_outlink)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        btnClose.setOnClickListener { finish() }
        btnGoBack.setOnClickListener { goBack() }
        btnGoForward.setOnClickListener { goForward() }
        btnRefresh.setOnClickListener { webView.reload() }
        btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, loadUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        //==========================================================================================
        initWebView()
        checkIntent()
    }

    override fun onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    private fun initWebView() {
        webView.apply {
            clearHistory()
            clearCache(true)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = false
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(true)
                cacheMode = WebSettings.LOAD_NO_CACHE
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                allowFileAccessFromFileURLs = true

                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Logger.d("Page Loading..")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Logger.d("Page Loaded : ${view?.title}")
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url
                    loadUrl = url.toString()
                    Logger.d("Page Url : ${url}")

                    return false
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    Logger.d("Page url : $url")
                    loadUrl = url.toString()
                    return false
                }
            }
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
    }

    private fun checkIntent() {
        siteUrl = intent?.getStringExtra(INTENT_EXTRA_KEY_URL) ?: ""
        Logger.d("godo mall url : $siteUrl")

        try {
            webView.loadUrl(siteUrl)
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    private fun goBack() {
        btnGoBack.apply {
            when {
                webView.canGoBack() == true -> {
                    setBackgroundResource(R.drawable.ic_go_back)
                    webView.goBack()
                }

                else -> setBackgroundResource(R.drawable.ic_go_back_disable)
            }
        }
    }

    private fun goForward() {
        btnGoForward.apply {
            when {
                webView.canGoForward() == true -> {
                    setBackgroundResource(R.drawable.ic_go_forward)
                    webView.goForward()
                }

                else -> setBackgroundResource(R.drawable.ic_go_forward_disable)
            }
        }
    }
}