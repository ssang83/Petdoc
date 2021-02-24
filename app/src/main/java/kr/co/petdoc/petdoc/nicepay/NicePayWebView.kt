package kr.co.petdoc.petdoc.nicepay

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.webkit.*
import com.google.gson.Gson
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R
import okhttp3.*
import java.net.URISyntaxException
import java.util.*

class NicePayWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): WebView(context, attrs, defStyleAttr) {
    companion object {
        const val SCHEME = "petdoc"
        const val PURCHASE_SUCCESS = "success"
        const val PURCHASE_FAIL = "fail"
        const val PURCHASE_CANCEL = "cancel"
    }

    private lateinit var config: NicePayConfig

    private var listener: NicePayWebViewListener? = null

    private var initComplete = false

    init {
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        CookieManager.getInstance().setAcceptCookie(true)

        settings.domStorageEnabled = true
        setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        setNetworkAvailable(true)
        isFocusable = true
        isFocusableInTouchMode = true

        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true

        @SuppressWarnings("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true

        webViewClient = PurchaseWebViewClient()
        webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                view?.run {
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogStyle)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            result?.confirm()
                        }
                        .setCancelable(true)
                        .create()
                        .show()
                }

                return true
            }

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                view?.run {
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogStyle)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            result?.confirm()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            result?.cancel()
                        }
                        .setCancelable(true)
                        .create()
                        .show()
                }

                return true
            }
        }
    }

    fun load(config: NicePayConfig) {
        this.config = config
        loadUrl(BuildConfig.PAYMENT_URL + "app/order")
    }

    fun setPurchaseWebViewListener(listener: NicePayWebViewListener) {
        this.listener = listener
    }

    inner class PurchaseWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url?.let {
                if (url.startsWith(SCHEME)) {
                    val removedPrefix = url.replaceFirst("$SCHEME://", "").toLowerCase(Locale.getDefault())
                    if (removedPrefix.startsWith(PURCHASE_SUCCESS)) {
                        val orderNumber = removedPrefix.replaceFirst("$PURCHASE_SUCCESS/", "")
                        listener?.onPurchaseComplete(orderNumber)
                    } else {//if (removedPrefix.startsWith(PURCHASE_FAIL) or (removedPrefix.startsWith(PURCHASE_CANCEL))) {
                        destroyChildWebView()
                    }
                } else {
                    return handleUrl(view, url)
                }
            }

            return true
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val url = request!!.url.toString()
            if (initComplete) {
                if (url.startsWith(SCHEME)) {
                    overrideUrlLoading(url)
                    return null
                }
                return super.shouldInterceptRequest(view, request)
            }
            return requestPurchase(url)
        }

        private fun overrideUrlLoading(url: String) {
            if (url.startsWith(SCHEME)) {
                val removedPrefix = url.replaceFirst("$SCHEME://", "").toLowerCase(Locale.getDefault())
                if (removedPrefix.startsWith(PURCHASE_SUCCESS)) {
                    destroyChildWebView()

                    val ticketManageNumber = removedPrefix.replaceFirst("$PURCHASE_SUCCESS/", "")
                    listener?.onPurchaseComplete(ticketManageNumber)
                } else if (removedPrefix.startsWith(PURCHASE_FAIL) or (removedPrefix.startsWith(PURCHASE_CANCEL))) {
                    destroyChildWebView()
                }
            }
        }

        private fun handleUrl(view: WebView?, url: String?): Boolean {
            try {
                if( url != null && (url.startsWith("intent:")
                            || url.contains("market://")
                            || url.contains("vguard")
                            || url.contains("droidxantivirus")
                            || url.contains("v3mobile")
                            || url.contains(".apk")
                            || url.contains("mvaccine")
                            || url.contains("smartwall://")
                            || url.contains("http://m.ahnlab.com/kr/site/download")) ) {

                    var intent: Intent? = null

                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    } catch (e: URISyntaxException) {
                        println("error : " + e.printStackTrace())
                    }

                    if( view?.context?.packageManager?.resolveActivity(intent, 0) == null ) {
                        val pkgName = intent?.`package`
                        if( pkgName != null ) {
                            val uri = Uri.parse("market://search?q=pname:" + pkgName)
                            intent = Intent(Intent.ACTION_VIEW, uri)
                            view?.context?.startActivity(intent)
                        }
                    } else {
                        val uri = Uri.parse(intent?.dataString)
                        intent = Intent(Intent.ACTION_VIEW, uri)
                        view.context?.startActivity(intent)
                    }
                } else {
                    view?.loadUrl(url)
                }
            } catch (e: Exception) {
                return false
            }

            return true
        }

        private fun destroyChildWebView() {
            /*childWebView?.let {
                it.post {
                    it.destroy()
                    (this@PurchaseWebView.parent as ViewGroup).removeView(it)
                }
            }
            childWebView = null*/
        }

        private fun requestPurchase(url: String): WebResourceResponse? {
            val httpClient = OkHttpClient()

            val jsonMessage = Gson().toJson(config)

            val requestBody = RequestBody.create(MediaType.parse("application/json"), jsonMessage)
            val request = Request.Builder()
                .url(url.trim())
                .post(requestBody)
                .build()

            val response: Response
            try {
                response = httpClient.newCall(request).execute()
            } catch (e: Exception) {
                return null
            }

            return WebResourceResponse(
                null,
                response.header("content-encoding", "utf-8"),
                response.body()?.byteStream()
            )
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            listener?.initComplete()
            initComplete = true
        }

    }

    interface NicePayWebViewListener {
        fun initComplete()
        fun onPurchaseComplete(orderNumber: String)
        fun closeByForce()
    }
}