package kr.co.petdoc.petdoc.fragment.mypage.health_care

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_health_care_info.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel

/**
 * Petdoc
 * Class: HealthCareInfoDialogFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class HealthCareInfoDialogFragment : DialogFragment() {

    private lateinit var dataModel : MyPageInformationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullDialog)
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java)
        return inflater.inflate(R.layout.dialog_health_care_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitDate.text = dataModel.visitDate.value.toString()
        regDate.text = dataModel.resultDate.value.toString()
        type.text = "혈액"
        hospitalName.text = dataModel.centerName.value.toString()
    }

}