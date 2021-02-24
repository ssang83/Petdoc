package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_info_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel

/**
 * Petdoc
 * Class: PetInfoDetailFragment
 * Created by kimjoonsung on 2020/04/09.
 *
 * Description : 반려동물 정보 상세 화면
 */
class PetInfoDetailFragment : Fragment() {

    private lateinit var petInfoModel: PetAddInfoDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        petInfoModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_info_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        layoutDefaultInfo.setOnClickListener {
            findNavController().navigate(R.id.action_petInfoDetailFragment2_to_petDefaultInfoFragment2)
        }

        layoutDianosis.setOnClickListener {
            //sungmin connecting between this and petfood recommend routine -  20200413
            findNavController().navigate(R.id.action_petInfoDetailFragment2_to_petFoodRecommendFragment3)
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
    }
}