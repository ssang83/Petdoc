package kr.co.petdoc.petdoc.fragment.petadd

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pet_breed_know.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.pet_add.BreedSearchAdapter
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetKindData
import kr.co.petdoc.petdoc.network.response.submodel.PetSpeciesData
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetBreedKnowFragment
 * Created by kimjoonsung on 2020/04/07.
 *
 * Description : 반려동물 등록 시 품종을 알 경우 보여지는 화면
 */
class PetBreedKnowFragment : BaseFragment(), BreedSearchAdapter.AdapterListener {

    private val LOGTAG = "PetBreedKnowFragment"
    private val PET_BREED_LIST_REQUEST_TAG = "$LOGTAG.petBreedListRequest"

    private var items:MutableList<PetSpeciesData> = mutableListOf()

    private lateinit var mAdapter: BreedSearchAdapter
    private lateinit var dataModel: PetAddInfoDataModel

    private var isClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_breed_know, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = BreedSearchAdapter().apply {
            setListener(this@PetBreedKnowFragment)
        }

        petBreedList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        editPetBreed.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.isNotBlank()) {
                    PetBreedFragment.instance.setEnableNextBtn(true)
                } else {
                    PetBreedFragment.instance.setEnableNextBtn(false)
                }
                mApiClient.getPetSpeciesList(PET_BREED_LIST_REQUEST_TAG, dataModel.petKindKey.value.toString(), s.toString().trim())
            }
        })

        btnTop.setOnClickListener { petBreedList.scrollToPosition(0) }

        if (dataModel.petBreed.value.toString().isNotEmpty()) {
            if (dataModel.petBreedKnow.value == true) {
                editPetBreed.setText(dataModel.petBreed.value.toString())
            }
        }
    }

    override fun onItemClicked(item: PetSpeciesData) {
        Logger.d("petBreed : ${item.name}")
        dataModel.petBreed.value = item.name
        dataModel.petBreedId.value = item.id
        dataModel.petBreedKnow.value = true
        dataModel.petBreedNotKnow.value = false

        isClicked = true

        editPetBreed.setText(item.name)
        mAdapter.updateData(null)

        PetBreedFragment.instance.setEnableNextBtn(true)
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
            PET_BREED_LIST_REQUEST_TAG -> {
                if ("ok" == event.status) {
                    if(isClicked) {
                        mAdapter.updateData(null)
                        isClicked = false
                    } else {
                        items = Gson().fromJson(event.response, object : TypeToken<MutableList<PetSpeciesData>>() {}.type)
                        if (items.isNullOrEmpty()) {
                            textViewEmpty.visibility = View.VISIBLE
                        } else {
                            textViewEmpty.visibility = View.GONE
                        }
                        mAdapter.updateData(items)
                    }
                }
            }
        }
    }
}