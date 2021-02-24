package kr.co.petdoc.petdoc.fragment.petadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_sex.*
import kotlinx.android.synthetic.main.fragment_pet_sex.btnClose
import kotlinx.android.synthetic.main.fragment_pet_sex.btnNext
import kotlinx.android.synthetic.main.fragment_pet_sex.layoutHeader
import kotlinx.android.synthetic.main.fragment_pet_sex.stepper
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: PetSexFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 성별 등록 화면
 */
class PetSexFragment : BaseFragment() {

    companion object {
        const val KEY_IS_INCOMPLETE_STEP = "isInCompleteStep"
    }

    private val LOGTAG = "PetSexFragment"
    private val UPLOAD_GENER_REQUEST = "$LOGTAG.uploadGenderRequest"

    private var petGender = ""
    private var petNeutralization = false

    private lateinit var dataModel: PetAddInfoDataModel

    private val isInCompleteStep: Boolean? by lazy { arguments?.get(KEY_IS_INCOMPLETE_STEP) as? Boolean  }

    private var defaultGender = ""
    private var defaultNeutralization = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_pet_sex, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE

            FirebaseAPI(requireActivity()).logEventFirebase("추가_성별", "Page View", "성별 입력 단계 페이지뷰")

            btnNext.isEnabled = false
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE

            btnNext.text = Helper.readStringRes(R.string.confirm)
            btnNext.setTextColor(Helper.readColorRes(R.color.orange))
            btnNext.setBackgroundResource(R.drawable.main_color_round_rect)
            btnNext.isEnabled = true

            FirebaseAPI(requireActivity()).logEventFirebase("추가_성별_수정", "Page View", "성별 입력 단계 수정 페이지뷰")

            when (dataModel.petSex.value.toString()) {
                "man" -> {
                    radioMale.isChecked = true
                    radioFemale.isChecked = false

                    defaultGender = "man"
                }

                else -> {
                    radioMale.isChecked = false
                    radioFemale.isChecked = true

                    defaultGender = "woman"
                }
            }

            if (dataModel.petNeutralization.value == true) {
                radioComplete.isChecked = true
                radioBefore.isChecked = false

                defaultNeutralization = true
            } else {
                radioComplete.isChecked = false
                radioBefore.isChecked = true

                defaultNeutralization = false
            }
        }

        layoutBefore.setOnClickListener {
            petNeutralization = false

            radioComplete.isChecked = false
            radioBefore.isChecked = true

            dataModel.flagBox.value!!.set(1, true)
            dataModel.petNeutralization.value = petNeutralization

            checkBtnStatus()
        }

        layoutComplete.setOnClickListener {
            petNeutralization = true

            radioComplete.isChecked = true
            radioBefore.isChecked = false

            dataModel.flagBox.value!!.set(1, true)
            dataModel.petNeutralization.value = petNeutralization

            checkBtnStatus()
        }

        layoutMale.setOnClickListener {
            petGender = "man"

            radioMale.isChecked = true
            radioFemale.isChecked = false

            dataModel.flagBox.value!!.set(0, true)
            dataModel.petSex.value = petGender

            checkBtnStatus()
        }

        layoutFemale.setOnClickListener {
            petGender = "woman"

            radioMale.isChecked = false
            radioFemale.isChecked = true

            dataModel.flagBox.value!!.set(0, true)
            dataModel.petSex.value = petGender

            checkBtnStatus()
        }

        btnNext.setOnClickListener {
            if (dataModel.type.value == PetAddType.ADD.ordinal) {
                if (dataModel.flagBox.value!![0] && dataModel.flagBox.value!![1]) {
                    mApiClient.uploadPetGender(UPLOAD_GENER_REQUEST, dataModel.petId.value!!, dataModel.petSex.value.toString(), dataModel.petNeutralization.value!!)
                } else {
                    AppToast(requireActivity()).showToastMessage(
                        requireActivity().resources.getString(R.string.pet_sex_confirm),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            } else {
                mApiClient.uploadPetGender(UPLOAD_GENER_REQUEST, dataModel.petId.value!!, dataModel.petSex.value.toString(), dataModel.petNeutralization.value!!)
            }
        }

        btnClose.setOnClickListener {
            dataModel.petSex.value = defaultGender
            dataModel.petNeutralization.value = defaultNeutralization

            requireActivity().onBackPressed()
        }

        //==========================================================================================

        Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이의", "의").let {
            textViewPetSexDesc.text = "${it} 성별을 선택해주세요."
            textViewPetNeutralDesc.text = "${it} 중성화 여부를 선택해주세요."
        }

    }

    override fun onResume() {
        super.onResume()
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)

        if ((dataModel.flagBox.value!!.get(0) && dataModel.flagBox.value!!.get(1))) {
            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)

                isEnabled = true
            }
        }

        initCheckBoxState()
    }

    private fun initCheckBoxState() {
        if (isInCompleteStep == true ) {
            return
        }

        if (dataModel.petSex.value.toString().isNotEmpty()) {
            if (dataModel.petSex.value.toString() == "man") {
                radioMale.isChecked = true
                radioFemale.isChecked = false
            } else {
                radioMale.isChecked = false
                radioFemale.isChecked = true
            }
        }

        if (dataModel.petNeutralization.value != null) {
            if (dataModel.petNeutralization.value == true) {
                radioComplete.isChecked = true
                radioBefore.isChecked = false

                dataModel.flagBox.value!![1] = true
            } else {
                radioComplete.isChecked = false
                radioBefore.isChecked = true

                dataModel.flagBox.value!![1] = true
            }
        }

    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(UPLOAD_GENER_REQUEST)) {
            mApiClient.cancelRequest(UPLOAD_GENER_REQUEST)
        }
        super.onDestroyView()
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
    fun onEventMainThread(event:NetworkBusResponse) {
        when(event.tag) {
            UPLOAD_GENER_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    if ("SUCCESS" == json["code"]) {
                        if (dataModel.type.value == PetAddType.ADD.ordinal) {
                            findNavController().navigate(PetSexFragmentDirections.actionPetSexFragmentToPetVaccineFragment())
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun checkBtnStatus() {
        if ((dataModel.flagBox.value!!.get(0) && dataModel.flagBox.value!!.get(1))) {
            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)

                isEnabled = true
            }
        }
    }
}