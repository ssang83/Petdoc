package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_marketing_agree.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import kr.co.petdoc.petdoc.web.NormalWebViewClient

/**
 * Petdoc
 * Class: MarketingAgreeFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 마케팅 수신 동의 안내 화면
 */
class MarketingAgreeFragment : Fragment() {

    val MARKETING_AGREE_URL = "https://auth.petdoc.co.kr/policyType1"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_marketing_agree, container, false)
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

            webViewClient = NormalWebViewClient(this)
            webChromeClient = NormalWebChromeViewClient(null)

            loadUrl(MARKETING_AGREE_URL)
        }
    }
}