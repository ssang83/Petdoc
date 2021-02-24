package kr.co.petdoc.petdoc.fragment.login

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_only_webview.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import java.net.URISyntaxException

/**
 * petdoc-android
 * Class: AuthorizationFragment
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 본인 확인 인증 관련 PASS 웹 페이지 처리용 프래그먼트
 */
class AuthorizationFragment : Fragment() {
    companion object {
        const val SKT_SCHEME = "tauthlink"
        const val KT_SCHEME = "ktauthexternalcall"
        const val LG_UPLUS_SCHEME = "upluscorporation"

        const val SKT_PACKAGE = "com.sktelecom.tauth"
        const val KT_PACKAGE = "com.kt.ktauth"
        const val LG_UPLUS_PACKAGE = "com.lguplus.smartotp"

        const val SKT_INSTALL_URL = "https://play.google.com/store/apps/details?id=com.sktelecom.tauth"
        const val KT_INSTALL_URL = "https://play.google.com/store/apps/details?id=com.kt.ktauth"
        const val LGU_PLUS_INSTALL_URL = "https://play.google.com/store/apps/details?id=com.lguplus.smartotp"
    }

    lateinit var authorization : AuthorizationDataModel

    private var callback : String = ""

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        authorization = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)

        arguments?.apply{
            callback = this.getString("callback", "")
        }

        return inflater.inflate(R.layout.fragment_only_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

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

            webViewClient = PhoneNumberAuthWebViewClient()
            webChromeClient = NormalWebChromeViewClient(null)

            val uri = Uri.Builder().scheme("https").authority("auth.petdoc.co.kr").appendPath("userConfirmApp").appendQueryParameter("redirectURL", callback).build()
            loadUrl(uri.toString())
        }
    }

    inner class PhoneNumberAuthWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest?
        ): Boolean {
            request?.let {
                overrideUrlLoading(view, it.url.toString())
            }
            return true
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            url?.let {
                overrideUrlLoading(view, url)
            }
            return true
        }

        private fun overrideUrlLoading(view: WebView, url: String) {
            if(url.startsWith("petdoc://") ){
                try {
                    val changeUrl = url.replace("petdoc://", "petdoc://app.android.data/auth")  // 테스트 용도
                    Logger.d("changeUrl : $changeUrl")
                    val deeplink = Intent(Intent.ACTION_VIEW, Uri.parse(changeUrl))
                    deeplink.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                    PetdocApplication.application.applicationContext.startActivity(deeplink)
                } catch (e: Exception) {
                    Logger.p(e)
                }
            } else if (url == SKT_INSTALL_URL ||
                url == KT_INSTALL_URL ||
                url == LGU_PLUS_INSTALL_URL) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                view.context.startActivity(intent)
            } else {
                try {
                    if (url.startsWith("intent://") ||
                        url.startsWith(SKT_SCHEME) ||
                        url.startsWith(KT_SCHEME) ||
                        url.startsWith(LG_UPLUS_SCHEME)) {
                        view.context.startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME))
                    } else {
                        view.loadUrl(url)
                    }
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                } catch (e: ActivityNotFoundException) {
                    if (url.startsWith(SKT_SCHEME)) {
                        startActivity(view.context, getMarketUri(SKT_PACKAGE))
                    } else if (url.startsWith(KT_SCHEME)) {
                        startActivity(view.context, getMarketUri(KT_PACKAGE))
                    } else if (url.startsWith(LG_UPLUS_SCHEME)) {
                        startActivity(view.context, getMarketUri(LG_UPLUS_PACKAGE))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun getMarketUri(packageName: String) = Uri.parse("market://details?id=$packageName")

        private fun startActivity(context: Context, uri: Uri) {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(marketIntent)
            } catch (e: Exception) {}
        }

    }
}