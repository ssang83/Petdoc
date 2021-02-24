package kr.co.petdoc.petdoc.fragment.petadd

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.ab180.airbridge.Airbridge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pet_information.*
import kotlinx.android.synthetic.main.fragment_pet_vaccine.*
import kotlinx.android.synthetic.main.fragment_pet_vaccine.layoutHeader
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.adapter.pet_add.VaccineStatusAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: PetVaccineFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 예방접종 등록 화면
 */
class PetVaccineFragment : BaseFragment(), VaccineStatusAdapter.AdapterListener {

    private val LOGTAG = "PetVaccineFragment"
    private val UPLOAD_VACCINE_STATUS_REQUEST = "$LOGTAG.uploadVaccineStatusRequest"

    private var statusList:MutableList<String> = mutableListOf()
    private var vaccineStatus = ""

    private lateinit var mAdapter: VaccineStatusAdapter
    private lateinit var dataModel: PetAddInfoDataModel

    private var petName = ""
    private var petInfoItem:PetInfoItem? = null

    private var defaultStatus = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_pet_vaccine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이의", "의").let {
            textViewPeVaccineDesc.text = "${it} 예방접종 여부를 선택해주세요."
        }

        petName = Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이에게", "에게")

        if(statusList.size > 0) {
            statusList.clear()
        }

        statusList.add("접종 전")
        statusList.add("접종 중")
        statusList.add("접종 완료")
        statusList.add("모름")

        mAdapter = VaccineStatusAdapter().apply {
            setListener(this@PetVaccineFragment)
        }

        vaccineStatusList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        btnComplete.setOnClickListener {
            if (dataModel.type.value == PetAddType.ADD.ordinal) {
                if (vaccineStatus.isEmpty()) {
                    AppToast(requireActivity()).showToastMessage(
                        requireActivity().resources.getString(R.string.pet_vaccine_status_confirm),
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                } else {
                    Airbridge.trackEvent("customeal", "click", "add_pet_done", null, null, null)
                    mApiClient.uploadPetVaccineStatus(UPLOAD_VACCINE_STATUS_REQUEST, dataModel.petId.value!!, vaccineStatus)
                }
            } else {
                mApiClient.uploadPetVaccineStatus(UPLOAD_VACCINE_STATUS_REQUEST, dataModel.petId.value!!, vaccineStatus)
            }
        }

        btnClose.setOnClickListener {
            dataModel.petVaccine.value = defaultStatus
            requireActivity().onBackPressed()
        }

        mAdapter.updateData(statusList)

        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE

            FirebaseAPI(requireActivity()).logEventFirebase("추가_접종", "Page View", "접종여부 입력 단계 페이지뷰")
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE

            btnComplete.text = Helper.readStringRes(R.string.confirm)
            btnComplete.setTextColor(Helper.readColorRes(R.color.orange))
            btnComplete.setBackgroundResource(R.drawable.main_color_round_rect)

            when (dataModel.petVaccine.value.toString()) {
                "before" -> mAdapter.setSelectPosition(0)
                "complete" -> mAdapter.setSelectPosition(2)
                "ing" -> mAdapter.setSelectPosition(1)
                "donKnow" -> mAdapter.setSelectPosition(3)
            }

            defaultStatus = dataModel.petVaccine.value.toString()
            FirebaseAPI(requireActivity()).logEventFirebase("추가_접종_수정", "Page View", "접종여부 입력 단계 수정 페이지뷰")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(UPLOAD_VACCINE_STATUS_REQUEST)) {
            mApiClient.cancelRequest(UPLOAD_VACCINE_STATUS_REQUEST)
        }
    }

    override fun onItemClicked(status: String) {
        vaccineStatus = getStatus(status)

        stepperFinish.apply {
            when {
                dataModel.type.value == PetAddType.ADD.ordinal -> {
                    visibility = View.VISIBLE
                    btnComplete.setTextColor(Helper.readColorRes(R.color.white))
                }
                else -> {
                    visibility = View.GONE
                    btnComplete.setTextColor(Helper.readColorRes(R.color.orange))
                }
            }
        } 

        dataModel.petVaccine.value = vaccineStatus
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
            UPLOAD_VACCINE_STATUS_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    if ("SUCCESS" == json["code"]) {
                        if (dataModel.type.value == PetAddType.ADD.ordinal) {
                            if (dataModel.petKind.value.toString() == "강아지") {
                                loadPetInfo()
                            } else {
                                requireActivity().finish()
                            }
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if (StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            if (items.isNotEmpty()) {
                for (item in items) {
                    if (item.id == dataModel.petId.value) {
                        petInfoItem = item
                        break
                    }
                }

                Logger.d("name : ${petInfoItem?.name}")

                TwoBtnDialog(requireContext()).apply {
                    setTitle("맞춤형 진단 안내")
                    setMessage("추가정보를 등록하면 ${petName}에게 꼭 맞는 맞춤식을 추천해 드립니다.")
                    setConfirmButton("진단받기", View.OnClickListener {
                        dismiss()
                        startActivity(
                            Intent(
                                requireActivity(),
                                MatchFoodActivity::class.java
                            ).apply {
                                putExtra("item", petInfoItem)
                            })
                        requireActivity().finish()
                    })
                    setCancelButton("다음에 할래요", View.OnClickListener {
                        dismiss()
                        requireActivity().finish()
                    })
                }.show()
            }
        }
    }

    private fun getStatus(status: String) =
        when (status) {
            "접종 전" -> "before"
            "접종 중" -> "ing"
            "접종 완료" -> "complete"
            "모름" -> "donKnow"
            else -> ""
        }
}