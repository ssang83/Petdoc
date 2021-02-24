package kr.co.petdoc.petdoc.fragment.petadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pet_kind.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.pet_add.PetKindAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.network.response.submodel.PetKindData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: PetKindFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 종류 선택 화면
 */
class PetKindFragment : BaseFragment(), PetKindAdapter.AdapterListener {

    private val LOGTAG = "PetNameFragment"
    private val PET_KIND_LIST_REQUEST_TAG = "$LOGTAG.petKindListRequest"
    private val PET_KIND_UPLOAD_REQUEST = "$LOGTAG.petKindUploadRequest"

    private lateinit var dataModel:PetAddInfoDataModel

    private var items:MutableList<PetKindData> = mutableListOf()
    private var petKindName = ""
    private var petKindKey = ""

    private lateinit var mAdapter: PetKindAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_kind, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        stepper.apply {
            when {
                dataModel.type.value == PetAddType.ADD.ordinal -> {
                    visibility = View.VISIBLE
                    FirebaseAPI(requireActivity()).logEventFirebase("추가_종류", "Page View", "종류 입력 단계 페이지뷰")
                }
                else -> {
                    visibility = View.GONE
                    FirebaseAPI(requireActivity()).logEventFirebase("추가_종류_수정", "Page View", "종류 입력 단계 수정 페이지뷰")
                }
            }
        }

        Helper.getCompleteWordByJongsung(dataModel.petName.value.toString(), "이는", "는").let {
            textViewPetKindDesc.text = "${it} 어떤 반려동물인가요?"
        }

        mAdapter = PetKindAdapter().apply {
            setListener(this@PetKindFragment)
        }

        petKindList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        btnNext.setOnClickListener {
            if (dataModel.petKind.value.toString().isEmpty()) {
                AppToast(requireActivity()).showToastMessage(
                    requireActivity().resources.getString(R.string.pet_kind_confirm),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            } else {
                mApiClient.uploadPetKind(PET_KIND_UPLOAD_REQUEST, dataModel.petId.value!!, dataModel.petKind.value.toString())
            }
        }

        mApiClient.getPetKindList(PET_KIND_LIST_REQUEST_TAG)

        btnNext.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PET_KIND_LIST_REQUEST_TAG)) {
            mApiClient.cancelRequest(PET_KIND_LIST_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(PET_KIND_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(PET_KIND_UPLOAD_REQUEST)
        }
        super.onDestroyView()
    }

    override fun onItemClicked(item:PetKindData) {
        Logger.d("petKindName : ${item.name}, petKindKey : ${item.key}")
        petKindName = item.name
        petKindKey = item.key

        dataModel.petKind.value = petKindName
        dataModel.petKindKey.value = petKindKey

        btnNext.apply {
            setTextColor(Helper.readColorRes(R.color.orange))
            setBackgroundResource(R.drawable.main_color_round_rect)
            isEnabled = true
        }
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
            PET_KIND_LIST_REQUEST_TAG -> {
                if ("ok" == event.status) {
                    items = Gson().fromJson(event.response, object : TypeToken<MutableList<PetKindData>>() {}.type)
                    mAdapter.updateData(items)

                    if(dataModel.petKind.value.toString().isNotEmpty()) {
                        var position = -1
                        for(i in 0 until items.size) {
                            if(items[i].name == dataModel.petKind.value.toString()) {
                                position = i
                                break
                            }
                        }

                        mAdapter.setPosition(position)

                        btnNext.apply {
                            setTextColor(Helper.readColorRes(R.color.orange))
                            setBackgroundResource(R.drawable.main_color_round_rect)
                            isEnabled = true
                        }
                    }
                }
            }

            PET_KIND_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    try {
                        if ("SUCCESS" == json["code"]) {
                            val obj = json.getJSONObject("resultData")
                            val petKind = obj["petKind"].toString()

                            dataModel.petKind.value = petKind
                            dataModel.petKindKey.value = getKindKey(petKind)

                            if (dataModel.petKind.value.toString() == "강아지" || dataModel.petKind.value.toString() == "고양이") {
                                findNavController().navigate(PetKindFragmentDirections.actionPetKindFragmentToPetBreedFragment())
                            } else {
                                findNavController().navigate(PetKindFragmentDirections.actionPetKindFragmentToPetSpeciesListFragment())
                            }
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    private fun getKindKey(kind: String) =
        when (kind) {
            "강아지" -> "dog"
            "고양이" -> "cat"
            "햄스터" -> "hamster"
            "토끼" -> "rabbit"
            "고슴도치" -> "hedgehog"
            "거북이" -> "turtle"
            "기니피그" -> "guinea_pig"
            "조류" -> "bird"
            "패릿" -> "ferret"
            else -> ""
        }
}