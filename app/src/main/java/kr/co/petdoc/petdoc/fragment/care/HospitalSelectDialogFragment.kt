package kr.co.petdoc.petdoc.fragment.care

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.dialog_hospital_select.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * Petdoc
 * Class: HospitalSelectDialogFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HospitalSelectDialogFragment(callback:CallbackListener) : DialogFragment() {

    private lateinit var dataModel:HospitalDataModel

    private var hospitalItem:HospitalItem? = null
    private var callbackListener:CallbackListener = callback

    interface CallbackListener {
        fun onHospitalDetail()
        fun onBooking()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(true)
        return inflater.inflate(R.layout.dialog_hospital_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hospitalItem = dataModel.hospitalItem.value

        btnCancel.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener {
            callbackListener.onBooking()
            dismiss()
        }
        layoutHospitalInfo.setOnClickListener {
            callbackListener.onHospitalDetail()
            dismiss()
        }

        //===========================================================================================
        message.text = "${hospitalItem?.name}으로 예약하시겠어요?"
        hospitalName.text = hospitalItem?.name
        location.text = hospitalItem?.location
        distance.apply {
            when {
                hospitalItem?.distance!! < 1000 -> text = "${hospitalItem?.distance}m"
                else -> {
                    val kilometer: Float = hospitalItem?.distance!!.toFloat() / 1000
                    val distnce = String.format("%.1f km", kilometer)
                    text = distnce
                }
            }
        }
    }
}