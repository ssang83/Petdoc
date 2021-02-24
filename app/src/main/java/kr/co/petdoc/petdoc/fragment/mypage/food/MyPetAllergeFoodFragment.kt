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
import kotlinx.android.synthetic.main.fragment_pet_allerge_food.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetAllergeFoodBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.MatchFoodData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


/**
 * petdoc-android
 * Class: MyPetAllergeFoodFragment
 * Created by sungminkim on 2020/04/13.
 *
 * Description : 맞춤식 진단 중, 음식 알러지 체크,   중복 체크 가능
 */
class MyPetAllergeFoodFragment : BaseFragment() {

    private val TAG = "MyPetAllergeFoodFragment"
    private val ALLERGE_REQUEST = "$TAG.allergeRequest"
    private val ALLERGE_UPLOAD_REQUEST = "$TAG.allergeUploadRequest"

    private var foodRecommentDataModel : FoodRecommentDataModel? = null
    private var foodAllergeObserver : Observer<HashSet<String>>? = null

    private var backup : HashSet<String> = hashSetOf()
    private var foodId:HashSet<Int> = hashSetOf()

    private var allergeFoodItems:MutableList<MatchFoodData> = mutableListOf()

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommentDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val dataBinding = DataBindingUtil.inflate<FragmentPetAllergeFoodBinding>(inflater, R.layout.fragment_pet_allerge_food, container, false)
            dataBinding.lifecycleOwner = requireActivity()
            dataBinding.foodreference = foodRecommentDataModel

        if(foodRecommentDataModel?.matchmode?.value == true){
            dataBinding.petfoodRecommendWalkingBackButton.visibility = View.GONE
            dataBinding.mypageTitleText.visibility = View.GONE
            dataBinding.petfoodRecommendAllergeDesc.visibility = View.VISIBLE

            dataBinding.petfoodRecommentWalkingSaveButton.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_알러지재료", "Page View", "알러지주재료 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_알러지재료_수정", "Page View", "알러지주재료 입력 단계 수정 페이지뷰")
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommentDataModel?.matchmode?.value != true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        petfood_recommend_walking_back_button.setOnClickListener {
            requireActivity().onBackPressed()
        }
        petfood_recomment_walking_save_button.setOnClickListener {
            mApiClient.uploadAllergeFood(
                ALLERGE_UPLOAD_REQUEST,
                foodRecommentDataModel?.petId?.value!!,
                foodRecommentDataModel?.cmInfoId?.value!!,
                foodId
            )
        }

        //===========================================================================================
        petfood_recommend_tab_1.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[0].type)) this.remove(allergeFoodItems[0].type)
                else{
                    this.add(allergeFoodItems[0].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[0].id)) this.remove(allergeFoodItems[0].id)
                else{
                    this.add(allergeFoodItems[0].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_2.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[1].type)) this.remove(allergeFoodItems[1].type)
                else{
                    this.add(allergeFoodItems[1].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[1].id)) this.remove(allergeFoodItems[1].id)
                else{
                    this.add(allergeFoodItems[1].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_3.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[2].type)) this.remove(allergeFoodItems[2].type)
                else{
                    this.add(allergeFoodItems[2].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[2].id)) this.remove(allergeFoodItems[2].id)
                else{
                    this.add(allergeFoodItems[2].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_4.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[3].type)) this.remove(allergeFoodItems[3].type)
                else{
                    this.add(allergeFoodItems[3].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[3].id)) this.remove(allergeFoodItems[3].id)
                else{
                    this.add(allergeFoodItems[3].id)
                }
            }

            updateFoodPanels()
        }

        petfood_recommend_tab_5.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[4].type)) this.remove(allergeFoodItems[4].type)
                else{
                    this.add(allergeFoodItems[4].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[4].id)) this.remove(allergeFoodItems[4].id)
                else{
                    this.add(allergeFoodItems[4].id)
                }
            }

            updateFoodPanels()
        }

