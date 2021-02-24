package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_terms.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import kr.co.petdoc.petdoc.web.NormalWebViewClient

/**
 * Petdoc
 * Class: TermsFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 서비스 이용약관 화면
 */
class TermsFragment : Fragment() {

    val TERMS_URL = "https://petdoc.co.kr/policy/service?isNoTitle=true"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_terms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        webView.apply{

            settings.apply {
                javaScriptEnabled = true
                loadsImagesAutomatically = true
                domStorageEnabled = true
                setAppCacheEnabled(true)
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                CookieManager.getInstance().apply{
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webView, true)
                }

                if(Build.VERSION.SDK_INT >= 26) {
                    setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                }else{
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest
                ): Boolean {
                    try {
                        val url = request.url.toString()
                        startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        )
                        return true
                    } catch (e: Exception) {}

                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
            webChromeClient = NormalWebChromeViewClient(null)

            loadUrl(TERMS_URL)
        }
    }

}