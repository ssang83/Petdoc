package kr.co.petdoc.petdoc.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_policy_content.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import kr.co.petdoc.petdoc.web.NormalWebViewClient
import kr.co.petdoc.petdoc.web.WebViewCallback
import java.io.Serializable

/**
 * petdoc-android
 * Class: PolicyWebContentFragment
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 약관 내용 웹뷰 연결
 */
class PolicyWebContentFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_policy_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)


        //get Argument -----------------------------------------------------------------------------
        arguments?.let{
            val data = it.getSerializable("showDataInfo") as PolicyContentInfo

            policy_title_text.text = data.title
            policy_web_content.apply{
                webViewClient = NormalWebViewClient(this)
                webChromeClient = NormalWebChromeViewClient(object:WebViewCallback{
                    override fun pageLoadedComplete() {
                        policy_web_progress.visibility = View.GONE
                    }

                    override fun pageLoading() {
                        policy_web_progress.visibility = View.VISIBLE
                    }
                })

                loadUrl(data.url)
            }
        }


        policy_close_button.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }

}


data class PolicyContentInfo(var url:String, var title:String) : Serializable