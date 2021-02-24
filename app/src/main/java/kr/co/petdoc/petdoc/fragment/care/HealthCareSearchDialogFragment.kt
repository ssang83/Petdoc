package kr.co.petdoc.petdoc.fragment.care

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_health_care_search.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: HealthCareSearchDialogFragment
 * Created by kimjoonsung on 2020/09/23.
 *
 * Description :
 */
class HealthCareSearchDialogFragment(callback:CallbackListener) : DialogFragment() {

    private var callbackListener: CallbackListener = callback

    interface CallbackListener {
        fun onSearch()
        fun onPurchase()
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_health_care_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnHospital.setOnClickListener {
            dismiss()
            callbackListener.onSearch()
        }
        btnPurchase.setOnClickListener {
            dismiss()
            callbackListener.onPurchase()
        }

    }
}