package kr.co.petdoc.petdoc.web

import android.R
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.provider.Browser
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.log.Logger
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Petdoc
 * Class: NormalWebViewClient
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 기본 WebViewClient
 */
//class NormalWebViewClient(webView: WebView, webViewMode:UIMode, callback: ClientCallback) : WebViewClient() {
class NormalWebViewClient(webView: WebView) : WebViewClient() {

    //private val mCallback:ClientCallback = callback
    private val mWebView:WebView = webView
    //private var currentUrlLocation = ""

   /* enum class UIMode{
        MODE_ISOLATE,
        MIDE_LIST
    }*/

    //private var mWebViewMode:UIMode = webViewMode

    /*override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        super.onPageStarted(view, url, favicon)

        if (!url.startsWith("source://")) {
            currentUrlLocation = url
        }

        mCallback.onPageStarted(view)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)

        if (url.startsWith("source:")) {
            if (currentUrlLocation != null) {
                mWebView.loadUrl(currentUrlLocation)
            }
        }

        mCallback.onPageFinished(mWebView)
    }*/

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        Logger.d("url : $url")

        if (url.startsWith("http://") || url.startsWith("HTTP://")
            || url.startsWith("https://") || url.startsWith("HTTPS://")) {

            /*if (mWebViewMode == UIMode.MIDE_LIST) {
                // Scenario : List MODE 에서는 현재 WebView 상에서 URL 이동을 하지 않는다.
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                mWebView.context.startActivity(intent)

                return true
            }*/

            return false
        }
        // sungmin added 20200407 -- deeplink send outside -----------------------------------------
        else if( url.startsWith("petdoc://") ){
            try {
                val changeUrl = url.replace("petdoc://", "petdoc://app.android.data/auth")  // 테스트 용도
                Logger.d("changeUrl : $changeUrl")
                val deeplink = Intent(Intent.ACTION_VIEW, Uri.parse(changeUrl))
                deeplink.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                PetdocApplication.application.applicationContext.startActivity(deeplink)
            } catch (e: Exception) {
                Logger.p(e)
            }

            return true
        }
        //------------------------------------------------------------------------------------------
        else if (url.startsWith("source::")) {    // 수신된 HTML Source 코드 확인
            try {
                val html = URLDecoder.decode(url, "UTF-8")

                // Received Source
                Logger.d("HTML Source code (" + URLDecoder.decode(url, "UTF-8") + ") : \n $html")
            } catch (e: UnsupportedEncodingException) {
                Logger.p(e)
            } catch (e: IllegalArgumentException) {
                Logger.p(e)
            }

            return true
        } else if (url.startsWith("vnd.youtube:")) {
            val n = url.indexOf("?")
            if (n > 0) {
                mWebView.context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(String.format("http://www.youtube.com/v/%s", url.substring("vnd.youtube:".length, n)))))
            }

            return true
        } else {
            var override = false

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                putExtra(Browser.EXTRA_APPLICATION_ID, mWebView.context.packageName)
            }

            if (url.startsWith("sms:")) {   // sms
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                if (mWebView.context != null) {
                    mWebView.context.startActivity(i)
                }

                return true
            } else if (url.startsWith("tel:")) {    // Call
                // TODO : 전화걸기(TBD)
                return true
            } else if (url.startsWith("mailto:")) { // E-mail
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                if (mWebView.context != null) {
                    mWebView.context.startActivity(i)
                }

                return true
            }  else if (url.startsWith("market:")) {    // PlayStore
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }

                if (mWebView.context != null) {
                    mWebView.context.startActivity(i)
                }

                return true
            } else if (url.startsWith("kakaolink:")) {  // KakaoTalk
                try {
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    }

                    if (mWebView.context != null) {
                        mWebView.context.startActivity(i)
                    }

                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.e(e.toString())
                }
            } else {
                try {
                    if (mWebView.context != null) {
                        mWebView.context.startActivity(intent)
                        override = true
                    }
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    return override
                }
            }
        }

        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val sb = StringBuilder()

        when (error.primaryError) {
            SslError.SSL_EXPIRED -> {
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")
            }

            SslError.SSL_IDMISMATCH -> {
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")
            }

            SslError.SSL_NOTYETVALID -> {
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")
            }

            SslError.SSL_UNTRUSTED -> {
                sb.append("이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n")
            }

            else -> {
                sb.append("보안 인증서에 오류가 있습니다.\n")
            }
        }

        val builder = AlertDialog.Builder(mWebView.getContext())
        builder.setTitle("SSL 보안경고")
            .setMessage(sb.toString())
            .setPositiveButton(
                R.string.ok
            ) { dialog: DialogInterface, which: Int ->
                handler.proceed()
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog: DialogInterface, which: Int ->
                handler.cancel()
                dialog.dismiss()
            }
            .create().show()
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        super.onScaleChanged(view, oldScale, newScale)
    }

    /**
     * 해당 화면의 HTML 소스코드 부분을 WebView 상에서 확인
     */
    fun viewSource() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);")
        }
    }

    /*interface ClientCallback {
        *//**
         * Page Loading 시작 됨
         *//*
        fun onPageStarted(webView: WebView)

        *//**
         * Page Loading 종료 됨
         *//*
        fun onPageFinished(webView: WebView)
    }*/
}

class NormalWebChromeViewClient(var callback:WebViewCallback?) : WebChromeClient(){
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        if(newProgress==100) callback?.pageLoadedComplete()
        else callback?.pageLoading()
    }
}

interface WebViewCallback{
    fun pageLoadedComplete()
    fun pageLoading()
}