/*
        petfood_recommend_tab_6.setOnClickListener {
            backup?.apply{
                if(this.contains(allergeFoodItems[5].type)) this.remove(allergeFoodItems[5].type)
                else{
                    this.add(allergeFoodItems[5].type)
                }
            }

            foodId.apply {
                if(this.contains(allergeFoodItems[5].id)) this.remove(allergeFoodItems[5].id)
                else{
                    this.add(allergeFoodItems[5].id)
                }
            }

            updateFoodPanels()
        }
*/

        //===========================================================================================
        mApiClient.getMatchFoodList(ALLERGE_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        foodRecommentDataModel?.allerge?.removeObserver(foodAllergeObserver!!)

        if (mApiClient.isRequestRunning(ALLERGE_REQUEST)) {
            mApiClient.cancelRequest(ALLERGE_REQUEST)
        }
    }

    private fun setFoodPanels() {
        for (i in 0 until allergeFoodItems.size) {
            when (i) {
                0 -> {
                    val id = resources.getIdentifier(allergeFoodItems[0].type, "drawable", context?.packageName)
                    petfood_recommend_tab_1_image.setImageResource(id)
                    petfood_recommend_tab_1_text.text = allergeFoodItems[0].name
                    petfood_recommend_tab_1_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                1-> {
                    val id = resources.getIdentifier(allergeFoodItems[1].type, "drawable", context?.packageName)
                    petfood_recommend_tab_2_image.setImageResource(id)
                    petfood_recommend_tab_2_text.text = allergeFoodItems[1].name
                    petfood_recommend_tab_2_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                2-> {
                    val id = resources.getIdentifier(allergeFoodItems[2].type, "drawable", context?.packageName)
                    petfood_recommend_tab_3_image.setImageResource(id)
                    petfood_recommend_tab_3_text.text = allergeFoodItems[2].name
                    petfood_recommend_tab_3_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                3-> {
                    val id = resources.getIdentifier(allergeFoodItems[3].type, "drawable", context?.packageName)
                    petfood_recommend_tab_4_image.setImageResource(id)
                    petfood_recommend_tab_4_text.text = allergeFoodItems[3].name
                    petfood_recommend_tab_4_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

                4-> {
                    val id = resources.getIdentifier(allergeFoodItems[4].type, "drawable", context?.packageName)
                    petfood_recommend_tab_5_image.setImageResource(id)
                    petfood_recommend_tab_5_text.text = allergeFoodItems[4].name
                    petfood_recommend_tab_5_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }

/*                5-> {
                    val id = resources.getIdentifier(allergeFoodItems[5].type, "drawable", context?.packageName)
                    petfood_recommend_tab_6_image.setImageResource(id)
                    petfood_recommend_tab_6_text.text = allergeFoodItems[5].name
                    petfood_recommend_tab_6_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                }*/
            }
        }
    }

    private fun updateFoodPanels(){
        setFoodPanels()

        Logger.d("allerge select count : ${backup?.size}, $backup")
        petfood_recommend_allerge_desc.apply {
            when {
                backup?.size  == 5 -> {
                    visibility = View.VISIBLE
                    petfood_recomment_walking_save_button.isEnabled = false
                    petfood_recomment_walking_save_button.setTextColor(Helper.readColorRes(R.color.white_alpha))
                }
                else -> {
                    visibility = View.GONE
                    petfood_recomment_walking_save_button.isEnabled = true
                    petfood_recomment_walking_save_button.setTextColor(Helper.readColorRes(R.color.white))
                }
            }
        }

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
        val item = allergeFoodItems.find { it.type == type }
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

            /*6 -> {
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
            ALLERGE_REQUEST -> {
                if ("ok" == event.status) {
                    allergeFoodItems = Gson().fromJson(event.response,
                        object : TypeToken<MutableList<MatchFoodData>>() {}.type)

                    allergeFoodItems.sortedBy { it.displaySeq }
                    setFoodPanels()

                    foodRecommentDataModel?.allerge?.value?.let{
                        backup = it.clone() as HashSet<String>
                    }

                    foodAllergeObserver = Observer<HashSet<String>>{
                        updateFoodPanels()
                    }

                    foodRecommentDataModel?.allerge?.observe(viewLifecycleOwner, foodAllergeObserver!!)
                }
            }

            ALLERGE_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommentDataModel?.allerge?.removeObserver(foodAllergeObserver!!)
                        foodRecommentDataModel?.allerge?.value = backup

                        if(foodRecommentDataModel?.matchmode?.value == true) findNavController().navigate(MyPetAllergeFoodFragmentDirections.actionMyPetAllergeFoodFragment2ToMyPetDetailDiagnosisFragment())
                        else requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}