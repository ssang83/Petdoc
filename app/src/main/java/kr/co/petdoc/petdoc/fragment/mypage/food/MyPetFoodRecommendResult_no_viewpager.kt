package kr.co.petdoc.petdoc.fragment.mypage.food

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_feed_choice_item.view.*
import kotlinx.android.synthetic.main.adapter_nutrition_item.view.*
import kotlinx.android.synthetic.main.fragment_main_food_recommend.*
import kotlinx.android.synthetic.main.fragment_nutrition_recommend.*
import kotlinx.android.synthetic.main.fragment_pet_food_recommend.*
import kotlinx.android.synthetic.main.layout_food_recommend_result.*
import kotlinx.android.synthetic.main.view_ingredient_item.view.*
import kotlinx.android.synthetic.main.view_main_food_ingredient_item.view.ingredientDesc
import kotlinx.android.synthetic.main.view_main_food_ingredient_item.view.point
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.IngredientDetailActivity
import kr.co.petdoc.petdoc.activity.MatchFoodModifyActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetFoodRecommendBinding
import kr.co.petdoc.petdoc.enum.FoodStatus
import kr.co.petdoc.petdoc.enum.NutritionStatus
import kr.co.petdoc.petdoc.enum.NutritionType
import kr.co.petdoc.petdoc.enum.SpeciesSize
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.customeal.MainFoodFragment
import kr.co.petdoc.petdoc.fragment.customeal.NutritionFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.*
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

class MyPetFoodRecommendResult_no_viewpager : BaseFragment() {

    private val TAG = "MyPetFoodRecommendResult"
    private val RECOMMEND_FOOD_RESULT_REQUEST = "$TAG.recommendFoodResultRequest"
    private val NUTRITION_1_INFO_REQUEST = "$TAG.nutrition1InfoRequest"
    private val NUTRITION_2_INFO_REQUEST = "$TAG.nutrition2InfoRequest"
    private val VLAB_REQUEST = "$TAG.vlabUrlRequest"
    private val SPECIES_INFO_REQUEST = "$TAG.speciesInfoRequest"
    private val FEED_INFO_REQUEST = "$TAG.feedInfoRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var tabMode = FoodTabMode.MAIN_FEED

    private var petInfoItem:PetInfoItem? = null
    private var nutritionData1:NutritionData? = null
    private var nutritionData2:NutritionData? = null
    private var resultData:DiagnosisResultData? = null
    private var twoWeeksData:VLabData? = null
    private var fourWeeksData:VLabData? = null
    private var sampleData:VLabData? = null
    private var feedData: FeedInfoData? = null
    private var speciesData: SpeciesInfoData? = null

    private lateinit var feedChoiceAdapter: FeedChoiceAdaoter
    private var feedChoiceItems:MutableList<FeedChoiceItem1> = mutableListOf()

    private var nutritionItems:MutableList<NutritionData> = mutableListOf()
    private lateinit var pageAdapter: NutritionAdapter

//    private lateinit var pagerAdapter: PagerAdapter

    private var selectedPosition = -1
    private var feedChoiceType = ""

    private var ageStatus = ""
    private var nutritionType1 = ""
    private var nutritionType2 = ""

    private var callType = ""

    private var petName = ""
    private var petImage = ""

    enum class FoodTabMode{
        MAIN_FEED, SUPPLIER
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databiding = DataBindingUtil.inflate<FragmentPetFoodRecommendBinding>(inflater, R.layout.fragment_pet_food_recommend, container, false)
            databiding.lifecycleOwner = requireActivity()
            databiding.foodreference = foodRecommendDataModel

        if(foodRecommendDataModel?.matchmode?.value == true){
            databiding.petfoodRecommendBackButton.visibility = View.GONE
            databiding.mypageTitleText.visibility = View.GONE

            databiding.foodRecommendResultProfileFix.visibility = View.GONE
        }

