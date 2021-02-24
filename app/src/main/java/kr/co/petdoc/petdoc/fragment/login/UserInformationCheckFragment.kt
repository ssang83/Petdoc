package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_userinformation_check.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.databinding.FragmentUserinformationCheckBinding
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel


/**
 * petdoc-android
 * Class: UserInformationCheckFragment
 * Created by sungminkim on 2020/04/08.
 *
 * Description : 본인 확인 인증 관련 PASS 웹 페이지 처리 이후, 본인 데이터를 출력하는 화면으로 변경
 */

class UserInformationCheckFragment : Fragment() {

    private lateinit var authorization : AuthorizationDataModel


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

//        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)

        authorization = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)

        val dataBinding = FragmentUserinformationCheckBinding.inflate(layoutInflater, container, false)
        dataBinding.lifecycleOwner = requireActivity()
        dataBinding.authdata = authorization

        return dataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        authorization.apply{
            sex.observe(requireActivity(), Observer {

                userinformation_gender_male_text.apply{
                    setTextColor(Helper.readColorRes(R.color.color8d95a0))
                    setBackgroundResource(R.drawable.male_button_background_off)
                    typeface = Typeface.DEFAULT
                }
                userinformation_gender_female_text.apply{
                    setTextColor(Helper.readColorRes(R.color.color8d95a0))
                    setBackgroundResource(R.drawable.female_button_background_off)
                    typeface = Typeface.DEFAULT
                }

                when(it){
                    "M" -> {
                        userinformation_gender_male_text.apply{
                            setTextColor(Helper.readColorRes(R.color.white))
                            setBackgroundResource(R.drawable.male_button_background_on)
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                    "F" -> {
                        userinformation_gender_female_text.apply{
                            setTextColor(Helper.readColorRes(R.color.white))
                            setBackgroundResource(R.drawable.female_button_background_on)
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                    else -> {}
                }
            })
        }


        find_password_confirm_button.setOnClickListener {
            //todo api 연결,  회원가입 완료 신호 처리 해야 함
            requireActivity().finish()
        }

        userinfomation_check_backbutton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

}