package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_password_change.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import java.util.regex.Pattern


/**
 * petdoc-android
 * Class: PasswordChangeFragment
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 비밀번호 변경 화면
 */
class PasswordChangeFragment : Fragment() {


    private lateinit var screenSizeListener : ViewTreeObserver.OnGlobalLayoutListener
    private val screenChangeRect = Rect()
    private var availableHeight = 0
    private var passwordChangable = booleanArrayOf(false,false)

    private val passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&.]).{8,16}$") // 영문, 숫자, 특수문자 조합



    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

//        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return inflater.inflate(R.layout.fragment_password_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        availableHeight = Helper.screenSize(requireActivity())[1] / 100 * 85
        screenSizeListener = ViewTreeObserver.OnGlobalLayoutListener {
            password_change_canvas.getWindowVisibleDisplayFrame(screenChangeRect)
            password_change_button_area.apply{
                layoutParams = layoutParams.apply{
                    height = if(screenChangeRect.bottom - screenChangeRect.top < availableHeight) 0 else Helper.convertDPResourceToPx(requireContext(), R.dimen.button_height_default)
                }
            }
        }

        readyUIandEvent()
    }


    override fun onResume() {
        super.onResume()
        password_change_canvas.viewTreeObserver.addOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onPause() {
        super.onPause()
        password_change_canvas.viewTreeObserver.removeOnGlobalLayoutListener(screenSizeListener)
    }



    private fun readyUIandEvent(){
        password_change_back_button.setOnClickListener { requireActivity().onBackPressed() }

        password_change_input_email_text.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int ) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordChangable[0] = passwordPattern.matcher(s?:"").matches()
                checkConfirmState()
            }
        })

        password_change_input_email_text2.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int ) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordChangable[1] = s.toString() == password_change_input_email_text.text.toString()
                checkConfirmState()
            }
        })

        password_change_button.setOnClickListener {
            if(passwordChangable[0] && passwordChangable[1]) {
                //todo : password change api call
                requireActivity().onBackPressed()
            }
        }
    }

    private fun checkConfirmState(){
        password_change_button.apply {
            if (passwordChangable[0] && passwordChangable[1]) {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }
        }
    }

}