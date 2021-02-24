package kr.co.petdoc.petdoc.fragment.petadd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_pet_breed_not_know.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.pet_add.PetWeightAdapter
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel

/**
 * Petdoc
 * Class: PetBreedNotKnowFragment
 * Created by kimjoonsung on 2020/04/07.
 *
 * Description : 반려동물 등록 시 품종을 모를경우 화면
 */
class PetBreedNotKnowFragment : Fragment(), PetWeightAdapter.AdapterListener {

    private var items:MutableList<PetBreedData> = mutableListOf()

    private lateinit var mAdapter: PetWeightAdapter
    private lateinit var dataModel: PetAddInfoDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_breed_not_know, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.clear()
        if (dataModel.petKind.value.toString() == "강아지") {
            items.add(PetBreedData(
                104,
                "dog",
                "소형 0~10kg"
            ))

            items.add(PetBreedData(
                104,
                "dog",
                "중형 10~25kg"
            ))

            items.add(PetBreedData(
                104,
                "dog",
                "대형 25kg이상"
            ))
        } else {
            items.add(PetBreedData(
                436,
                "cat",
                "고양이"
            ))
        }

        mAdapter = PetWeightAdapter().apply {
            setListener(this@PetBreedNotKnowFragment)
            setPetName(dataModel.petName.value.toString())
        }

        petWeightList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mAdapter.updateData(items)
    }

    override fun onResume() {
        super.onResume()

        if (dataModel.petKind.value.toString() == "강아지") {
            if (dataModel.petBreed.value.toString().contains("소형")) {
                mAdapter.setPosition(0)
                PetBreedFragment.instance.setEnableNextBtn(true)
            } else if (dataModel.petBreed.value.toString().contains("중형")) {
                mAdapter.setPosition(1)
                PetBreedFragment.instance.setEnableNextBtn(true)
            } else if (dataModel.petBreed.value.toString().contains("대형")) {
                mAdapter.setPosition(2)
                PetBreedFragment.instance.setEnableNextBtn(true)
            }
        } else {
            if (dataModel.petBreedNotKnow.value == true) {
                mAdapter.setPosition(0)
                PetBreedFragment.instance.setEnableNextBtn(true)
            }
        }
    }

    override fun onItemClicked(item: PetBreedData) {
        Logger.d("name : ${item.name}, id : ${item.id}")
        dataModel.petBreed.value = item.name
        dataModel.petBreedId.value = item.id
        dataModel.petBreedNotKnow.value = true
        dataModel.petBreedKnow.value = false

        PetBreedFragment.instance.setEnableNextBtn(true)
    }
}

data class PetBreedData(
    var id:Int,
    var knd:String,
    var name:String
)