package kr.co.petdoc.petdoc.fragment.scanner

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_temperature_calibration.*
import kotlinx.android.synthetic.main.dialog_temperature_calibration.title
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: CalibrationDialogFragment
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
class CalibrationDialogFragment(private val callback: CallbackListener) : DialogFragment() {

    private var petKind = ""
    private var bodyTemp = ""
    private var calibrationValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullDialog)
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
        return inflater.inflate(R.layout.dialog_temperature_calibration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        petKind = arguments?.getString("petKind") ?: petKind
        bodyTemp = arguments?.getString("bodyTemp") ?: bodyTemp
        Logger.d("petKind : $petKind, bodyTmep : $bodyTemp")

        btnCancel.setOnClickListener { dismiss() }
        btnComplete.setOnClickListener {
            when {
                btnComplete.text.toString() == Helper.readStringRes(R.string.care_body_temperature_calibration_check) -> {
                    layoutNoCalibration.visibility = View.GONE
                    layoutCalibraiton.visibility = View.VISIBLE

                    btnCancel.text = Helper.readStringRes(R.string.cancel)
                    btnComplete.text = Helper.readStringRes(R.string.care_body_temperature_calibration_confirm)

                    val value = if(petKind == "강아지") {
                        38.8 - bodyTemp.toDouble()
                    } else {
                        38.7 - bodyTemp.toDouble()
                    }

                    calibrationTemp.text = "+${String.format("%.1f", value)}˚C"
                    calibrationValue = String.format("%.1f", value)
                }

                else -> {
                    callback.onCalibartion(calibrationValue)
                    dismiss()
                }
            }
        }

        //=========================================================================================

        layoutNoCalibration.visibility = View.VISIBLE
        layoutCalibraiton.visibility = View.GONE

        title.text = if(petKind == "강아지") {
            "강아지의 표준 체온은"
        } else {
            "고양이의 표준 체온은"
        }

        temperature.text = if(petKind == "강아지") {
            "38.8˚C"
        } else {
            "38.7˚C"
        }

        calibrationTemperature.text = if(petKind == "강아지") {
            "38.8˚C"
        } else {
            "38.7˚C"
        }

        bodyTemperture.text = bodyTemp
        legacyTemperature.text = bodyTemp

    }

    interface CallbackListener {
        fun onCalibartion(value:String)
    }
}