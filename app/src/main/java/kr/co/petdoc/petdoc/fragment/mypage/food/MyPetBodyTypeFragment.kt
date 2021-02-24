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
import kotlinx.android.synthetic.main.fragment_pet_body.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetBodyBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * petdoc-android
 * Class: MyPetBodyTypeFragment
 * Created by sungminkim on 2020/04/13.
 *
 * Description : 마이페이지 내부에서 맞춤식 진단 정보 입력 메인화면
 */
class MyPetBodyTypeFragment : BaseFragment(){

    private val TAG = "MyPetBodyTypeFragment"
    private val BODY_TYPE_REQUEST = "$TAG.bodyTypeRequest"

    private var foodRecommentDirections : FoodRecommentDataModel? = null
    private var bodyTypeObserver : Observer<FoodRecommentDataModel.PetBodyLevel>? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommentDirections = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val dataBinding = DataBindingUtil.inflate<FragmentPetBodyBinding>(inflater, R.layout.fragment_pet_body, container, false)
        dataBinding.lifecycleOwner = requireActivity()
        dataBinding.foodreference = foodRecommentDirections

        if(foodRecommentDirections?.matchmode?.value == true){
            dataBinding.petfoodRecommendWeightBackButton.visibility = View.GONE
            dataBinding.mypageTitleText.visibility = View.GONE

            FirebaseAPI(requireActivity()).logEventFirebase("진단_체형", "Page View", "체형 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_체형_수정", "Page View", "체형 입력 단계 수정 페이지뷰")
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)


        bodyTypeObserver = Observer { bodyType ->
            updateBodyType()
        }
        foodRecommentDirections?.body?.observe(viewLifecycleOwner, bodyTypeObserver!!)

        petfood_recommend_weight_back_button.setOnClickListener { requireActivity().onBackPressed() }


        petfood_recommend_body_type_1.setOnClickListener { foodRecommentDirections?.body?.value = FoodRecommentDataModel.PetBodyLevel.LEVEL1 }
        petfood_recommend_body_type_2.setOnClickListener { foodRecommentDirections?.body?.value = FoodRecommentDataModel.PetBodyLevel.LEVEL2 }
        petfood_recommend_body_type_3.setOnClickListener { foodRecommentDirections?.body?.value = FoodRecommentDataModel.PetBodyLevel.LEVEL3 }
        petfood_recommend_body_type_4.setOnClickListener { foodRecommentDirections?.body?.value = FoodRecommentDataModel.PetBodyLevel.LEVEL4 }
        petfood_recommend_body_type_5.setOnClickListener { foodRecommentDirections?.body?.value = FoodRecommentDataModel.PetBodyLevel.LEVEL5 }

        petfood_recomment_weight_save_button.apply {
            if(foodRecommentDirections?.matchmode?.value == true) text = Helper.readStringRes(R.string.pet_add_next)
            setOnClickListener {
                val bodyType = when (foodRecommentDirections?.body?.value!!) {
                    FoodRecommentDataModel.PetBodyLevel.LEVEL1 -> 10
                    FoodRecommentDataModel.PetBodyLevel.LEVEL2 -> 20
                    FoodRecommentDataModel.PetBodyLevel.LEVEL3 -> 30
                    FoodRecommentDataModel.PetBodyLevel.LEVEL4 -> 40
                    FoodRecommentDataModel.PetBodyLevel.LEVEL5 -> 50
                    else -> 0
                }

                mApiClient.uploadBodyType(
                    BODY_TYPE_REQUEST,
                    foodRecommentDirections?.petId?.value!!,
                    foodRecommentDirections?.cmInfoId?.value!!,
                    bodyType
                )
            }
        }

        petfood_recomment_weight_save_button.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        foodRecommentDirections?.body?.removeObserver(bodyTypeObserver!!)

        if (mApiClient.isRequestRunning(BODY_TYPE_REQUEST)) {
            mApiClient.cancelRequest(BODY_TYPE_REQUEST)
        }
    }


    private fun updateBodyType(){

        petfood_recommend_body_type_1.setBackgroundResource(R.drawable.pinkishgrey_round_rect)
        petfood_recommend_body_type_2.setBackgroundResource(R.drawable.pinkishgrey_round_rect)
        petfood_recommend_body_type_3.setBackgroundResource(R.drawable.pinkishgrey_round_rect)
        petfood_recommend_body_type_4.setBackgroundResource(R.drawable.pinkishgrey_round_rect)
        petfood_recommend_body_type_5.setBackgroundResource(R.drawable.pinkishgrey_round_rect)

        petfood_recommend_body_image_1.setImageResource(R.drawable.body_level_1)
        petfood_recommend_body_image_2.setImageResource(R.drawable.body_level_2)
        petfood_recommend_body_image_3.setImageResource(R.drawable.body_level_3)
        petfood_recommend_body_image_4.setImageResource(R.drawable.body_level_4)
        petfood_recommend_body_image_5.setImageResource(R.drawable.body_level_5)

        petfood_recommend_body_title_1.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_body_title_2.apply{
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_body_title_3.apply {
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_body_title_4.apply {
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }
        petfood_recommend_body_title_5.apply {
            setTextColor(Helper.readColorRes(R.color.reddishgrey))
            setTypeface(null, Typeface.NORMAL)
        }

        when(foodRecommentDirections?.body?.value){
            FoodRecommentDataModel.PetBodyLevel.LEVEL1 -> {
                petfood_recommend_body_image_1.setImageResource(R.drawable.body_level_1_on)
                petfood_recommend_body_title_1.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                }
                petfood_recommend_body_type_1.setBackgroundResource(R.drawable.lightmauve_round_rect)

                petfood_recomment_weight_save_button.isEnabled = true
                petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
            }
            FoodRecommentDataModel.PetBodyLevel.LEVEL2 -> {
                petfood_recommend_body_image_2.setImageResource(R.drawable.body_level_2_on)
                petfood_recommend_body_title_2.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                }
                petfood_recommend_body_type_2.setBackgroundResource(R.drawable.lightmauve_round_rect)

                petfood_recomment_weight_save_button.isEnabled = true
                petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
            }
            FoodRecommentDataModel.PetBodyLevel.LEVEL3 -> {
                petfood_recommend_body_image_3.setImageResource(R.drawable.body_level_3_on)
                petfood_recommend_body_title_3.apply {
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                }
                petfood_recommend_body_type_3.setBackgroundResource(R.drawable.lightmauve_round_rect)

                petfood_recomment_weight_save_button.isEnabled = true
                petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
            }
            FoodRecommentDataModel.PetBodyLevel.LEVEL4 -> {
                petfood_recommend_body_image_4.setImageResource(R.drawable.body_level_4_on)
                petfood_recommend_body_title_4.apply {
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                }
                petfood_recommend_body_type_4.setBackgroundResource(R.drawable.lightmauve_round_rect)

                petfood_recomment_weight_save_button.isEnabled = true
                petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
            }
            FoodRecommentDataModel.PetBodyLevel.LEVEL5 -> {
                petfood_recommend_body_image_5.setImageResource(R.drawable.body_level_5_on)
                petfood_recommend_body_title_5.apply {
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                }
                petfood_recommend_body_type_5.setBackgroundResource(R.drawable.lightmauve_round_rect)

                petfood_recomment_weight_save_button.isEnabled = true
                petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
            }
            else -> {}
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
        when (event.tag) {
            BODY_TYPE_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        if(foodRecommentDirections?.matchmode?.value == true) findNavController().navigate(MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2())
                        else requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}