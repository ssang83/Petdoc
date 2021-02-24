package kr.co.petdoc.petdoc.fragment.hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_booking_policy.*
import kotlinx.android.synthetic.main.fragment_booking_policy.policy_title_text
import kotlinx.android.synthetic.main.fragment_policy_content.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.login.PolicyContentInfo
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.web.NormalWebChromeViewClient
import kr.co.petdoc.petdoc.web.NormalWebViewClient
import kr.co.petdoc.petdoc.web.WebViewCallback

/**
 * Petdoc
 * Class: BookingPolicyFragment
 * Created by kimjoonsung on 2020/09/07.
 *
 * Description :
 */
class BookingPolicyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_booking_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        //=========================================================================================

        arguments?.let{
            val url = it.getString("url")

            webContent.apply{
                webViewClient = NormalWebViewClient(this)
                webChromeClient = NormalWebChromeViewClient(object: WebViewCallback {
                    override fun pageLoadedComplete() {
                        progressBar.visibility = View.GONE
                    }

                    override fun pageLoading() {
                        progressBar.visibility = View.VISIBLE
                    }
                })

                loadUrl(url)
            }
        }
    }
}