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
import kotlinx.android.synthetic.main.fragment_species_list.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.pet_add.BreedSearchAdapter
import kr.co.petdoc.petdoc.adapter.pet_add.SpeciesAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetSpeciesData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: PetSpeciesListFragment
 * Created by kimjoonsung on 2020/07/03.
 *
 * Description :
 */
class PetSpeciesListFragment : BaseFragment() , SpeciesAdapter.AdapterListener{

    private val TAG = "PetSpeciesListFragment"
    private val SPECIES_LIST_REQUEST = "$TAG.speciesListRequest"
    private val SPECIES_UPOAD_REQUEST = "$TAG.speciesUploadRequest"

    private lateinit var dataModel: PetAddInfoDataModel
    private lateinit var mAdapter: SpeciesAdapter

    private var items:MutableList<PetSpeciesData> = mutableListOf()

    private var defaultBreed = ""
    private var defaultBreedId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_species_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnNext.isSelected = false
        if(dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE

            FirebaseAPI(requireActivity()).logEventFirebase("추가_품종", "Page View", "품종 입력 단계 페이지뷰")
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE

            btnNext.text = Helper.readStringRes(R.string.confirm)
            btnNext.isSelected = true

            defaultBreed = dataModel.petBreed.value.toString()
            defaultBreedId = dataModel.petBreedId.value!!

            FirebaseAPI(requireActivity()).logEventFirebase("추가_품종_수정", "Page View", "품종 입력 단계 수정 페이지뷰")
        }

        textViewPetBreedDesc.text = "${dataModel.petName.value.toString()}의 품종을 알려주세요."

        //==========================================================================================
        btnNext.setOnClickListener {
            if (dataModel.type.value == PetAddType.ADD.ordinal) {
                Logger.d("petBreedNam : ${dataModel.petBreed.value}")
                if (!it.isSelected) {
                    AppToast(requireActivity()).showToastMessage(
                            requireActivity().resources.getString(R.string.pet_breed_confirm)
                    )
                    return@setOnClickListener
                }
            }

            mApiClient.uploadPetSpecies(
                SPECIES_UPOAD_REQUEST,
                dataModel.petId.value!!,
                dataModel.petBreed.value.toString(),
                dataModel.petBreedId.value!!)
        }

        btnClose.setOnClickListener {
            dataModel.petBreedId.value = defaultBreedId
            dataModel.petBreed.value = defaultBreed

            requireActivity().onBackPressed()
        }
        //===========================================================================================

        mAdapter = SpeciesAdapter().apply {
            setListener(this@PetSpeciesListFragment)
        }

        speciesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mApiClient.getPetSpeciesList(SPECIES_LIST_REQUEST, dataModel.petKindKey.value.toString(), "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(SPECIES_UPOAD_REQUEST)) {
            mApiClient.cancelRequest(SPECIES_UPOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(SPECIES_LIST_REQUEST)) {
            mApiClient.cancelRequest(SPECIES_LIST_REQUEST)
        }
    }

    override fun onItemClicked(item: PetSpeciesData) {
        Logger.d("petBreed : ${item.name}")
        dataModel.petBreed.value = item.name
        dataModel.petBreedId.value = item.id

        btnNext.isSelected = true
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
            SPECIES_LIST_REQUEST -> {
                if ("ok" == event.status) {
                    items = Gson().fromJson(event.response, object : TypeToken<MutableList<PetSpeciesData>>() {}.type)
                    mAdapter.updateData(items)

                    if(dataModel.petBreed.value.toString().isNotEmpty()) {
                        val selectedPosition = items.indexOfFirst { it.name == dataModel.petBreed.value.toString() }
                        if (selectedPosition != -1) {
                            btnNext.isSelected = true
                            mAdapter.setPosition(selectedPosition)
                        }
                    }
                }
            }

            SPECIES_UPOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val json = JSONObject(event.response)
                    if ("SUCCESS" == json["code"]) {
                        if (dataModel.type.value == PetAddType.ADD.ordinal) {
                            findNavController().navigate(PetSpeciesListFragmentDirections.actionPetSpeciesListFragmentToPetAgeFragment())
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }
}