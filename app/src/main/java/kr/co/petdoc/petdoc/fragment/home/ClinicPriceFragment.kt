package kr.co.petdoc.petdoc.fragment.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_clinic_price.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ClinicPriceDataModel
import kr.co.petdoc.petdoc.widget.NumberPickerDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast

/**
 * Petdoc
 * Class: ClinicPriceFragment
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class ClinicPriceFragment : Fragment(), NumberPicker.OnValueChangeListener {

    enum class PickerType {
        PICKER_YEAR,
        PICKER_MONTH
    }

    private lateinit var clinicDataModel: ClinicPriceDataModel

    private val flagBox = booleanArrayOf(false,false)

    private var pickerType = PickerType.PICKER_YEAR
    private var petYear = ""
    private var petMonth = ""
    private var petWeight = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        clinicDataModel = ViewModelProvider(requireActivity()).get(ClinicPriceDataModel::class.java)
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_clinic_price, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }

        //===========================================================================================

        radioMale.setOnClickListener {
            clinicDataModel.petGender.value = "M"

            flagBox[0] = true
            checkNextBtn()
        }

        radioFemale.setOnClickListener {
            clinicDataModel.petGender.value = "F"

            flagBox[0] = true
            checkNextBtn()
        }

        radioDog.setOnClickListener {
            clinicDataModel.petKind.value = "dog"

            flagBox[1] = true
            checkNextBtn()
        }

        radioCat.setOnClickListener {
            clinicDataModel.petKind.value = "cat"

            flagBox[1] = true
            checkNextBtn()
        }

        layoutAge.setOnClickListener {
            pickerType = PickerType.PICKER_YEAR
            showNumberPicker(getString(R.string.pet_age_picker_age_title))
        }

        layoutMonth.setOnClickListener {
            pickerType = PickerType.PICKER_MONTH
            showNumberPicker(getString(R.string.pet_age_picker_month_title))
        }

        btnNext.setOnSingleClickListener {
            clinicDataModel.petWeight.value = petWeight.toString()
            Airbridge.trackEvent("home", "click", "price_petinfo", null, null, null)
            findNavController().navigate(ClinicPriceFragmentDirections.actionClinicPriceFragmentToClinicPriceCategoryFragment())
        }

        //==========================================================================================

        editWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return
                if (s.toString().startsWith(".")) return
                if (s.toString().startsWith("-")) return

                if (s.toString().length != 0) {
                    val lastChar = s.toString().substring(s.toString().length - 1)
                    if (lastChar == ".") {
                        btnNext.isEnabled = false

                        btnNext.apply {
                            setTextColor(Helper.readColorRes(R.color.light_grey3))
                            setBackgroundResource(R.drawable.grey_round_rect)
                        }

                        return
                    }
                }

                if (s.toString().toFloat() > 50f) {
                    AppToast(requireActivity()).showToastMessage(
                        "50kg이하만 입력 가능합니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    return
                }

                petWeight = s.toString().toFloat()
                Logger.d("petWeight : $petWeight")
                checkNextBtn()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! == 0) {
                    btnNext.isEnabled = false

                    btnNext.apply {
                        setTextColor(Helper.readColorRes(R.color.light_grey3))
                        setBackgroundResource(R.drawable.grey_round_rect)
                    }
                }
            }
        })

        btnNext.isEnabled = false

        if (clinicDataModel.petYear.value != null) {
            textViewPickerAge.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${clinicDataModel.petYear.value}세"
            }
        }
        if (clinicDataModel.petMonth.value != null) {
            textViewPickerMonth.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${clinicDataModel.petMonth.value}개월"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNextBtn()
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        Logger.d("value : ${picker?.value}")
        if (pickerType == PickerType.PICKER_YEAR) {
            textViewPickerAge.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${picker?.value}세"
            }

            petYear = picker?.value.toString()
            clinicDataModel.petYear.value = picker?.value

            checkNextBtn()
        } else {
            textViewPickerMonth.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${picker?.value}개월"
            }

            petMonth = picker?.value.toString()
            clinicDataModel.petMonth.value = picker?.value

            checkNextBtn()
        }
    }

    private fun showNumberPicker(title: String) {
        val bundle = Bundle().apply {
            putString("title", title)
            putInt("maxValue", 30)
        }

        NumberPickerDialog().apply {
            arguments = bundle
            setValueChangeListener(this@ClinicPriceFragment)
        }.show(childFragmentManager, "Number Picker")
    }

    private fun checkNextBtn() {
        if (flagBox[0] && flagBox[1] &&
            petYear.isNotEmpty() &&
            petMonth.isNotEmpty() &&
            ((petWeight > 0f && petWeight <= 50f))) {
            btnNext.isEnabled = true

            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)
            }
        } else {
            btnNext.isEnabled = false

            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.grey_round_rect)
            }
        }
    }
}