        return databiding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommendDataModel?.matchmode?.value != true ) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("customeal", "click", "customeal_done", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("진단결과", "Page View", "진단결과 페이지뷰")

        callType = arguments?.getString("type") ?: callType
        if (callType == "activity") {
            petInfoItem = arguments?.getParcelable("item")
        } else {
            petInfoItem = foodRecommendDataModel?.petInfo?.value
        }

        Logger.d("petName : ${petInfoItem?.name}, callType : $callType")

        foodRecommendDataModel?.petname?.value = petInfoItem?.name!!
        foodRecommendDataModel?.petimage?.value = petInfoItem?.imageUrl!!
        petName = petInfoItem?.name!!
        petImage = petInfoItem?.imageUrl!!

        petfood_recommend_back_button.setOnClickListener { requireActivity().onBackPressed() }

        petfood_recommend_result_button.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("진단결과_구매버튼", "Click Event", "진단결과 내 구매하기 버튼 클릭")
            if (layoutFeedChoice.visibility == View.GONE) {
                layoutFeedChoice.visibility = View.VISIBLE
            } else {
                if (selectedPosition != -1) {
                    godoMall()
                    Logger.d("godo mall move")
                } else {
                    AppToast(requireContext()).showToastMessage(
                        "구매 단위를 선택해주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }
        }

        food_recommend_result_profile_fix.setOnClickListener {
            startActivity(Intent(requireActivity(), MatchFoodModifyActivity::class.java).apply {
                putExtra("item", petInfoItem)
                putExtra("editType", "matchFood")
            })
        }

        petfood_recommend_food_tab_1_text.setOnClickListener {
            tabMode = FoodTabMode.MAIN_FEED
            showAdapter()
        }

        petfood_recommend_food_tab_2_text.setOnClickListener {
            tabMode = FoodTabMode.SUPPLIER
            showAdapter()
        }

        btnFoldPopup.setOnClickListener {
            layoutFeedChoice.visibility = View.GONE
            selectedPosition = -1
            feedChoiceAdapter.notifyDataSetChanged()
        }

        layoutIngredientDetail.setOnClickListener {
            startActivity(Intent(requireActivity(), IngredientDetailActivity::class.java).apply {
                putExtra("foodId", resultData?.pfId)
                putExtra("type", "food")
            })
        }

        layoutIngredientDetail1.setOnClickListener {
            startActivity(Intent(requireActivity(), IngredientDetailActivity::class.java).apply {
                putExtra("foodId", resultData?.pfId)
                putExtra("type", "food")
            })
        }

        layoutIngredientDetail3.setOnClickListener {
            startActivity(Intent(requireActivity(), IngredientDetailActivity::class.java).apply {
                putExtra("pnId1", resultData?.pnId1)
                putExtra("type", "nutrition")
                putExtra("order", 1)
            })
        }

        layoutIngredientDetail2.setOnClickListener {
            startActivity(Intent(requireActivity(), IngredientDetailActivity::class.java).apply {
                putExtra("pnId2", resultData?.pnId2)
                putExtra("type", "nutrition")
                putExtra("order", 2)
            })
        }

        //============================================================================================

        if (petImage.isNotEmpty()) {
            Glide.with(requireContext())
                .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                .apply(RequestOptions.circleCropTransform())
                .into(food_recommend_result_profile_image)
        } else {
            Glide.with(requireContext())
                .load(R.drawable.img_pet_profile_default)
                .apply(RequestOptions.circleCropTransform())
                .into(food_recommend_result_profile_image)
        }

        setFeedChoiceData()

        feedChoiceAdapter = FeedChoiceAdaoter()
        feedChoiceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedChoiceAdapter
        }

        mApiClient.getMatchFoodResult(RECOMMEND_FOOD_RESULT_REQUEST, petInfoItem?.id!!)
        mApiClient.getMatchFoodResultForVLab(VLAB_REQUEST, petInfoItem?.id!!)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(SPECIES_INFO_REQUEST)) {
            mApiClient.cancelRequest(SPECIES_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(FEED_INFO_REQUEST)) {
            mApiClient.cancelRequest(FEED_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(RECOMMEND_FOOD_RESULT_REQUEST)) {
            mApiClient.cancelRequest(RECOMMEND_FOOD_RESULT_REQUEST)
        }

        if (mApiClient.isRequestRunning(NUTRITION_1_INFO_REQUEST)) {
            mApiClient.cancelRequest(NUTRITION_1_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(NUTRITION_2_INFO_REQUEST)) {
            mApiClient.cancelRequest(NUTRITION_2_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(VLAB_REQUEST)) {
            mApiClient.cancelRequest(VLAB_REQUEST)
        }

        super.onDestroyView()
    }

    private fun showAdapter(){
        when(tabMode){
            FoodTabMode.MAIN_FEED -> {
                petfood_recommend_food_tab_1_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
                petfood_recommend_food_tab_2_text.setTextColor(Helper.readColorRes(R.color.light_grey))

                petfood_recommend_food_tab_1_underline.visibility = View.VISIBLE
                petfood_recommend_food_tab_2_underline.visibility = View.INVISIBLE

//                mainFoodLayout.visibility = View.VISIBLE
//                nutritionLayout.visibility = View.INVISIBLE

//                viewPagerContainer.setCurrentItem(FoodTabMode.MAIN_FEED.ordinal, false)
            }
            FoodTabMode.SUPPLIER -> {
                petfood_recommend_food_tab_1_text.setTextColor(Helper.readColorRes(R.color.light_grey))
                petfood_recommend_food_tab_2_text.setTextColor(Helper.readColorRes(R.color.dark_grey))

                petfood_recommend_food_tab_2_underline.visibility = View.VISIBLE
                petfood_recommend_food_tab_1_underline.visibility = View.INVISIBLE

//                mainFoodLayout.visibility = View.INVISIBLE
//                nutritionLayout.visibility = View.VISIBLE

//                viewPagerContainer.setCurrentItem(FoodTabMode.SUPPLIER.ordinal, false)
            }
        }
    }

    private fun setFeedChoiceData() {
        val item = FeedChoiceItem1(
            "샘플팩 신청",
            "아이디 당 1회 신청 가능",
            "sample"
        )

        feedChoiceItems.add(item)

        val item1 = FeedChoiceItem1(
            "2주 식단",
            "",
            "two_weeks"
        )

        feedChoiceItems.add(item1)

        val item2 = FeedChoiceItem1(
            "4주 식단",
            "",
            "four_weeks"
        )

        feedChoiceItems.add(item2)
    }

    private fun onFeedChoiceClicked(item: FeedChoiceItem1) {
        feedChoiceType = item.type
        Logger.d("feedChoiceType : $feedChoiceType")
    }

    private fun getAgePeriod(period: String) =
        when (period) {
            "old" -> "#노년기"
            "young" -> "#성장기"
            "adult" -> "#성년기"
            else -> ""
        }

    private fun godoMall() {
        val cipherStr = getAESCipherStr()
        when (feedChoiceType) {
            "sample" -> {
                Airbridge.trackEvent("customeal", "click", "buy_sample", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("진단결과_샘플팩", "Click Event", "샘플팩 선택하여 구매하기 버튼 클릭")
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?${sampleData?.godoURL}&referer=true&jsonbody=${cipherStr}"
                        )
                    )
                )
            }

            "two_weeks" -> {
                Airbridge.trackEvent("customeal", "click", "buy_2week", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("진단결과_2주", "Click Event", "2주 선택하여 구매하기 버튼 클릭")
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?${twoWeeksData?.godoURL}&referer=true&jsonbody=${cipherStr}"
                        )
                    )
                )
            }

            "four_weeks" -> {
                Airbridge.trackEvent("customeal", "click", "buy_4week", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("진단결과_4주", "Click Event", "4주 선택하여 구매하기 버튼 클릭")
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?${fourWeeksData?.godoURL}&referer=true&jsonbody=${cipherStr}"
                        )
                    )
                )
            }
        }
    }

    private fun getAESCipherStr(): String {
        val json = JSONObject().apply {
            put("user_id", StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, ""))
            put("name", PetdocApplication.mUserInfo?.name)
        }

        return AES256Cipher.encryptURL(json.toString())
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
            RECOMMEND_FOOD_RESULT_REQUEST -> {
                if ("ok" == event.status) {
                    val items: MutableList<DiagnosisResultData> = Gson().fromJson(
                        event.response,
                        object : TypeToken<MutableList<DiagnosisResultData>>() {}.type
                    )

                    resultData = items[0]

                    ageStatus = getAgePeriod(resultData?.agePeriod!!)
                    setAgeGraph(resultData?.agePeriod!!)
                    setWeightGrapth(resultData?.weight!!.toInt())

                    layoutPregnant.apply {
                        when {
                            resultData?.pregnancy!! > 0 -> {
                                visibility = View.VISIBLE
                                guideTxt7.text = "${Helper.getCompleteWordByJongsung(
                                    petInfoItem?.name!!,
                                    "이는",
                                    "는"
                                )} 임신 ${resultData?.pregnancy}주 상태로 충분한 영양 공급이 이루어져야 합니다. 6주 이상의 임신 후기에는 사료의 급여량 도 늘려야 하니 권장 급여량을 참고하여 급여 해주세요."
                            }

                            else -> {
                                visibility = View.GONE
                            }
                        }
                    }

                    guideTxt8.text = "${Helper.getCompleteWordByJongsung(
                        petInfoItem?.name!!,
                        "이의",
                        "의"
                    )} 1일 권장 급여량 ${resultData?.foodAmount}g을 1일 2회로 나누어 급여해주세요."

                    mApiClient.getNutritionInfo(NUTRITION_1_INFO_REQUEST, resultData?.pnId1!!)
                    mApiClient.getFeedInfo(FEED_INFO_REQUEST, resultData?.pfId!!)
                    mApiClient.getSpeciesInfo(SPECIES_INFO_REQUEST, resultData?.speciesId!!)

                    foodRecommendDataModel?.petResultData?.value = resultData

