package kr.co.petdoc.petdoc.fragment.petadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_breed.*
import kotlinx.android.synthetic.main.fragment_pet_breed.btnClose
import kotlinx.android.synthetic.main.fragment_pet_breed.btnNext
import kotlinx.android.synthetic.main.fragment_pet_breed.layoutHeader
import kotlinx.android.synthetic.main.fragment_pet_breed.stepper
import kotlinx.android.synthetic.main.fragment_pet_name.*
import kotlinx.android.synthetic.main.fragment_pet_sex.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: PetBreedFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 품종 선택 화면
 */
class PetBreedFragment : BaseFragment() {

    private val TAG = "PetBreedFragment"
    private val SPECIES_UPOAD_REQUEST = "$TAG.speciesUploadRequest"

    companion object {
        lateinit var instance:PetBreedFragment
    }

    private lateinit var dataModel:PetAddInfoDataModel

    private var petBreedKnowFragment:PetBreedKnowFragment? = null
    private var petBreedNotKnowFragment:PetBreedNotKnowFragment? = null

    private var defaultBreed = ""
    private var defaultBreedKnow = false
    private var defaultBreedNotKnow = false
    private var defaultBreedId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_breed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)
        instance = this

        if(dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE
            btnNext.isEnabled = false

            FirebaseAPI(requireActivity()).logEventFirebase("추가_품종", "Page View", "품종 입력 단계 페이지뷰")
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE
            btnNext.isEnabled = true

            btnNext.text = Helper.readStringRes(R.string.confirm)
            btnNext.setTextColor(Helper.readColorRes(R.color.orange))
            btnNext.setBackgroundResource(R.drawable.main_color_round_rect)

            FirebaseAPI(requireActivity()).logEventFirebase("추가_품종_수정", "Page View", "품종 입력 단계 수정 페이지뷰")

            val breed = dataModel.petBreed.value.toString()
            if (breed.startsWith("강아지")
                    || breed == "고양이"
                    || breed.startsWith("소형")
                    || breed.startsWith("중형")
                    || breed.startsWith("대형")) {
                radioKnow.isChecked = false
                radioNotKnow.isChecked = true

                dataModel.petBreedNotKnow.value = true
                replacePetBreedNotKnowFragment()
            } else {
                radioKnow.isChecked = true
                radioNotKnow.isChecked = false

                dataModel.petBreedKnow.value = true
                replacePetBreedKnowFragment()
            }

            defaultBreed = dataModel.petBreed.value.toString()
            defaultBreedKnow = dataModel.petBreedKnow.value!!
            defaultBreedNotKnow = dataModel.petBreedNotKnow.value!!
            defaultBreedId = dataModel.petBreedId.value!!
        }

        Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이의", "의").let {
            textViewPetBreedDesc.text = "${it} 품종을 알려주세요."
        }

        layoutBreedKnow.setOnClickListener {
            radioKnow.isChecked = true
            radioNotKnow.isChecked = false

            setEnableNextBtn(false)
            replacePetBreedKnowFragment()
        }

        layoutBreedNotKnow.setOnClickListener {
            radioKnow.isChecked = false
            radioNotKnow.isChecked = true

            setEnableNextBtn(false)
            replacePetBreedNotKnowFragment()
        }

        btnNext.setOnClickListener {
            if (dataModel.type.value == PetAddType.ADD.ordinal) {
                Logger.d("petBreedNam : ${dataModel.petBreed.value}")
                if (dataModel.petBreed.value.toString().isEmpty()) {
                    AppToast(requireActivity()).showToastMessage(
                        requireActivity().resources.getString(R.string.pet_breed_confirm),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                } else {
                    mApiClient.uploadPetSpecies(SPECIES_UPOAD_REQUEST, dataModel.petId.value!!, dataModel.petBreed.value.toString(), dataModel.petBreedId.value!!)
                }
            } else {
                mApiClient.uploadPetSpecies(SPECIES_UPOAD_REQUEST, dataModel.petId.value!!, dataModel.petBreed.value.toString(), dataModel.petBreedId.value!!)
            }
        }

        btnClose.setOnClickListener {
            dataModel.petBreed.value = defaultBreed
            dataModel.petBreedKnow.value = defaultBreedKnow
            dataModel.petBreedNotKnow.value = defaultBreedNotKnow
            dataModel.petBreedId.value = defaultBreedId

            requireActivity().onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)

        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            if (dataModel.petBreedKnow.value!!) {
                radioKnow.isChecked = true
                radioNotKnow.isChecked = false

                replacePetBreedKnowFragment()
                setEnableNextBtn(true)
            } else if (dataModel.petBreedNotKnow.value!!) {
                radioKnow.isChecked = false
                radioNotKnow.isChecked = true

                replacePetBreedNotKnowFragment()
                setEnableNextBtn(true)
            } else {
                radioKnow.isChecked = true
                radioNotKnow.isChecked = false
                replacePetBreedKnowFragment()
            }
        }
    }

    fun setEnableNextBtn(status:Boolean) {
        if (status) {
            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)

                isEnabled = true
            }
        } else {
            btnNext.apply {
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.grey_round_rect)

                isEnabled = false
            }
        }
    }

    /**
     * 반려동물 품종을 아는 화면으로 전환
     */
    private fun replacePetBreedKnowFragment() {
        val transaction = childFragmentManager.beginTransaction()
        petBreedKnowFragment = childFragmentManager.findFragmentByTag(PetBreedKnowFragment::class.java.name) as? PetBreedKnowFragment
        if (petBreedKnowFragment == null) {
            petBreedKnowFragment = PetBreedKnowFragment()
        }

        transaction.replace(
            R.id.layoutContainer,
            petBreedKnowFragment!!,
            PetBreedKnowFragment::class.java.name
        )

        transaction.commit()
    }

    /**
     * 반려동물 품종을 모르는 화면으로 전환
     */
    private fun replacePetBreedNotKnowFragment() {
        val transaction = childFragmentManager.beginTransaction()
        petBreedNotKnowFragment = childFragmentManager.findFragmentByTag(PetBreedNotKnowFragment::class.java.name) as? PetBreedNotKnowFragment
        if (petBreedNotKnowFragment == null) {
            petBreedNotKnowFragment = PetBreedNotKnowFragment()
        }

        transaction.replace(
            R.id.layoutContainer,
            petBreedNotKnowFragment!!,
            PetBreedNotKnowFragment::class.java.name
        )

        transaction.commit()
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
        when (event.tag) {
            SPECIES_UPOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    if ("SUCCESS" == json["code"]) {
                        if (dataModel.type.value == PetAddType.ADD.ordinal) {
                            findNavController().navigate(PetBreedFragmentDirections.actionPetBreedFragmentToPetAgeFragment())
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }
}