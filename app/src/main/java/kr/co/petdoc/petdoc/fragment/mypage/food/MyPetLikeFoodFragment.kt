package kr.co.petdoc.petdoc.fragment.mypage.food

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pet_like_food.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetLikeFoodBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.MatchFoodData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MyPetLikeFoodFragment : BaseFragment() {

    private val TAG = "MyPetLikeFoodFragment"
    private val MAIN_FOOD_REQUEST = "$TAG.mainFoodRequest"
    private val MAIN_FOOD_UPLOAD_REQUEST = "$TAG.mainFoodUploadRequest"

    private var foodRecommentDataModel : FoodRecommentDataModel? = null
    private var foodLikeObserver : Observer<HashSet<String>>? = null

    private var backup : HashSet<String>? = null
    private var foodId : HashSet<Int> = hashSetOf()
    private var foodItems:MutableList<MatchFoodData> = mutableListOf()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommentDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val dataBinding = DataBindingUtil.inflate<FragmentPetLikeFoodBinding>(inflater, R.layout.fragment_pet_like_food, container, false)
        dataBinding.lifecycleOwner = requireActivity()
        dataBinding.foodreference = foodRecommentDataModel

        if(foodRecommentDataModel?.matchmode?.value == true){
            dataBinding.petfoodRecommendLikefoodBackButton.visibility = View.GONE
            dataBinding.mypageTitleText.visibility = View.GONE

            dataBinding.petfoodRecommentFoodLikeButton.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_선호재료", "Page View", "선호주재료 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_선호재료_수정", "Page View", "선호주재료 입력 단계 수정 페이지뷰")
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommentDataModel?.matchmode?.value != true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        petfood_recommend_likefood_back_button.setOnClickListener {
            requireActivity().onBackPressed()
        }
        petfood_recomment_food_like_button.setOnClickListener {
            mApiClient.uploadLikeFood(
                MAIN_FOOD_UPLOAD_REQUEST,
                foodRecommentDataModel?.petId?.value!!,
                foodRecommentDataModel?.cmInfoId?.value!!,
                foodId
            )
        }

        //===============================================================================================
        petfood_recommend_tab_1.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[0].type)) this.remove(foodItems[0].type)
                else{
                    this.add(foodItems[0].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[0].id)) this.remove(foodItems[0].id)
                else{
                    this.add(foodItems[0].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_2.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[1].type)) this.remove(foodItems[1].type)
                else{
                    this.add(foodItems[1].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[1].id)) this.remove(foodItems[1].id)
                else{
                    this.add(foodItems[1].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_3.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[2].type)) this.remove(foodItems[2].type)
                else{
                    this.add(foodItems[2].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[2].id)) this.remove(foodItems[2].id)
                else{
                    this.add(foodItems[2].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_4.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[3].type)) this.remove(foodItems[3].type)
                else{
                    this.add(foodItems[3].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[3].id)) this.remove(foodItems[3].id)
                else{
                    this.add(foodItems[3].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_5.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[4].type)) this.remove(foodItems[4].type)
                else{
                    this.add(foodItems[4].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[4].id)) this.remove(foodItems[4].id)
                else{
                    this.add(foodItems[4].id)
                }
            }

            updateFoodPanels()
        }