//                    viewPagerContainer.apply {
//                        pagerAdapter = PagerAdapter()
//                        adapter = pagerAdapter
//                        isUserInputEnabled = false
//                    }
                }
            }

            NUTRITION_1_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    nutritionData1 = Gson().fromJson(event.response, NutritionData::class.java)
                    foodRecommendDataModel?.nutritionData1?.value = nutritionData1
                    nutritionItems.add(nutritionData1!!)

                    when (nutritionData1?.code!!.toUpperCase(Locale.getDefault())) {
                        NutritionType.STRONG.name -> {
                            nutritionType1 = NutritionType.STRONG.type
                        }

                        NutritionType.DIGESTION.name -> {
                            nutritionType1 = NutritionType.DIGESTION.type
                        }

                        NutritionType.IMMUNITY.name -> {
                            nutritionType1 = NutritionType.IMMUNITY.type
                        }

                        NutritionType.JOINT.name -> {
                            nutritionType1 = NutritionType.JOINT.type
                        }

                        NutritionType.OBESITY.name -> {
                            nutritionType1 = NutritionType.OBESITY.type
                        }

                        NutritionType.SKIN.name -> {
                            nutritionType1 = NutritionType.SKIN.type
                        }

                        NutritionType.STONE_KIDNEY.name -> {
                            nutritionType1 = NutritionType.STONE_KIDNEY.type
                        }
                    }

                    mApiClient.getNutritionInfo(NUTRITION_2_INFO_REQUEST, resultData?.pnId2!!)
                }
            }

            NUTRITION_2_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    nutritionData2 = Gson().fromJson(event.response, NutritionData::class.java)
                    foodRecommendDataModel?.nutritionData2?.value = nutritionData2
                    nutritionItems.add(nutritionData2!!)

                    when (nutritionData2?.code!!.toUpperCase(Locale.getDefault())) {
                        NutritionType.STRONG.name -> {
                            nutritionType2 = NutritionType.STRONG.type
                        }

                        NutritionType.DIGESTION.name -> {
                            nutritionType2 = NutritionType.DIGESTION.type
                        }

                        NutritionType.IMMUNITY.name -> {
                            nutritionType2 = NutritionType.IMMUNITY.type
                        }

                        NutritionType.JOINT.name -> {
                            nutritionType2 = NutritionType.JOINT.type
                        }

                        NutritionType.OBESITY.name -> {
                            nutritionType2 = NutritionType.OBESITY.type
                        }

                        NutritionType.SKIN.name -> {
                            nutritionType2 = NutritionType.SKIN.type
                        }

                        NutritionType.STONE_KIDNEY.name -> {
                            nutritionType2 = NutritionType.STONE_KIDNEY.type
                        }
                    }

                    resultKeyword.text = "${ageStatus} #${nutritionType1} #${nutritionType2}"

                    foodRecommendDataModel?.nutritionType1?.value = nutritionType1
                    foodRecommendDataModel?.nutritionType2?.value = nutritionType2

                    pageAdapter = NutritionAdapter()
