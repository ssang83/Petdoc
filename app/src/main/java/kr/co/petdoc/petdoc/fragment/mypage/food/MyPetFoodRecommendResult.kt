package kr.co.petdoc.petdoc.fragment.mypage.food

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_feed_choice_item.view.*
import kotlinx.android.synthetic.main.fragment_my_health_care_result.*
import kotlinx.android.synthetic.main.fragment_my_health_care_result.petImage
import kotlinx.android.synthetic.main.fragment_pet_food_recommend.*
import kotlinx.android.synthetic.main.fragment_pet_food_recommend.bottomSheetPager
import kotlinx.android.synthetic.main.fragment_pet_food_recommend.tabLayout
import kotlinx.android.synthetic.main.fragment_pet_home.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodModifyActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetFoodRecommendBinding
import kr.co.petdoc.petdoc.enum.NutritionType
import kr.co.petdoc.petdoc.event.PetInfoRefreshEvent
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.customeal.MainFoodFragment
import kr.co.petdoc.petdoc.fragment.customeal.NutritionFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisResultData
import kr.co.petdoc.petdoc.network.response.submodel.NutritionData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.network.response.submodel.VLabData
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.BottomSheetViewPager
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

class MyPetFoodRecommendResult : BaseFragment() {

    private val TAG = "MyPetFoodRecommendResult"
    private val RECOMMEND_FOOD_RESULT_REQUEST = "$TAG.recommendFoodResultRequest"
    private val NUTRITION_1_INFO_REQUEST = "$TAG.nutrition1InfoRequest"
    private val NUTRITION_2_INFO_REQUEST = "$TAG.nutrition2InfoRequest"
    private val VLAB_REQUEST = "$TAG.vlabUrlRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null

    private var petInfoItem:PetInfoItem? = null
    private var nutritionData1:NutritionData? = null
    private var nutritionData2:NutritionData? = null
    private var resultData:DiagnosisResultData? = null
    private var twoWeeksData:VLabData? = null
    private var fourWeeksData:VLabData? = null
    private var sampleData:VLabData? = null

    private lateinit var feedChoiceAdapter: FeedChoiceAdaoter
    private var feedChoiceItems:MutableList<FeedChoiceItem> = mutableListOf()

    private var selectedPosition = -1
    private var feedChoiceType = ""

    private var ageStatus = ""
    private var nutritionType1 = ""
    private var nutritionType2 = ""

    private var callType = ""
    private var isPurchase = false

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<BottomSheetViewPager>

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databiding = DataBindingUtil.inflate<FragmentPetFoodRecommendBinding>(inflater, R.layout.fragment_pet_food_recommend, container, false)
            databiding.lifecycleOwner = requireActivity()
            databiding.foodreference = foodRecommendDataModel

        if(foodRecommendDataModel?.matchmode?.value == true){
            databiding.petfoodRecommendBackButton.visibility = View.GONE
            databiding.mypageTitleText.visibility = View.GONE

            databiding.foodRecommendResultProfileFix.visibility = View.GONE
            databiding.toolTipImg.visibility = View.GONE
        }

        return databiding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("customeal", "click", "customeal_done", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("진단결과", "Page View", "진단결과 페이지뷰")
        EventBus.getDefault().post(PetInfoRefreshEvent())

        callType = arguments?.getString("type") ?: callType
        if (callType == "activity") {
            petInfoItem = arguments?.getParcelable("item")
            isPurchase = arguments?.getBoolean("isPurchase", isPurchase)!!
        } else {
            petInfoItem = foodRecommendDataModel?.petInfo?.value
            isPurchase = foodRecommendDataModel?.isPurchase?.value!!
        }

        Logger.d("petName : ${petInfoItem?.name}, callType : $callType")

        foodRecommendDataModel?.petname?.value = petInfoItem?.name!!
        foodRecommendDataModel?.petimage?.value = petInfoItem?.imageUrl!!

        if (isPurchase) {
            layoutFeedChoice.visibility = View.VISIBLE
            dim.visibility = View.VISIBLE
        }

        //==========================================================================================
        petfood_recommend_back_button.setOnClickListener { requireActivity().onBackPressed() }

        petfood_recommend_result_button.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("진단결과_구매버튼", "Click Event", "진단결과 내 구매하기 버튼 클릭")
            if (layoutFeedChoice.visibility == View.GONE) {
                layoutFeedChoice.visibility = View.VISIBLE
                dim.visibility = View.VISIBLE
            } else {
                if (selectedPosition != -1) {
                    godoMall()
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

        btnFoldPopup.setOnClickListener {
            layoutFeedChoice.visibility = View.GONE
            dim.visibility = View.GONE
            selectedPosition = -1
            feedChoiceAdapter.notifyDataSetChanged()
        }

        emptyView.setOnClickListener { toolTipImg.visibility = View.GONE }

        bottomSheetBehavior = BottomSheetBehavior.from<BottomSheetViewPager>(bottomSheetPager)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Logger.d("slideOffset : $slideOffset")
                toolTipImg.visibility = View.GONE
            }
        })

        //==============================================================================================
        food_recommend_result_profile_image.apply {
            when {
                petInfoItem?.imageUrl.isNullOrBlank() -> {
                    Glide.with(requireContext())
                            .load(R.drawable.img_customeal_pet_profile)
                            .apply(RequestOptions.circleCropTransform())
                            .into(this)
                }

                else -> {
                    Glide.with(requireContext())
                            .load(if (petInfoItem?.imageUrl!!.startsWith("http")) petInfoItem?.imageUrl else "${AppConstants.IMAGE_URL}${petInfoItem?.imageUrl}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(this)
                }
            }
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
        super.onDestroyView()
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

    private fun setFeedChoiceData() {
        val item1 = FeedChoiceItem(
            "2주 식단",
            "",
            "two_weeks"
        )

        feedChoiceItems.add(item1)

        val item2 = FeedChoiceItem(
            "4주 식단",
            "",
            "four_weeks"
        )

        feedChoiceItems.add(item2)
    }

    private fun onFeedChoiceClicked(item: FeedChoiceItem) {
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
            "two_weeks" -> {
                Airbridge.trackEvent("customeal", "click", "buy_2week", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("진단결과_2주", "Click Event", "2주 선택하여 구매하기 버튼 클릭")
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://vlab.kr/member/signin.php?renderurl=${twoWeeksData?.godoURL}&referer=true&jsonbody=${cipherStr}"
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
                            "https://vlab.kr/member/signin.php?renderurl=${fourWeeksData?.godoURL}&referer=true&jsonbody=${cipherStr}"
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

    private fun setTabLayoutFont() {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount

        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    tabViewChild.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/notosanskr_bold.otf")
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

                    mApiClient.getNutritionInfo(NUTRITION_1_INFO_REQUEST, resultData?.pnId1!!)

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

                    bottomSheetPager.adapter = BottomSheetPagerAdapter(childFragmentManager)
                    bottomSheetPager.setSwipeEnable(false)
                    setTabLayoutFont()
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
    class BottomSheetPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(MainFoodFragment())
                add(NutritionFragment())
            }
        }

        override fun getCount() = mFragmentList.size
        override fun getItem(position: Int) = mFragmentList[position]
        override fun getPageTitle(position: Int) =
            when (position) {
                0 -> "주식사료"
                else -> "보조 영양겔"
            }

    }
}

data class FeedChoiceItem(
    var title:String,
    var desc:String,
    var type:String
)