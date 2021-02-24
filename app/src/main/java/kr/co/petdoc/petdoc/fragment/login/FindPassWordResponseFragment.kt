package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_find_password_response.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * petdoc-android
 * Class: FindPassWordResponseFragment
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 로그인 액티비티 -> 비밀번호찾기(이메일) -> 인증코드 확인,  현재는 네비게이터에 등록되어 있음
 */
class FindPassWordResponseFragment : Fragment() {

    private var emailAddress = ""

    private lateinit var screenSizeListener : ViewTreeObserver.OnGlobalLayoutListener
    private val screenChangeRect = Rect()
    private var availableHeight = 0
    private var checkAuthCode = false


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

//        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.let{
            emailAddress = it.getString("emailAddress") ?: ""
        }

        return inflater.inflate(R.layout.fragment_find_password_response, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        availableHeight = Helper.screenSize(requireActivity())[1] / 100 * 85
        screenSizeListener = ViewTreeObserver.OnGlobalLayoutListener {
            find_password_confirm_code_canvas.getWindowVisibleDisplayFrame(screenChangeRect)
            find_password_confirm_button_area.apply{
                layoutParams = layoutParams.apply{
                    height = if(screenChangeRect.bottom - screenChangeRect.top < availableHeight) 0 else Helper.convertDPResourceToPx(requireContext(), R.dimen.button_height_default)
                }
            }
        }

        readyUIandEvent()
    }

    override fun onResume() {
        super.onResume()
        find_password_confirm_code_canvas.viewTreeObserver.addOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onPause() {
        super.onPause()
        find_password_confirm_code_canvas.viewTreeObserver.removeOnGlobalLayoutListener(screenSizeListener)
    }


    private fun readyUIandEvent(){

        find_password_email_input_email_text.text = emailAddress

        find_password_confirm_code_back_button.setOnClickListener { requireActivity().onBackPressed() }


        // text watchers ---------------------------------------------------
        find_password_code_input_text.let{ edit ->
            edit.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int ) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkConfirmState()
                }
            })
        }


        find_password_confirm_button.setOnClickListener {
            if(checkAuthCode){
                //check auth code is right from user email process ............................
                // if true ->
                // move to password change screen
            }
        }
    }


    private fun checkConfirmState(){
        find_password_confirm_button.apply{
            if(find_password_code_input_text.text.length > 3){
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
                checkAuthCode = true
            }else{
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
                checkAuthCode = false
            }
        }
    }

}