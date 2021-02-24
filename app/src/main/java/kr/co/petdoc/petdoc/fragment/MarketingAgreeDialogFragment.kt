package kr.co.petdoc.petdoc.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_marketing_agree.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: MarketingAgreeDialogFragment
 * Created by kimjoonsung on 2020/10/06.
 *
 * Description :
 */
class MarketingAgreeDialogFragment(val callback:CallbackListener) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return inflater.inflate(R.layout.dialog_marketing_agree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnCancel.setOnClickListener {
            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, true)
            dismiss()
        }
        btnAgree.setOnClickListener {
            callback.marketingAgree()
            dismiss()
        }
    }

    interface CallbackListener {
        fun marketingAgree()
    }
}