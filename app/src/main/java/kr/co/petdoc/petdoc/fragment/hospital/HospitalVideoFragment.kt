package kr.co.petdoc.petdoc.fragment.hospital

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_only_webview.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import kr.co.petdoc.petdoc.web.NormalWebViewClient

/**
 * Petdoc
 * Class: HospitalVideoFragment
 * Created by kimjoonsung on 2020/09/03.
 *
 * Description :
 */
class HospitalVideoFragment : Fragment() {

    private var videoUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_only_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoUrl = arguments?.getString("videoUrl") ?: videoUrl
        Logger.d("videoUrl : $videoUrl")

        //============================================================================================
        webview_area.apply{

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
                    setAcceptThirdPartyCookies(webview_area, true)
                }

                if(Build.VERSION.SDK_INT >= 26) {
                    setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
                }else{
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                }
            }

            webViewClient = NormalWebViewClient(this)
            webChromeClient = NormalWebChromeViewClient(null)

            loadUrl(videoUrl)
        }
    }
}