/*        petfood_recommend_tab_6.setOnClickListener {
            backup?.apply{
                if(this.contains(foodItems[5].type)) this.remove(foodItems[5].type)
                else{
                    this.add(foodItems[5].type)
                }
            }

            foodId.apply {
                if(this.contains(foodItems[5].id)) this.remove(foodItems[5].id)
                else{
                    this.add(foodItems[5].id)
                }
            }

            updateFoodPanels()
        }*/

        //============================================================================================

        mApiClient.getMatchFoodList(MAIN_FOOD_REQUEST)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if(foodRecommentDataModel != null) {
            foodRecommentDataModel?.likefood?.removeObserver(foodLikeObserver!!)
        }

        if (mApiClient.isRequestRunning(MAIN_FOOD_REQUEST)) {
            mApiClient.cancelRequest(MAIN_FOOD_REQUEST)
        }
    }

    private fun setFoodPanels() {
        for (i in 0 until foodItems.size) {
            when (i) {
                0 -> {
                    val id = resources.getIdentifier(foodItems[0].type, "drawable", context?.packageName)
                    petfood_recommend_tab_1_image.setImageResource(id)
                    petfood_recommend_tab_1_text.text = foodItems[0].name
                    petfood_recommend_tab_1_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                1-> {
                    val id = resources.getIdentifier(foodItems[1].type, "drawable", context?.packageName)
                    petfood_recommend_tab_2_image.setImageResource(id)
                    petfood_recommend_tab_2_text.text = foodItems[1].name
                    petfood_recommend_tab_2_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                2-> {
                    val id = resources.getIdentifier(foodItems[2].type, "drawable", context?.packageName)
                    petfood_recommend_tab_3_image.setImageResource(id)
                    petfood_recommend_tab_3_text.text = foodItems[2].name
                    petfood_recommend_tab_3_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                3-> {
                    val id = resources.getIdentifier(foodItems[3].type, "drawable", context?.packageName)
                    petfood_recommend_tab_4_image.setImageResource(id)
                    petfood_recommend_tab_4_text.text = foodItems[3].name
                    petfood_recommend_tab_4_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                4-> {
                    val id = resources.getIdentifier(foodItems[4].type, "drawable", context?.packageName)
                    petfood_recommend_tab_5_image.setImageResource(id)
                    petfood_recommend_tab_5_text.text = foodItems[4].name
                    petfood_recommend_tab_5_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

/*                5-> {
                    val id = resources.getIdentifier(foodItems[5].type, "drawable", context?.packageName)
                    petfood_recommend_tab_6_image.setImageResource(id)
                    petfood_recommend_tab_6_text.text = foodItems[5].name
                    petfood_recommend_tab_6_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }*/
            }
        }
    }

    private fun updateFoodPanels(){
        setFoodPanels()

        backup?.apply{
            for(item in this){
                when(item){
                    "chicken_meet" -> {
                        setFoodPanelUI("chicken_meet")
                    }
                    "salmon_tuna" -> {
                        setFoodPanelUI("salmon_tuna")
                    }
                    "beef" -> {
                        setFoodPanelUI("beef")
                    }
                    "lamb" -> {
                        setFoodPanelUI("lamb")
                    }
                    "duck_meet" -> {
                        setFoodPanelUI("duck_meet")
                    }

                    "like_all" -> {
                        setFoodPanelUI("like_all")
                    }
                }
            }
        }
    }

    private fun setFoodPanelUI(type:String) {
        val item = foodItems.find { it.type == type }
        val id = resources.getIdentifier("${type}_on", "drawable", context?.packageName)
        when (item?.displaySeq) {
            1 -> {
                petfood_recommend_tab_1_image.setImageResource(id)
                petfood_recommend_tab_1_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }

            2 -> {
                petfood_recommend_tab_2_image.setImageResource(id)
                petfood_recommend_tab_2_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }

            3 -> {
                petfood_recommend_tab_3_image.setImageResource(id)
                petfood_recommend_tab_3_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }

            4-> {
                petfood_recommend_tab_4_image.setImageResource(id)
                petfood_recommend_tab_4_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }

            5 -> {
                petfood_recommend_tab_5_image.setImageResource(id)
                petfood_recommend_tab_5_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }

/*            6 -> {
                petfood_recommend_tab_6_image.setImageResource(id)
                petfood_recommend_tab_6_text.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setTypeface(null, Typeface.BOLD)
                    text = item.name
                }
            }*/
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            MAIN_FOOD_REQUEST -> {
                if ("ok" == event.status) {
                    foodItems = Gson().fromJson(event.response,
                        object : TypeToken<MutableList<MatchFoodData>>() {}.type)

                    foodItems.sortedBy { it.displaySeq }
                    setFoodPanels()

                    foodRecommentDataModel?.likefood?.value?.let{
                        backup = it.clone() as HashSet<String>
                    }

                    foodLikeObserver = Observer<HashSet<String>>{
                        updateFoodPanels()
                    }

                    foodRecommentDataModel?.likefood?.observe(viewLifecycleOwner, foodLikeObserver!!)
                }
            }

            MAIN_FOOD_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommentDataModel?.likefood?.removeObserver(foodLikeObserver!!)
                        foodRecommentDataModel?.likefood?.value = backup

                        if(foodRecommentDataModel?.matchmode?.value == true) findNavController().navigate(MyPetLikeFoodFragmentDirections.actionMatchfoodLikefoodToAllergefood())
                        else requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}