package kr.co.petdoc.petdoc.fragment.mypage.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sick_search_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.databinding.FragmentSickSearchDetailBinding
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel

class PetSicknessDetailFragment : Fragment(){

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var modeObserver : Observer<Int>? = null
    private var dataObserver : Observer<Array<HashSet<Int>>>? = null


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentSickSearchDetailBinding>(inflater, R.layout.fragment_sick_search_detail, container, false)
            databinding.lifecycleOwner = requireActivity()
            databinding.foodreference = foodRecommendDataModel

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        modeObserver = Observer<Int> { modeChange(foodRecommendDataModel?.sickDetailMode?.value?:0) }
        dataObserver = Observer { checkBoxChange() }

        foodRecommendDataModel?.sickDetailMode?.observe(viewLifecycleOwner, modeObserver!!)
        foodRecommendDataModel?.sickDetailData?.observe(viewLifecycleOwner, dataObserver!!)


        petfood_recommend_sickness_select_1.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 1) }
        petfood_recommend_sickness_select_1_text?.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 1) }

        petfood_recommend_sickness_select_2.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 2) }
        petfood_recommend_sickness_select_2_text?.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 2) }

        petfood_recommend_sickness_select_3.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 3) }
        petfood_recommend_sickness_select_3_text?.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 3) }

        petfood_recommend_sickness_select_4.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 4) }
        petfood_recommend_sickness_select_4_text?.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 4) }

        petfood_recommend_sickness_select_5.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 5) }
        petfood_recommend_sickness_select_5_text?.setOnClickListener { checkBox(foodRecommendDataModel?.sickDetailMode?.value ?: -1, 5) }


        petfood_recomment_sickness_save_button?.setOnClickListener {
            if((foodRecommendDataModel?.sickDetailMode?.value?:0) == 5){

                modeObserver?.let{ foodRecommendDataModel?.sickDetailMode?.removeObserver(it) }
                dataObserver?.let{ foodRecommendDataModel?.sickDetailData?.removeObserver(it) }

                foodRecommendDataModel?.sickDetailMode?.value = 6
//                findNavController().navigate(PetSicknessDetailFragmentDirections.actionSicklistToSickresult())
            }else{
                foodRecommendDataModel?.sickDetailMode?.value = (foodRecommendDataModel?.sickDetailMode?.value?:0) + 1
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        modeObserver?.let{ foodRecommendDataModel?.sickDetailMode?.removeObserver(it) }
        dataObserver?.let{ foodRecommendDataModel?.sickDetailData?.removeObserver(it) }
    }





    private fun modeChange(mode:Int){

        checkBoxChange()

        petfood_recommend_sickness_guide.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_question)
            1 -> Helper.readStringRes(R.string.detail_search_skin_question)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_question)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_question)
            4 -> Helper.readStringRes(R.string.detail_search_bean_question)
            5 -> Helper.readStringRes(R.string.detail_search_fat_question)
            else -> Helper.readStringRes(R.string.detail_search_joint_question)
        }

        petfood_recommend_sickness_select_1_text.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_no1)
            1 -> Helper.readStringRes(R.string.detail_search_skin_no1)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_no1)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_no1)
            4 -> Helper.readStringRes(R.string.detail_search_bean_no1)
            5 -> Helper.readStringRes(R.string.detail_search_fat_no1)
            else -> Helper.readStringRes(R.string.detail_search_joint_no1)
        }
        petfood_recommend_sickness_select_2_text.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_no2)
            1 -> Helper.readStringRes(R.string.detail_search_skin_no2)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_no2)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_no2)
            4 -> Helper.readStringRes(R.string.detail_search_bean_no2)
            5 -> Helper.readStringRes(R.string.detail_search_fat_no2)
            else -> Helper.readStringRes(R.string.detail_search_joint_no2)
        }
        petfood_recommend_sickness_select_3_text.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_no3)
            1 -> Helper.readStringRes(R.string.detail_search_skin_no3)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_no3)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_no3)
            4 -> Helper.readStringRes(R.string.detail_search_bean_no3)
            5 -> Helper.readStringRes(R.string.detail_search_fat_no3)
            else -> Helper.readStringRes(R.string.detail_search_joint_no3)
        }
        petfood_recommend_sickness_select_4_text.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_no4)
            1 -> Helper.readStringRes(R.string.detail_search_skin_no4)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_no4)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_no4)
            4 -> Helper.readStringRes(R.string.detail_search_bean_no4)
            5 -> Helper.readStringRes(R.string.detail_search_fat_no4)
            else -> Helper.readStringRes(R.string.detail_search_joint_no4)
        }
        petfood_recommend_sickness_select_5_text.text = when(mode){
            0 -> Helper.readStringRes(R.string.detail_search_joint_no5)
            1 -> Helper.readStringRes(R.string.detail_search_skin_no5)
            2 -> Helper.readStringRes(R.string.detail_search_smotach_no5)
            3 -> Helper.readStringRes(R.string.detail_search_dispense_no5)
            4 -> Helper.readStringRes(R.string.detail_search_bean_no5)
            5 -> Helper.readStringRes(R.string.detail_search_fat_no5)
            else -> Helper.readStringRes(R.string.detail_search_joint_no5)
        }


        when(mode){
            5 -> petfood_recomment_sickness_save_button.text = Helper.readStringRes(R.string.detail_detect_done)
            else-> petfood_recomment_sickness_save_button.text = Helper.readStringRes(R.string.pet_add_next)
        }

    }

    private fun checkBox(mode:Int, select:Int){

        foodRecommendDataModel?.sickDetailData?.value?.let{ bag ->

            if(bag[mode].contains(select)) bag[mode].remove(select)
            else bag[mode].add(select)

            foodRecommendDataModel?.sickDetailData?.value = bag
        }

    }


    val textOffColor = Helper.readColorRes(R.color.reddishgrey)
    val textOnColor = Helper.readColorRes(R.color.dark_grey)

    private fun checkBoxChange(){

        val mode = foodRecommendDataModel?.sickDetailMode?.value?:-1
        if(mode>=0){
            foodRecommendDataModel?.sickDetailData?.value?.let{ list ->

                petfood_recommend_sickness_select_1.setImageResource(R.drawable.detail_search_check_off)
                petfood_recommend_sickness_select_2.setImageResource(R.drawable.detail_search_check_off)
                petfood_recommend_sickness_select_3.setImageResource(R.drawable.detail_search_check_off)
                petfood_recommend_sickness_select_4.setImageResource(R.drawable.detail_search_check_off)
                petfood_recommend_sickness_select_5.setImageResource(R.drawable.detail_search_check_off)

                petfood_recommend_sickness_select_1_text.setTextColor(textOffColor)
                petfood_recommend_sickness_select_2_text.setTextColor(textOffColor)
                petfood_recommend_sickness_select_3_text.setTextColor(textOffColor)
                petfood_recommend_sickness_select_4_text.setTextColor(textOffColor)
                petfood_recommend_sickness_select_5_text.setTextColor(textOffColor)

                for(item in list[mode].iterator()){
                    when(item){
                        1 -> {
                            petfood_recommend_sickness_select_1.setImageResource(R.drawable.detail_search_check_on)
                            petfood_recommend_sickness_select_1_text.setTextColor(textOnColor)
                        }
                        2 -> {
                            petfood_recommend_sickness_select_2.setImageResource(R.drawable.detail_search_check_on)
                            petfood_recommend_sickness_select_2_text.setTextColor(textOnColor)
                        }
                        3 -> {
                            petfood_recommend_sickness_select_3.setImageResource(R.drawable.detail_search_check_on)
                            petfood_recommend_sickness_select_3_text.setTextColor(textOnColor)
                        }
                        4 -> {
                            petfood_recommend_sickness_select_4.setImageResource(R.drawable.detail_search_check_on)
                            petfood_recommend_sickness_select_4_text.setTextColor(textOnColor)
                        }
                        5 -> {
                            petfood_recommend_sickness_select_5.setImageResource(R.drawable.detail_search_check_on)
                            petfood_recommend_sickness_select_5_text.setTextColor(textOnColor)
                        }
                        else -> {}
                    }
                }
            }
        }

    }
}