//                    viewPagerContainer.apply {
//                        adapter = pageAdapter
//                        orientation = ViewPager2.ORIENTATION_HORIZONTAL
//                        isNestedScrollingEnabled = false
//                    }

//                    TabLayoutMediator(indicator, viewPagerContainer) {tab, position ->  }.attach()

                    ingredientContainer2.removeAllViews()
                    for (i in 0 until nutritionItems[1].innerNutrList.size) {
                        ingredientContainer2.addView(
                            IngredientNutritionTag(requireContext(), nutritionItems[1].innerNutrList[i].name, nutritionItems[1].innerNutrList[i].description, nutritionItems[1].code)
                        )
                    }

                    if (nutritionType1 == NutritionType.STRONG.type || nutritionType2 == NutritionType.STRONG.type) {
                        guideNutritionTxt1.text = "${Helper.getCompleteWordByJongsung(
                            petName,
                            "이는",
                            "는"
                        )} 튼튼한 아이군요! 계속해서 건강함을 유지할 수 있도록 밸런스 영양겔을 추천 드립니다."
                    } else {
                        guideNutritionTxt1.text = "${Helper.getCompleteWordByJongsung(
                            petName,
                            "이는",
                            "는"
                        )} 현재 ${nutritionType1},${nutritionType2} 관련 질환이 걱정되므로 이를 완화, 개선하는데 도움을 주는 보조 영양겔을 추천 드립니다."
                    }

                    textViewCustomMeal1.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[0].code)))
                    nutrition1.text = nutritionType1
                    nutrition1.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[0].code)))

                    textViewCustomMeal2.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[1].code)))
                    nutrition2.text = nutritionType2
                    nutrition2.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[1].code)))
                }
            }

            VLAB_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val messageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject
                        val twoWeeks = obj["14Day"].toString()
                        val fourWeeks = obj["28Day"].toString()
                        val sample = obj["sample"].toString()

                        twoWeeksData = Gson().fromJson(twoWeeks, VLabData::class.java)
                        fourWeeksData = Gson().fromJson(fourWeeks, VLabData::class.java)
                        sampleData = Gson().fromJson(sample, VLabData::class.java)
                    } else {
                        Logger.d("error : $messageKey")
                    }
                }
            }

            FEED_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    feedData = Gson().fromJson(event.response, FeedInfoData::class.java)

                    mainFoodName.text = "${feedData?.name!!.replace("브이랩 ", "")}\n반습식 사료"
                    mainFoodName.setTextColor(Color.parseColor(getFoodTxtColor(feedData?.code!!)))
                    foodAmount.text = "${resultData?.foodAmount}g"

                    Glide.with(requireContext())
                        .load(if (feedData?.prodImgUrl!!.startsWith("http")) feedData?.prodImgUrl else "${AppConstants.IMAGE_URL}${feedData?.prodImgUrl}")
                        .into(mainFoodImg)

                    val id = resources.getIdentifier("${feedData?.code}_on", "drawable", context?.packageName)
                    mainFoodTypeImg.setImageResource(id)
                    textViewCustomMeal.setTextColor(Color.parseColor(getFoodTxtColor(feedData?.code!!)))
                    mainFoodType.text = getFoodType(feedData?.code!!)
                    mainFoodType.setTextColor(Color.parseColor(getFoodTxtColor(feedData?.code!!)))

                    guideTxt1.text = "${Helper.getCompleteWordByJongsung(
                        petName,
                        "이가",
                        "가"
                    )} 좋아하는 ${getFoodType(feedData?.code!!)}를 주재료로 한 단일 육류의 반습식 사료를 추천 드립니다."

                    ingredientContainer.removeAllViews()
                    for (i in 0 until feedData?.innerFoodList!!.size) {
                        ingredientContainer.addView(
                            IngredientTag(requireContext(), feedData?.innerFoodList!![i].description)
                        )
                    }

                    guideTxt2.text = "${Helper.getCompleteWordByJongsung(
                        petName,
                        "이의",
                        "의"
                    )} 연령주기와 몸무게를 고려하였을 때 하루 필요 칼로리는 ${resultData?.calorie}kcal 입니다."

                    guideTxt3.text = "${Helper.getCompleteWordByJongsung(
                        petName,
                        "이는",
                        "는"
                    )} ${resultData?.ageYear}세 ${resultData?.ageMonth}개월로 연령주기는 ${getAgePeriod(resultData?.agePeriod!!)} 입니다."

                    guideTxt4.text = "${getAgePeriod(resultData?.agePeriod!!)} 반려동물의 경우 적정 체중을 일정하게 유지하는 것이 중요합니다."

                    age.text = "${resultData?.ageYear}세 ${resultData?.ageMonth}개월"
                    ageType.text = getAgePeriod(resultData?.agePeriod!!)
                }
            }

            SPECIES_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    speciesData = Gson().fromJson(event.response, SpeciesInfoData::class.java)

                    guideTxt5.apply {
                        when {
                            speciesData?.name!!.startsWith("강아지") -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이는",
                                    "는"
                                )} ${getSpeciesSize(speciesData?.size!!)}견으로 분류되는 강아지 입니다."
                            }

                            else -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 품종은 ${speciesData?.name}으로 ${getSpeciesSize(speciesData?.size!!)}견 입니다."
                            }
                        }
                    }

                    guideTxt6.apply {
                        when {
                            speciesData?.kind!!.startsWith("강아지") -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 몸무게는 ${resultData?.weight}kg으로 ${getAgePeriod(resultData?.agePeriod!!)} ${getSpeciesSize(speciesData?.size!!)}견 " +
                                        "강아지 표준 몸무게를 기준으로 보았을 때 ${getWeightStatus(resultData?.weight!!.toInt())} 입니다."
                            }

                            else -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 몸무게는 ${resultData?.weight}kg으로 ${getAgePeriod(resultData?.agePeriod!!)} ${speciesData?.name!!} 표준 몸무게를 기준으로 보았을 때 ${getWeightStatus(
                                    resultData?.weight!!.toInt()
                                )} 입니다."
                            }
                        }
                    }

                    guideTxt9.apply {
                        when (getWeightStatus(resultData?.weight!!.toInt())) {
                            "왜소" -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이는",
                                    "는"
                                )} 약간 마른 상태로 충분한 영양 공급이 필요합니다. 정상 체중으로 유지 할 수 있도록 하루 권장량을 지켜서 급여해주세요."
                            }

                            "표준" -> {
                                text = "계속해서 정상 체중을 유지할 수 있도록 하루 권장량을 지켜서 급여해주세요."
                            }

                            "비만" -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이는",
                                    "는"
                                )} 품종별 표준 체중보다는 높은 편으로 하루 권장량을 지켜 급여해주시고, 반려동물이 더욱 활발하게 활동할 수 있도록 도와주세요."
                            }
                        }
                    }

                    guideNutritionTxt2.apply {
                        when {
                            speciesData?.name!!.startsWith("강아지") -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이는",
                                    "는"
                                )} 특별한 유전병 없이 대체로 건강한 편이지만 ${nutritionType1},${nutritionType2} 관련 질병에 취약함으로 미리 예방하셔야 합니다."
                            }

                            else -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 품종은 ${Helper.getCompleteWordByJongsung(speciesData?.name!!, "으로", "로")} ${nutritionType1},${nutritionType2} 관련 질병에 취약함으로 미리 예방 하셔야 합니다."
                            }
                        }
                    }

                    weight.text = "${resultData?.weight!!.toInt()}kg"
                    weightStatus.text = getWeightStatus(resultData?.weight!!.toInt())

                }
            }
        }
    }

    //==============================================================================================

    inner class FeedChoiceAdaoter : RecyclerView.Adapter<FeedChoiceHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            FeedChoiceHolder(layoutInflater.inflate(R.layout.adapter_feed_choice_item, parent, false))

        override fun onBindViewHolder(holder: FeedChoiceHolder, position: Int) {
            if (selectedPosition == position) {
                holder.itemView.btnCheck.visibility = View.VISIBLE
            } else {
                holder.itemView.btnCheck.visibility = View.GONE
            }

            holder.setTitle(feedChoiceItems[position].title)
            holder.setDesc(feedChoiceItems[position].desc)

            holder.itemView.setOnClickListener {
                selectedPosition = position
                onFeedChoiceClicked(feedChoiceItems[position])
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = feedChoiceItems.size
    }

    inner class FeedChoiceHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setTitle(_title: String) {
            item.feedTitle.text = _title
        }

        fun setDesc(_desc: String) {
            item.feedDesc.apply {
                when {
                    _desc.isNotEmpty() -> {
                        visibility = View.VISIBLE
                        text = _desc
                    }

                    else -> visibility = View.GONE
                }
            }
        }
    }

    //==============================================================================================
    inner class PagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(MainFoodFragment())
                add(NutritionFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }

    // 주식사료 UI =================================================================================
    private fun getFoodType(code: String) =
        when (code) {
            "chicken_meet" -> "닭고기"
            "salmon_tuna" -> "연어&참치"
            "beef" -> "소고기"
            "lamb" -> "양고기"
            "duck_meet" -> "오리고기"
            else -> "알수없음"
        }

    private fun getFoodTxtColor(code: String) =
        when (code) {
            FoodStatus.beef.name -> FoodStatus.beef.color
            FoodStatus.chicken_meet.name -> FoodStatus.chicken_meet.color
            FoodStatus.duck_meet.name -> FoodStatus.duck_meet.color
            FoodStatus.lamb.name -> FoodStatus.lamb.color
            FoodStatus.salmon_tuna.name -> FoodStatus.salmon_tuna.color
            FoodStatus.meet_fish.name -> FoodStatus.meet_fish.color
            else -> ""
        }

    private fun getSpeciesSize(size:String) =
        when (size) {
            SpeciesSize.XS.name -> SpeciesSize.XS.size
            SpeciesSize.S.name -> SpeciesSize.S.size
            SpeciesSize.M.name -> SpeciesSize.M.size
            SpeciesSize.L.name -> SpeciesSize.L.size
            SpeciesSize.XL.name -> SpeciesSize.XL.size
            else -> ""
        }

    private fun getWeightStatus(weight: Int) =
        if (9 < weight) {
            "비만"
        } else if(5 <= weight && 9 >= weight) {
            "표준"
        } else {
            "왜소"
        }

    private fun setAgeGraph(period: String) {
        when (period) {
            "young" -> {
                petAdultImg.visibility = View.INVISIBLE
                petOldImg.visibility = View.INVISIBLE

                youngGraph.visibility = View.VISIBLE
                youngSelectBar.visibility = View.VISIBLE

                youngTxt.setTextColor(Helper.readColorRes(R.color.dark_grey))
                youngPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

                if (petImage.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petYoungImg)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petYoungImg)
                }
            }

            "adult" -> {
                petOldImg.visibility = View.INVISIBLE
                petYoungImg.visibility = View.INVISIBLE

                adultGraph.visibility = View.VISIBLE
                adultSelectBar.visibility = View.VISIBLE
                youngFillBar.visibility = View.VISIBLE

                adultTxt.setTextColor(Helper.readColorRes(R.color.dark_grey))
                adultPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

                if (petImage.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petAdultImg)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petAdultImg)
                }
            }

            else -> {
                petAdultImg.visibility = View.INVISIBLE
                petYoungImg.visibility = View.INVISIBLE

                oldGraph.visibility = View.VISIBLE
                oldSelectBar.visibility = View.VISIBLE
                youngFillBar.visibility = View.VISIBLE
                adultFillBar.visibility = View.VISIBLE

                oldTxt.setTextColor(Helper.readColorRes(R.color.dark_grey))
                oldPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

                if (petImage.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petOldImg)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petOldImg)
                }
            }
        }
    }

    private fun setWeightGrapth(weight: Int) {
        if (9 < weight) {
            petImg1.visibility = View.INVISIBLE
            petImg2.visibility = View.INVISIBLE

            overWeightGraph.visibility = View.VISIBLE
            overWeightSelectBar.visibility = View.VISIBLE
            underWeightFillBar.visibility = View.VISIBLE
            standardFillBar.visibility = View.VISIBLE

            overWeightTxt.setTextColor(Helper.readColorRes(R.color.dark_grey))
            overWeightPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

            if (petImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg3)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.img_pet_profile_default)
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg3)
            }
        } else if(5 <= weight && 9 >= weight) {
            petImg3.visibility = View.INVISIBLE
            petImg1.visibility = View.INVISIBLE

            standardGraph.visibility = View.VISIBLE
            standardSelectBar.visibility = View.VISIBLE
            underWeightFillBar.visibility = View.VISIBLE

            standardTxt.setTextColor(Helper.readColorRes(R.color.dark_grey))
            standardPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

            if (petImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg2)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.img_pet_profile_default)
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg2)
            }
        } else {
            petImg2.visibility = View.INVISIBLE
            petImg3.visibility = View.INVISIBLE

            underWeightGraph.visibility = View.VISIBLE
            underWeightSelectBar.visibility = View.VISIBLE

            underWeight.setTextColor(Helper.readColorRes(R.color.dark_grey))
            underWeightPeriod.setTextColor(Helper.readColorRes(R.color.dark_grey))

            if (petImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(if(petImage.startsWith("http")) petImage else "${AppConstants.IMAGE_URL}${petImage}")
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg1)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.img_pet_profile_default)
                    .apply(RequestOptions.circleCropTransform())
                    .into(petImg1)
            }
        }
    }

    //==============================================================================================
    inner class IngredientTag(context: Context, incredientDesc:String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_main_food_ingredient_item, null, false)

            view.ingredientDesc.text = incredientDesc
            view.point.setBackgroundResource(getCircleType(feedData?.code!!))

            addView(view)
        }

        private fun getCircleType(code: String) =
            when (code) {
                "chicken_meet" -> R.drawable.chicken_meet_circle
                "salmon_tuna" -> R.drawable.salmon_tuna_circle
                "beef" -> R.drawable.beef_circle
                "lamb" -> R.drawable.lamb_circle
                "duck_meet" -> R.drawable.duck_meet_circle
                else -> R.drawable.salmon_circle
            }
    }

    // 보조 영약겔 UI ================================================================================
    private fun getNutritionTxtColor(code: String) =
        when (code) {
            NutritionStatus.stone_kidney.name -> NutritionStatus.stone_kidney.color
            NutritionStatus.obesity.name -> NutritionStatus.obesity.color
            NutritionStatus.skin.name -> NutritionStatus.skin.color
            NutritionStatus.strong.name -> NutritionStatus.strong.color
            NutritionStatus.digestion.name -> NutritionStatus.digestion.color
            NutritionStatus.joint.name -> NutritionStatus.joint.color
            NutritionStatus.immunity.name -> NutritionStatus.immunity.color
            else -> ""
        }

    inner class NutritionAdapter : RecyclerView.Adapter<NutritionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NutritionHolder(layoutInflater.inflate(R.layout.adapter_nutrition_item, parent, false))

        override fun onBindViewHolder(holder: NutritionHolder, position: Int) {
            holder.setName(nutritionItems[position].name, nutritionItems[position].code)
            holder.setImage(nutritionItems[position].prodImgUrl)
            holder.setFoodAmount("7")
        }

        override fun getItemCount() = nutritionItems.size
    }

    inner class NutritionHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setName(_name: String, code:String) {
            item.nutritionName.text = _name.replace("브이랩 ", "")
            item.nutritionName.apply {
                when (code) {
                    NutritionStatus.digestion.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.digestion.color))
                    }

                    NutritionStatus.immunity.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.immunity.color))
                    }

                    NutritionStatus.joint.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.joint.color))
                    }

                    NutritionStatus.obesity.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.obesity.color))
                    }

                    NutritionStatus.skin.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.skin.color))
                    }

                    NutritionStatus.strong.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.strong.color))
                    }

                    NutritionStatus.stone_kidney.name -> {
                        setTextColor(Color.parseColor(NutritionStatus.stone_kidney.color))
                    }
                }
            }
        }

        fun setFoodAmount(_amount: String) {
            item.foodAmount.text = "1포(${_amount})g"
        }

        fun setImage(_url: String) {
            Glide.with(requireContext())
                .load(if (_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}${_url}")
                .apply(RequestOptions.circleCropTransform())
                .into(item.nutritionImg)
        }
    }

    //================================================================================================
    inner class IngredientNutritionTag(context: Context, incredient:String, incredientDesc:String, code: String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_ingredient_item, null, false)

            view.ingredient.text = incredient
            view.ingredientDesc.text = incredientDesc
            view.point.setBackgroundResource(getCircleType(code))

            addView(view)
        }

        private fun getCircleType(code: String) =
            when (code) {
                "obesity" -> R.drawable.fat_circle
                "stone_kidney" -> R.drawable.stone_kidney_circle
                "immunity" -> R.drawable.immunity_circle
                "digestion" -> R.drawable.digestion_circle
                "joint" -> R.drawable.joint_circle
                "skin" -> R.drawable.skin_circle
                "strong" -> R.drawable.strong_circle
                else -> R.drawable.salmon_circle
            }
    }
}

data class FeedChoiceItem1(
    var title:String,
    var desc:String,
    var type:String
)