package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.dialog_find_id_result.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: FindIDResultDialogFragment
 * Created by kimjoonsung on 2020/07/30.
 *
 * Description :
 */
class FindIDResultDialogFragment(private val callback:CallbackListener?) : AppCompatDialogFragment() {

    private var message = ""
    private var callType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialog)
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_find_id_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        message = arguments?.getString("message") ?: message
        callType = arguments?.getString("callType") ?: callType

        resultDesc.text = message

        if (callType == "auth") {
            btnLogin.text = "확인"
            btnLogin.setOnClickListener { callback?.onDismiss() }
        } else {
            btnLogin.text = "로그인 하기"
            btnLogin.setOnClickListener { requireActivity().onBackPressed() }
        }
    }

    interface CallbackListener {
        fun onDismiss()
    }
}