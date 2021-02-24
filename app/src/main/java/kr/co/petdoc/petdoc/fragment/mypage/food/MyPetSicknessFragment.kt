package kr.co.petdoc.petdoc.fragment.mypage.food

import android.graphics.Typeface
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
import kotlinx.android.synthetic.main.fragment_pet_sickness.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.databinding.FragmentPetSicknessBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyPetSicknessFragment : BaseFragment(){

    private val TAG = "MyPetSicknessFragment"
    private val SICKNESS_REQUEST = "$TAG.sickNessRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var sickObserver : Observer<HashSet<String>>? = null

    private var sickSet : HashSet<String>? = null

    private var available : Boolean = false

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)
        sickSet = foodRecommendDataModel?.sickness?.value?.clone() as HashSet<String>

        val databinding = DataBindingUtil.inflate<FragmentPetSicknessBinding>(inflater, R.layout.fragment_pet_sickness, container, false)
            databinding.lifecycleOwner = requireActivity()
            databinding.foodreference = foodRecommendDataModel

        if(foodRecommendDataModel?.matchmode?.value == true){
            databinding.petfoodRecommendSicknessBackButton.visibility = View.GONE
            databinding.mypageTitleText.visibility = View.GONE

            databinding.petfoodRecommentSicknessSaveButton.text = Helper.readStringRes(R.string.pet_add_next)
        }

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommendDataModel?.matchmode?.value!=true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        sickObserver = Observer<HashSet<String>> {
            updatePanels()
        }
        foodRecommendDataModel?.sickness?.observe(viewLifecycleOwner, sickObserver!!)


        petfood_recommend_sickness_back_button.setOnClickListener { requireActivity().onBackPressed() }

        petfood_recomment_sickness_save_button.apply{

        }

        petfood_recomment_sickness_save_button.setOnClickListener {
            foodRecommendDataModel?.sickness?.value = sickSet?.clone() as HashSet<String>

            if(foodRecommendDataModel?.gender?.value.toString() == "woman" && foodRecommendDataModel?.neuter?.value == false){        //여아이고, 중성화가 안되어 있으면 임신 체크 화면 연결
//                findNavController().navigate(MyPetSicknessFragmentDirections.actionMatchfoodToPregnant())
            }else{
//                findNavController().navigate(MyPetSicknessFragmentDirections.actionMatchfoodToResultProgressing())
            }
        }


        petfood_recommend_sickness_tab_healthy.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("healthy") -> it.remove("healthy")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("healthy")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_joint.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("joint") -> it.remove("joint")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("joint")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_skin.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("skin") -> it.remove("skin")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("skin")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_stomach.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("stomach") -> it.remove("stomach")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("stomach")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_dispense.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("dispense") -> it.remove("dispense")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("dispense")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_bean.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("bean") -> it.remove("bean")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("bean")
                }
            }
            updatePanels()
        }

        petfood_recommend_sickness_tab_fat.setOnClickListener {
            sickSet?.let{
                when {
                    it.contains("fat") -> it.remove("fat")
                    it.size>=3 -> AppToast(requireContext()).showToastMessage("Only 3!!!")
                    else -> it.add("fat")
                }
            }
            updatePanels()
        }

        petfood_recommend_detail_search_panel.setOnClickListener {
            foodRecommendDataModel?.sickDetailMode?.value = 0
            foodRecommendDataModel?.sickDetailData?.value = Array<HashSet<Int>>(6){ HashSet<Int>() }
//            findNavController().navigate(MyPetSicknessFragmentDirections.actionMatchfoodToDetailList())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        foodRecommendDataModel?.sickness?.removeObserver(sickObserver!!)
    }

    private fun updatePanels(){

        petfood_recommend_sickness_tab_healthy_image.setImageResource(R.drawable.sickness_healthy_icon)
        petfood_recommend_sickness_tab_joint_image.setImageResource(R.drawable.sickness_joint)
        petfood_recommend_sickness_tab_skin_image.setImageResource(R.drawable.sickness_skin)
        petfood_recommend_sickness_tab_stomach_image.setImageResource(R.drawable.sickness_stomach)
        petfood_recommend_sickness_tab_despense_image.setImageResource(R.drawable.sickness_despense)
        petfood_recommend_sickness_tab_bean_image.setImageResource(R.drawable.sickness_bean)
        petfood_recommend_sickness_tab_fat_image.setImageResource(R.drawable.sickness_fat)

        petfood_recommend_sickness_tab_healthy_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_joint_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_skin_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_stomach_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_dispense_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_bean_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_sickness_tab_fat_text.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }

        available = (sickSet?.size?:0) >= 1
        petfood_recomment_sickness_save_button.text = if(available) Helper.readStringRes(R.string.food_search_done) else Helper.readStringRes(R.string.pet_add_next)

        sickSet?.let{
            for(item in it){
                when(item){
                    "healthy" -> {
                        petfood_recommend_sickness_tab_healthy_image.setImageResource(R.drawable.sickness_healthy_icon_on)
                        petfood_recommend_sickness_tab_healthy_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "joint" -> {
                        petfood_recommend_sickness_tab_joint_image.setImageResource(R.drawable.sickness_joint_on)
                        petfood_recommend_sickness_tab_joint_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "skin" -> {
                        petfood_recommend_sickness_tab_skin_image.setImageResource(R.drawable.sickness_skin_on)
                        petfood_recommend_sickness_tab_skin_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "stomach" -> {
                        petfood_recommend_sickness_tab_stomach_image.setImageResource(R.drawable.sickness_stomach_on)
                        petfood_recommend_sickness_tab_stomach_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "dispense" -> {
                        petfood_recommend_sickness_tab_despense_image.setImageResource(R.drawable.sickness_despense_on)
                        petfood_recommend_sickness_tab_dispense_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "bean" -> {
                        petfood_recommend_sickness_tab_bean_image.setImageResource(R.drawable.sickness_bean_on)
                        petfood_recommend_sickness_tab_bean_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                    "fat" -> {
                        petfood_recommend_sickness_tab_fat_image.setImageResource(R.drawable.sickness_fat_on)
                        petfood_recommend_sickness_tab_fat_text.apply{
                            setTextColor(Helper.readColorRes(R.color.dark_grey))
                            setTypeface(null, Typeface.BOLD)
                        }
                    }
                }
            }
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            SICKNESS_REQUEST -> {

            }
        }
    }
}