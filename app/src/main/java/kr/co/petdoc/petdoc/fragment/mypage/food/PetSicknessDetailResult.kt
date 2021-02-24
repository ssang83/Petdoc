package kr.co.petdoc.petdoc.fragment.mypage.food

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sick_search_result.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.databinding.FragmentSickSearchResultBinding
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel

class PetSicknessDetailResult : Fragment() {

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var dataSet = HashSet<String>()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?  ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val dataBinding = DataBindingUtil.inflate<FragmentSickSearchResultBinding>(inflater, R.layout.fragment_sick_search_result, container, false)
            dataBinding.lifecycleOwner = requireActivity()
            dataBinding.foodreference = foodRecommendDataModel

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val unitSize = Helper.screenSize(requireActivity())[0]/3

        petfood_recommend_sickness_tab_joint.apply{ layoutParams = layoutParams.apply{ width = unitSize }  }
        petfood_recommend_sickness_tab_skin.apply{ layoutParams = layoutParams.apply{ width = unitSize }   }
        petfood_recommend_sickness_tab_stomach.apply{ layoutParams = layoutParams.apply{ width = unitSize }   }
        petfood_recommend_sickness_tab_dispense.apply{ layoutParams = layoutParams.apply{ width = unitSize }   }
        petfood_recommend_sickness_tab_bean.apply{ layoutParams = layoutParams.apply{ width = unitSize }    }
        petfood_recommend_sickness_tab_fat.apply{  layoutParams = layoutParams.apply{ width = unitSize }    }

        foodRecommendDataModel?.sickDetailData?.value?.let{
            for((index,item) in it.withIndex()){
                when(index){
                    0 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_joint)
                    1 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_skin)
                    2 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_stomach)
                    3 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_dispense)
                    4 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_bean)
                    5 -> if(item.size<3) petfood_recommend_sickness_tabs_area.removeView(petfood_recommend_sickness_tab_fat)
                }
            }
        }
        petfood_recommend_sickness_tabs_area.invalidate()
        //------------------------------------------------------------------------------------------


        petfood_recommend_sickness_tab_joint?.setOnClickListener {
            if(dataSet.contains("joint")) dataSet.remove("joint")
            else dataSet.add("joint")
            updatePanels()
        }
        petfood_recommend_sickness_tab_skin?.setOnClickListener {
            if(dataSet.contains("skin")) dataSet.remove("skin")
            else dataSet.add("skin")
            updatePanels()
        }
        petfood_recommend_sickness_tab_stomach?.setOnClickListener {
            if(dataSet.contains("stomach")) dataSet.remove("stomach")
            else dataSet.add("stomach")
            updatePanels()
        }
        petfood_recommend_sickness_tab_dispense?.setOnClickListener {
            if(dataSet.contains("dispense")) dataSet.remove("dispense")
            else dataSet.add("dispense")
            updatePanels()
        }
        petfood_recommend_sickness_tab_bean?.setOnClickListener {
            if(dataSet.contains("bean")) dataSet.remove("bean")
            else dataSet.add("bean")
            updatePanels()
        }
        petfood_recommend_sickness_tab_fat?.setOnClickListener {
            if(dataSet.contains("fat")) dataSet.remove("fat")
            else dataSet.add("fat")
            updatePanels()
        }


        updatePanels()

        petfood_recomment_sickness_save_button.setOnClickListener {
            if(available){
                if(foodRecommendDataModel?.gender?.value.toString() == "woman" && foodRecommendDataModel?.neuter?.value == false){        //여아이고, 중성화가 안되어 있으면 임신 체크 화면 연결
//                    findNavController().navigate(PetSicknessDetailResultDirections.actionSickresultToPregnant())
                }else{
//                    findNavController().navigate(PetSicknessDetailResultDirections.actionMatchfoodToResultProgressing())
                }
            }
        }

    }


    val buttonOffColor = Helper.readColorRes(R.color.reddishgrey)
    val buttonOnColor = Helper.readColorRes(R.color.dark_grey)

    var available = false

    private fun updatePanels(){

        petfood_recomment_sickness_save_button.alpha = if(dataSet.size == 2) 1.0f else 0.4f
        available = dataSet.size == 2

        petfood_recommend_sickness_tab_joint_image?.setImageResource(R.drawable.sickness_joint)
        petfood_recommend_sickness_tab_skin_image?.setImageResource(R.drawable.sickness_skin)
        petfood_recommend_sickness_tab_stomach_image?.setImageResource(R.drawable.sickness_stomach)
        petfood_recommend_sickness_tab_despense_image?.setImageResource(R.drawable.sickness_despense)
        petfood_recommend_sickness_tab_bean_image?.setImageResource(R.drawable.sickness_bean)
        petfood_recommend_sickness_tab_fat_image?.setImageResource(R.drawable.sickness_fat)

        petfood_recommend_sickness_tab_joint_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_skin_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_dispense_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_fat_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_bean_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_stomach_text?.apply{
            setTextColor(buttonOffColor)
            setTypeface(null, Typeface.NORMAL)
        }

        for(item in dataSet){
            when(item){
                "joint" -> {
                    petfood_recommend_sickness_tab_joint_image?.setImageResource(R.drawable.sickness_joint_on)
                    petfood_recommend_sickness_tab_joint_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                "dispense" -> {
                    petfood_recommend_sickness_tab_despense_image?.setImageResource(R.drawable.sickness_despense_on)
                    petfood_recommend_sickness_tab_dispense_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                "skin" -> {
                    petfood_recommend_sickness_tab_skin_image?.setImageResource(R.drawable.sickness_skin_on)
                    petfood_recommend_sickness_tab_skin_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                "bean" -> {
                    petfood_recommend_sickness_tab_bean_image?.setImageResource(R.drawable.sickness_bean_on)
                    petfood_recommend_sickness_tab_bean_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                "fat" -> {
                    petfood_recommend_sickness_tab_fat_image?.setImageResource(R.drawable.sickness_fat_on)
                    petfood_recommend_sickness_tab_fat_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                "stomach" -> {
                    petfood_recommend_sickness_tab_stomach_image?.setImageResource(R.drawable.sickness_stomach_on)
                    petfood_recommend_sickness_tab_stomach_text?.apply{
                        setTextColor(buttonOnColor)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
            }
        }

    }


}