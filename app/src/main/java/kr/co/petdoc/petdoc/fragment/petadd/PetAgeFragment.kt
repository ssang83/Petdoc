package kr.co.petdoc.petdoc.fragment.petadd

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_age.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.NumberPickerDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: PetAgeFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 나이 등록 화면
 */
class PetAgeFragment : BaseFragment(), NumberPicker.OnValueChangeListener {

    private val LOGTAG = "PetAgeFragment"
    private val UPLOAD_AGE_REQUEST = "$LOGTAG.uploadAgeRequest"

    enum class PickerType {
        PICKER_YEAR,
        PICKER_MONTH
    }

    private var petAge:String = ""
    private var petYear: String = ""
    private var petMonth:String = ""
    private var pickerType = PickerType.PICKER_YEAR

    private lateinit var dataModel:PetAddInfoDataModel

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
    private val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""
    private var currentDay = ""

    private var ageType = 0

    // 나이 수정전 기본 값 저장
    private var defaultBirth = ""
    private var defaultYear = ""
    private var defaultMonth = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_age, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))
        currentDay = dayFormat.format(Date(System.currentTimeMillis()))

        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE
            btnNext.isEnabled = false

            FirebaseAPI(requireActivity()).logEventFirebase("추가_나이", "Page View", "나이 입력 단계 페이지뷰")
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE

            btnNext.text = Helper.readStringRes(R.string.confirm)
            btnNext.setTextColor(Helper.readColorRes(R.color.orange))
            btnNext.setBackgroundResource(R.drawable.main_color_round_rect)
            btnNext.isEnabled = true

            FirebaseAPI(requireActivity()).logEventFirebase("추가_나이_수정", "Page View", "나이 입력 단계 수정 페이지뷰")

            if (dataModel.petAgeType.value == 1) {
                radioPicker.isChecked = false
                radioCalendar.isChecked = true

                textViewCalendarDate.text = dataModel.petBirth.value.toString()
                textViewCalendarDate.setTextColor(Helper.readColorRes(R.color.dark_grey))
            } else {
                radioPicker.isChecked = true
                radioCalendar.isChecked = false

                textViewPickerAge.text = "${dataModel.petYear.value}세"
                textViewPickerAge.setTextColor(Helper.readColorRes(R.color.dark_grey))
                textViewPickerMonth.text = "${dataModel.petMonth.value}개월"
                textViewPickerMonth.setTextColor(Helper.readColorRes(R.color.dark_grey))
            }

            defaultBirth = dataModel.petBirth.value.toString()
            defaultYear = dataModel.petYear.value.toString()
            defaultMonth = dataModel.petMonth.value.toString()

            petAge = defaultBirth
            petYear = defaultYear
            petMonth = defaultMonth
        }

        layoutCalendar.setOnClickListener {
            onRadioCalendarClicked()
        }

        layoutCalendarDate.setOnClickListener {
            onRadioCalendarClicked()
        }

        layoutPicker.setOnClickListener {
            radioCalendar.isChecked = false
            radioPicker.isChecked = true
        }

        layoutAge.setOnClickListener {
            pickerType = PickerType.PICKER_YEAR
            radioCalendar.isChecked = false
            radioPicker.isChecked = true

            showNumberPicker(getString(R.string.pet_age_picker_age_title))
        }

        layoutMonth.setOnClickListener {
            pickerType = PickerType.PICKER_MONTH
            radioCalendar.isChecked = false
            radioPicker.isChecked = true

            showNumberPicker(getString(R.string.pet_age_picker_month_title))
        }

        btnNext.setOnClickListener {
            if (dataModel.type.value == PetAddType.ADD.ordinal) {
                if (dataModel.petAge.value.toString().isEmpty()) {
                    AppToast(requireActivity()).showToastMessage(
                        requireActivity().resources.getString(R.string.pet_age_confirm),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                } else {
                    mApiClient.uploadPetAge(UPLOAD_AGE_REQUEST, dataModel.petId.value!!, petAge, dataModel.petAgeType.value!!)
                }
            } else {
                mApiClient.uploadPetAge(UPLOAD_AGE_REQUEST, dataModel.petId.value!!, petAge, dataModel.petAgeType.value!!)
            }
        }

        btnClose.setOnClickListener {
            dataModel.petBirth.value = defaultBirth
            dataModel.petYear.value = defaultYear
            dataModel.petMonth.value = defaultMonth

            requireActivity().onBackPressed()
        }

        //==========================================================================================
        Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이는", "는").let {
            textViewPetAgeDesc.text = "${it} 몇 살인가요?"
        }
    }

    override fun onResume() {
        super.onResume()
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            if (dataModel.petBirth.value.toString().isNotEmpty()) {
                radioPicker.isChecked = false
                radioCalendar.isChecked = true

                textViewCalendarDate.text = dataModel.petBirth.value.toString()
                textViewCalendarDate.setTextColor(Helper.readColorRes(R.color.dark_grey))

                btnNext.apply {
                    setTextColor(Helper.readColorRes(R.color.orange))
                    setBackgroundResource(R.drawable.main_color_round_rect)
                    isEnabled = true
                }

                return
            }

            if(dataModel.petYear.value.toString().isNotEmpty() && dataModel.petMonth.value.toString().isNotEmpty()) {
                radioPicker.isChecked = true
                radioCalendar.isChecked = false

                textViewPickerAge.text = "${dataModel.petYear.value}세"
                textViewPickerAge.setTextColor(Helper.readColorRes(R.color.dark_grey))
                textViewPickerMonth.text = "${dataModel.petMonth.value}개월"
                textViewPickerMonth.setTextColor(Helper.readColorRes(R.color.dark_grey))

                btnNext.apply {
                    setTextColor(Helper.readColorRes(R.color.orange))
                    setBackgroundResource(R.drawable.main_color_round_rect)
                    isEnabled = true
                }

                return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(UPLOAD_AGE_REQUEST)) {
            mApiClient.cancelRequest(UPLOAD_AGE_REQUEST)
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        Logger.d("value : ${picker?.value}")
        if (pickerType == PickerType.PICKER_YEAR) {
            textViewPickerAge.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${picker?.value}세"
            }

            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)
                isEnabled = true
            }

            petYear = picker?.value.toString()
            dataModel.petYear.value = petYear
        } else {
            textViewPickerMonth.apply {
                setTextColor(Helper.readColorRes(R.color.dark_grey))
                text = "${picker?.value}개월"
            }

            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)
                isEnabled = true
            }

            petMonth = picker?.value.toString()
            dataModel.petMonth.value = petMonth
        }

        if (petYear.isNotEmpty() && petMonth.isNotEmpty()) {
            calculateAge(petYear, petMonth)
        }
    }

    private fun onRadioCalendarClicked() {
        radioCalendar.isChecked = true
        radioPicker.isChecked = false

        val cal = Calendar.getInstance()
        DatePickerDialog(requireActivity(),
            { _, year, month, dayOfMonth ->
                petAge = String.format("%d-%02d-%02d", year, month+1, dayOfMonth)
                ageType = 1
                textViewCalendarDate.apply {
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    val date = petAge.split("-")
                    text = "${date[0]}년${date[1]}월${date[2]}일"

                    dataModel.petBirth.value = petAge
                    dataModel.petAge.value = petAge

                    val period = LocalDate.of(year, month + 1, dayOfMonth).until(LocalDate.now())
                    dataModel.petYear.value = period.years.toString()
                    dataModel.petMonth.value = period.months.toString()
                    dataModel.petAgeType.value = 1
                }

                btnNext.apply {
                    setTextColor(Helper.readColorRes(R.color.orange))
                    setBackgroundResource(R.drawable.main_color_round_rect)
                    isEnabled = true
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DATE)
        ).apply {
            datePicker.maxDate = cal.timeInMillis
        }.show()
    }

    private fun showNumberPicker(title: String) {
        val bundle = Bundle().apply {
            putString("title", title)
        }

        NumberPickerDialog().apply {
            arguments = bundle
            setValueChangeListener(this@PetAgeFragment)
        }.show(childFragmentManager, "Number Picker")
    }

    private fun calculateAge(year: String, month: String) {
        var estimatedBirth = LocalDate.now().minusYears(year.toLong())
        if (month.toInt() > 0) {
            estimatedBirth = estimatedBirth.minusMonths(month.toLong())
        }
        petAge = estimatedBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ageType = 2
        dataModel.petAgeType.value = 2
        dataModel.petAge.value = petAge

        Logger.d("petAge : $petAge")
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            UPLOAD_AGE_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    if ("SUCCESS" == json["code"]) {
                        if (dataModel.type.value == PetAddType.ADD.ordinal) {
                            findNavController().navigate(R.id.action_petAgeFragment_to_petSexFragment)
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }
}