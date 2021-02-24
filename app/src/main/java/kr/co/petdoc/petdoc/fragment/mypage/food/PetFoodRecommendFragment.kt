package kr.co.petdoc.petdoc.fragment.mypage.food

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_petfood_recommend_home.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.databinding.FragmentPetfoodRecommendHomeBinding
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel

/**
 * petdoc-android
 * Class: MyPetWeightFragment
 * Created by sungminkim on 2020/04/08.
 *
 * Description : 마이페이지 내부에서 맞춤식 진단 정보 입력 메인화면
 */
class PetFoodRecommendFragment : Fragment(){

    private var foodRecommendDataModel : FoodRecommentDataModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentPetfoodRecommendHomeBinding>(inflater, R.layout.fragment_petfood_recommend_home, container, false)
        databinding.lifecycleOwner = requireActivity()
        databinding.foodreference = foodRecommendDataModel

        return databinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("mypage", "click", "customeal_info_change", null, null, null)

        readyUIandEvent()
    }

    private fun readyUIandEvent(){

        food_reference_pregnant_panel.apply {
            when {
                foodRecommendDataModel?.matchFoodAllStep?.value == 8 -> {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetPregnantFragment3)
                    }
                }

                else -> visibility = View.GONE
            }
        }

        change_information_profile_image.apply {
            when {
                foodRecommendDataModel?.petimage?.value!!.toString().isNotEmpty() -> {
                    Glide.with(requireActivity())
                        .load(if(foodRecommendDataModel?.petimage?.value!!.toString().startsWith("http")) foodRecommendDataModel?.petimage?.value!!.toString() else "${AppConstants.IMAGE_URL}${foodRecommendDataModel?.petimage?.value!!}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(change_information_profile_image)
                }

                else -> {
                    Glide.with(requireActivity())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(change_information_profile_image)
                }
            }
        }

        petfood_recommend_back_button.setOnClickListener { requireActivity().onBackPressed() }

        food_reference_weight_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetWeightFragment3)

        }

        food_reference_body_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetBodyTypeFragment3)

        }

        food_reference_activity_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetWalkingHourFragment3)

        }

        food_reference_allege_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetAllergeFoodFragment3)

        }

        food_reference_likefood_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetLikeFoodFragment3)
        }

        food_reference_current_feed_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetCurrentFeedFragment3)
        }

        food_reference_detail_diagnosis_panel.setOnClickListener {
            findNavController().navigate(R.id.action_petFoodRecommendFragment3_to_myPetDetailDiagnosisFragment3)
        }

        food_again_customeal.setOnClickListener {
            startActivity(Intent(requireActivity(), MatchFoodActivity::class.java).apply {
                putExtra("item", foodRecommendDataModel?.petInfo?.value)
                putExtra("matchFoodAgain", true)
            })
        }

    }

}