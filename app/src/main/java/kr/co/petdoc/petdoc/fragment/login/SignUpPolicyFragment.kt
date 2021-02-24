package kr.co.petdoc.petdoc.fragment.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_signup_policy.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel


/**
 * petdoc-android
 * Class: RegisterPolicyFragment
 * Created by sungminkim on 2020/04/06.
 *
 * Description : 로그인 액티비티 상에서 가입 절차 진입 시 약관 동의 프래그먼트로 이동됨,  현재는 네비게이터에 등록되어 있음
 */
class SignUpPolicyFragment : Fragment(){

    lateinit var authorization : AuthorizationDataModel

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        authorization = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)
        return inflater.inflate(R.layout.fragment_signup_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        readyUIandEvent()
        readyPolicyLinks()
    }



    // ----------------------------------------------------------------------------------------------
    private fun readyUIandEvent(){
        register_policy_back_button.setOnClickListener { requireActivity().onBackPressed() }
        register_policy_cancel_button.setOnClickListener {
            Airbridge.trackEvent("signup", "click", "disagree_이용약관", null, null, null)
            requireActivity().onBackPressed()
        }
        register_policy_allow_button.setOnClickListener {
            Airbridge.trackEvent("signup", "click", "agree_이용약관", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("가입_약관동의", "Click Event", "약관 동의 버튼 클릭")
            if (authorization.type.value.toString() == "email") {
                val action = SignUpPolicyFragmentDirections.actionSignUpPolicyFragmentToSignUpByEmailFragment()
                findNavController().navigate(action)
            } else {
                findNavController().navigate(SignUpPolicyFragmentDirections.actionSignUpPolicyFragmentToSnsUserExtraInfoFragment())
            }
        }


    }

    // ----------------------------------------------------------------------------------------------
    private fun readyPolicyLinks(){

        policy_check_content_hyperlink.text.apply{
            (this as Spannable).let{span ->
                span.setSpan(object:ClickableSpan(){
                    override fun onClick(widget: View) {
//                        val action = SignUpPolicyFragmentDirections.actionSignUpPolicyFragmentToPolicyWebContentFragment(
//                            PolicyContentInfo("https://petdoc.co.kr/policy/service?isNoTitle=true", requireContext().getString(R.string.policy_term))
//                        )
//                        findNavController().navigate(action)

                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://petdoc.co.kr/policy/service?isNoTitle=true")))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Helper.readColorRes(R.color.blue_grey)
                    }
                }, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            (this as Spannable).let{span ->
                span.setSpan(object:ClickableSpan(){
                    override fun onClick(widget: View) {
//                        val action = SignUpPolicyFragmentDirections.actionSignUpPolicyFragmentToPolicyWebContentFragment(
//                            PolicyContentInfo("https://petdoc.co.kr/policy/privacy?isNoTitle=true", requireContext().getString(R.string.policy_private_info))
//                        )
//                        findNavController().navigate(action)

                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://petdoc.co.kr/policy/privacy?isNoTitle=true")))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Helper.readColorRes(R.color.blue_grey)
                    }
                }, 12, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            (this as Spannable).let{span ->
                span.setSpan(object:ClickableSpan(){
                    override fun onClick(widget: View) {
//                        val action = SignUpPolicyFragmentDirections.actionSignUpPolicyFragmentToPolicyWebContentFragment(
//                            PolicyContentInfo("https://auth.petdoc.co.kr/policyType2?isNoTitle=true", requireContext().getString(R.string.policy_location))
//                        )
//                        findNavController().navigate(action)

                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://auth.petdoc.co.kr/policyType2?isNoTitle=true")))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Helper.readColorRes(R.color.blue_grey)
                    }
                }, 28, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        policy_check_content_hyperlink.movementMethod = LinkMovementMethod.getInstance()

    }
}