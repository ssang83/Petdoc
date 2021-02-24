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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_nutrition_item.view.*
import kotlinx.android.synthetic.main.fragment_nutrition_recommend.*
import kotlinx.android.synthetic.main.view_ingredient_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.IngredientDetailActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.NutritionStatus
import kr.co.petdoc.petdoc.enum.NutritionType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisResultData
import kr.co.petdoc.petdoc.network.response.submodel.NutritionData
import kr.co.petdoc.petdoc.network.response.submodel.SpeciesInfoData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: NutritionFragment
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
class NutritionFragment : BaseFragment() {

    private val TAG = "NutritionFragment"
    private val SPECIES_INFO_REQUEST = "$TAG.speciesInfoRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var speciesData: SpeciesInfoData? = null
    private var resultData: DiagnosisResultData? = null

    private var nutritionItems:MutableList<NutritionData> = mutableListOf()
    private lateinit var pageAdapter:NutritionAdapter

    private var petName = ""
    private var disease1 = ""
    private var disease2 = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)
        return inflater.inflate(R.layout.fragment_nutrition_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultData = foodRecommendDataModel?.petResultData?.value
        petName = foodRecommendDataModel?.petname?.value!!
        disease1 = foodRecommendDataModel?.nutritionType1?.value!!
        disease2 = foodRecommendDataModel?.nutritionType2?.value!!

        nutritionItems.add(foodRecommendDataModel?.nutritionData1?.value!!)
        nutritionItems.add(foodRecommendDataModel?.nutritionData2?.value!!)

        //===========================================================================================
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

        btnTop.setOnClickListener { nutritionScrollView.scrollTo(0, 0) }

        //==========================================================================================
        pageAdapter = NutritionAdapter()
        viewPagerContainer.apply {
            adapter = pageAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        TabLayoutMediator(indicator, viewPagerContainer) {tab, position ->  }.attach()

        //===========================================================================================
        ingredientContainer1.removeAllViews()
        for (i in 0 until nutritionItems[0].innerNutrList.size) {
            ingredientContainer1.addView(
                IngredientTag(requireContext(), nutritionItems[0].innerNutrList[i].name, nutritionItems[0].innerNutrList[i].description, nutritionItems[0].code)
            )
        }

        ingredientContainer2.removeAllViews()
        for (i in 0 until nutritionItems[1].innerNutrList.size) {
            ingredientContainer2.addView(
                IngredientTag(requireContext(), nutritionItems[1].innerNutrList[i].name, nutritionItems[1].innerNutrList[i].description, nutritionItems[1].code)
            )
        }

        if (disease1 == NutritionType.STRONG.type || disease2 == NutritionType.STRONG.type) {
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
            )} 현재 ${disease1},${disease2} 관련 질환이 걱정되므로 이를 완화, 개선하는데 도움을 주는 보조 영양겔을 추천 드립니다."
        }

        textViewCustomMeal1.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[0].code)))
        nutrition1.text = disease1
        nutrition1.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[0].code)))
        nutritionTypeImg1.setImageResource(getNutritionImg(nutritionItems[0].code))

        textViewCustomMeal2.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[1].code)))
        nutrition2.text = disease2
        nutrition2.setTextColor(Color.parseColor(getNutritionTxtColor(nutritionItems[1].code)))
        nutritionTypeImg2.setImageResource(getNutritionImg(nutritionItems[1].code))

        //============================================================================================

        mApiClient.getSpeciesInfo(SPECIES_INFO_REQUEST, resultData?.speciesId!!)
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
            SPECIES_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    speciesData = Gson().fromJson(event.response, SpeciesInfoData::class.java)

                    guideNutritionTxt2.apply {
                        when {
                            speciesData?.name!!.startsWith("강아지") -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이는",
                                    "는"
                                )} 특별한 유전병 없이 대체로 건강한 편이지만 ${disease1},${disease2} 관련 질병에 취약함으로 미리 예방하셔야 합니다."
                            }

                            else -> {
                                text = "${Helper.getCompleteWordByJongsung(
                                    petName,
                                    "이의",
                                    "의"
                                )} 품종은 ${Helper.getCompleteWordByJongsung(speciesData?.name!!, "으로", "로")} ${disease1},${disease2} 관련 질병에 취약함으로 미리 예방 하셔야 합니다."
                            }
                        }
                    }
                }
            }
        }
    }

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

    private fun getNutritionImg(code: String) =
        when (code) {
            NutritionStatus.stone_kidney.name -> resources.getIdentifier("stone_kidney_on", "drawable", context?.packageName)
            NutritionStatus.obesity.name -> resources.getIdentifier("fat_on", "drawable", context?.packageName)
            NutritionStatus.skin.name -> resources.getIdentifier("skin_on", "drawable", context?.packageName)
            NutritionStatus.strong.name -> resources.getIdentifier("ballance_on", "drawable", context?.packageName)
            NutritionStatus.digestion.name -> resources.getIdentifier("digestion_on", "drawable", context?.packageName)
            NutritionStatus.joint.name -> resources.getIdentifier("joint_on", "drawable", context?.packageName)
            NutritionStatus.immunity.name -> resources.getIdentifier("immunity_on", "drawable", context?.packageName)
            else -> 0
        }

    //==============================================================================================
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
            item.nutritionName.text = "${_name.replace("브이랩 ", "")} 영양겔"
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
    inner class IngredientTag(context: Context, incredient:String, incredientDesc:String, code: String) : FrameLayout(context) {

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