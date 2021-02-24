package kr.co.petdoc.petdoc.fragment.customeal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_main_food_recommend.*
import kotlinx.android.synthetic.main.view_main_food_ingredient_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.IngredientDetailActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.FoodStatus
import kr.co.petdoc.petdoc.enum.SpeciesSize
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisResultData
import kr.co.petdoc.petdoc.network.response.submodel.FeedInfoData
import kr.co.petdoc.petdoc.network.response.submodel.FoodIngredient
import kr.co.petdoc.petdoc.network.response.submodel.SpeciesInfoData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: MainFoodFragment
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
class MainFoodFragment : BaseFragment() {

    private val TAG = "MainFoodFragment"
    private val SPECIES_INFO_REQUEST = "$TAG.speciesInfoRequest"
    private val FEED_INFO_REQUEST = "$TAG.feedInfoRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var resultData:DiagnosisResultData? = null
    private var feedData: FeedInfoData? = null
    private var speciesData:SpeciesInfoData? = null

    private var petName = ""
    private var petImage = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)
        return inflater.inflate(R.layout.fragment_main_food_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultData = foodRecommendDataModel?.petResultData?.value
        petName = foodRecommendDataModel?.petname?.value.toString()
        petImage = foodRecommendDataModel?.petimage?.value.toString()

        //===========================================================================================
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

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }

        //===========================================================================================
        if (foodRecommendDataModel?.gender?.value.toString() == "woman") {
            layoutPregnant.apply {
                when {
                    resultData?.pregnancy!! > 0 -> {
                        visibility = View.VISIBLE
                        guideTxt7.text = "${Helper.getCompleteWordByJongsung(
                            petName,
                            "이는",
                            "는"
                        )} 임신 ${resultData?.pregnancy}주 상태로 충분한 영양 공급이 이루어져야 합니다. 6주 이상의 임신 후기에는 사료의 급여량 도 늘려야 하니 권장 급여량을 참고하여 급여 해주세요."
                    }

                    else -> {
                        visibility = View.GONE
                    }
                }
            }
        } else {
            layoutPregnant.visibility = View.GONE
        }

        guideTxt8.text = "${Helper.getCompleteWordByJongsung(
            petName,
            "이의",
            "의"
        )} 1일 권장 급여량 ${resultData?.foodAmount}g을 1일 2회로 나누어 급여해주세요."

        resultData?.agePeriod?.let { setAgeGraph(it) }
        resultData?.weight?.let { setWeightGrapth(it.toFloat()) }

        mApiClient.getFeedInfo(FEED_INFO_REQUEST, resultData?.pfId!!)
        mApiClient.getSpeciesInfo(SPECIES_INFO_REQUEST, resultData?.speciesId!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(SPECIES_INFO_REQUEST)) {
            mApiClient.cancelRequest(SPECIES_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(FEED_INFO_REQUEST)) {
            mApiClient.cancelRequest(FEED_INFO_REQUEST)
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
                                        "강아지 표준 몸무게를 기준으로 보았을 때 ${getWeightStatus(resultData?.weight!!.toFloat())} 입니다."
                            }

                            else -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 몸무게는 ${resultData?.weight}kg으로 ${getAgePeriod(resultData?.agePeriod!!)} ${speciesData?.name!!} 표준 몸무게를 기준으로 보았을 때 ${getWeightStatus(
                                    resultData?.weight!!.toFloat()
                                )} 입니다."
                            }
                        }
                    }

                    guideTxt9.apply {
                        when (getWeightStatus(resultData?.weight!!.toFloat())) {
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

                    weight.text = "${resultData?.weight!!}kg"
                    weightStatus.text = getWeightStatus(resultData?.weight!!.toFloat())

                }
            }
        }
    }

    //==============================================================================================
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

    private fun getAgePeriod(period:String) =
        when (period) {
            "adult" -> "성년기"
            "young" -> "성장기"
            else -> "노년기"
        }

    private fun getWeightStatus(weight: Float) =
        if (9.0 < weight) {
            "비만"
        } else if(5.0 <= weight && 9.0 >= weight) {
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

    private fun setWeightGrapth(weight: Float) {
        if (9.0 < weight) {
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
        } else if(5.0 <= weight && 9.0 >= weight) {
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
}