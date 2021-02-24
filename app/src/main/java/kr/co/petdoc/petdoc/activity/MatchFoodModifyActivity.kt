package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_match_food_modify.*
import kotlinx.android.synthetic.main.view_clinic_type.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CustoMealInfoData
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisResultData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Petdoc
 * Class: PetInfoDetailActivity
 * Created by kimjoonsung on 2020/07/17.
 *
 * Description :
 */
class MatchFoodModifyActivity : BaseActivity() {

    private val TAG = "MatchFoodModifyActivity"
    private val CUSTOMEAL_INFO_REQUEST = "$TAG.custoMealInfoRequest"

    private var foodRecommentDataModel : FoodRecommentDataModel? = null
    private var petInfoModel: PetAddInfoDataModel? = null

    private var petInfoItem: PetInfoItem? = null

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""

    private var editType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_food_modify)

        foodRecommentDataModel = ViewModelProvider(this).get(FoodRecommentDataModel::class.java)
        petInfoModel = ViewModelProvider(this).get(PetAddInfoDataModel::class.java)

        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))

        petInfoItem = intent?.getParcelableExtra("item")
        editType = intent?.getStringExtra("editType") ?: editType

        petInfoModel?.petId?.value = petInfoItem?.id
        petInfoModel?.petName?.value = petInfoItem?.name
        petInfoModel?.petKind?.value = petInfoItem?.kind
        petInfoModel?.petKindKey?.value = getKindKey(petInfoItem?.kind!!)
        petInfoModel?.petBreed?.value = petInfoItem?.species
        petInfoModel?.petBreedId?.value = petInfoItem?.speciesId
        petInfoModel?.petSex?.value = petInfoItem?.gender
        petInfoModel?.petNeutralization?.value = petInfoItem?.isNeutralized
        petInfoModel?.petVaccine?.value = petInfoItem?.inoculationStatus
        petInfoModel?.petProfile?.value = petInfoItem?.imageUrl
        petInfoModel?.petBirth?.value = petInfoItem?.birthday
        petInfoModel?.petAgeType?.value = petInfoItem?.ageType
        petInfoModel?.petAge?.value = calculateBirthday(petInfoItem!!)
        petInfoModel?.type?.value = PetAddType.EDIT.ordinal

        if (editType == "matchFood") {
            mApiClient.getCustomealInfo(CUSTOMEAL_INFO_REQUEST, petInfoItem?.id!!)
        } else {
            findNavController(R.id.navMatchFoodModifyFragment).navigate(R.id.petDefaultInfoFragment2)
        }
    }

    override fun onBackPressed() {
        findNavController(R.id.navMatchFoodModifyFragment)?.let {
            if (editType == "matchFood") {
                if(it.currentDestination?.id == R.id.petInfoDetailFragment2) {
                    finish()
                    return
                }
            } else {
                if(it.currentDestination?.id == R.id.petDefaultInfoFragment2) {
                    finish()
                    return
                }
            }
        }
        super.onBackPressed()
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
            CUSTOMEAL_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val items: MutableList<CustoMealInfoData> = Gson().fromJson(
                        event.response,
                        object : TypeToken<MutableList<CustoMealInfoData>>() {}.type
                    )

                    foodRecommentDataModel?.petname?.value = petInfoItem?.name
                    foodRecommentDataModel?.petimage?.value = petInfoItem?.imageUrl
                    foodRecommentDataModel?.cmInfoId?.value = petInfoItem?.cmInfoId
                    foodRecommentDataModel?.petId?.value = petInfoItem?.id
                    foodRecommentDataModel?.species?.value = petInfoItem?.species
                    foodRecommentDataModel?.gender?.value = petInfoItem?.gender
                    foodRecommentDataModel?.neuter?.value = petInfoItem?.isNeutralized
                    foodRecommentDataModel?.matchFoodAllStep?.value = petInfoItem?.regInfoAllStep
                    foodRecommentDataModel?.petInfo?.value = petInfoItem

                    foodRecommentDataModel?.weight?.value = items[0].weight.toFloat()
                    foodRecommentDataModel?.weightState?.value = items[0].weightState
                    foodRecommentDataModel?.body?.value = getBodyType(items[0].bodyType)
                    foodRecommentDataModel?.bodyTypeState?.value = items[0].bodyTypeState
                    foodRecommentDataModel?.activate?.value = items[0].walkTime
                    foodRecommentDataModel?.activateState?.value = items[0].walkTimeState
                    foodRecommentDataModel?.allerge?.value = getAllergeFood(items[0].allergyFoodIdList)
                    foodRecommentDataModel?.allergeState?.value = items[0].allergyFoodState
                    foodRecommentDataModel?.likefood?.value = getLikeFood(items[0].favoriteFoodList)
                    foodRecommentDataModel?.likeFoodState?.value = items[0].favoriteFoodState
                    foodRecommentDataModel?.pregnantStatus?.value = items[0].pregnancy.toString()
                    foodRecommentDataModel?.pregnant?.value = getPregnant(items[0].pregnancyState)
                    foodRecommentDataModel?.disease?.value = getDisease(items[0].nutritionIdList)
                    foodRecommentDataModel?.sickness?.value = getDisease(items[0].nutritionIdList)
                    foodRecommentDataModel?.feeding?.value = getFeedType(items[0])
                    foodRecommentDataModel?.feedingFlag?.value = getFeedStatus(items[0])
                }
            }
        }
    }

    private fun calculateBirthday(item:PetInfoItem) : String {
        val year = item.birthday!!.split("-")[0]
        val month = item.birthday!!.split("-")[1]
        if (currentYear == year && currentMonth == month) {
            petInfoModel?.petYear!!.value = "0"
            petInfoModel?.petMonth!!.value = "0"

            return "0세 0개월"
        } else {
            if (currentMonth.toInt() >= month.toInt()) {
                if (currentMonth == month) {
                    petInfoModel?.petYear!!.value = "${currentYear.toInt() - year.toInt() - 1}"
                    petInfoModel?.petMonth!!.value = "${currentMonth.toInt()}"

                    return "${currentYear.toInt() - year.toInt() - 1}세 ${currentMonth.toInt()}개월"
                } else {
                    petInfoModel?.petYear!!.value = "${currentYear.toInt() - year.toInt()}"
                    petInfoModel?.petMonth!!.value = "${currentMonth.toInt() - month.toInt()}"

                    return "${currentYear.toInt() - year.toInt()}세 ${currentMonth.toInt() - month.toInt()}개월"
                }
            } else {
                val calMonth = if(currentMonth.toInt() > 9) {
                    currentMonth
                } else {
                    currentMonth.replace("0", "")
                }

                petInfoModel?.petYear!!.value = "${currentYear.toInt() - (year.toInt() + 1)}"
                petInfoModel?.petMonth!!.value = "${calMonth}"

                return "${currentYear.toInt() - (year.toInt() + 1)}세 ${calMonth}개월"
            }
        }
    }

    private fun getKindKey(kind: String) =
        when (kind) {
            "강아지" -> "dog"
            "고양이" -> "cat"
            "햄스터" -> "hamster"
            "토끼" -> "rabbit"
            "고슴도치" -> "hedgehog"
            "거북이" -> "turtle"
            "기니피그" -> "guinea_pig"
            "조류" -> "bird"
            "패릿" -> "ferret"
            else -> ""
        }

    private fun getBodyType(bodyType: Int) =
        when (bodyType) {
            10 -> FoodRecommentDataModel.PetBodyLevel.LEVEL1
            20 -> FoodRecommentDataModel.PetBodyLevel.LEVEL2
            30 -> FoodRecommentDataModel.PetBodyLevel.LEVEL3
            40 -> FoodRecommentDataModel.PetBodyLevel.LEVEL4
            50 -> FoodRecommentDataModel.PetBodyLevel.LEVEL5
            else -> FoodRecommentDataModel.PetBodyLevel.NONE
        }

    private fun getAllergeFood(items: List<Int>) : HashSet<String> {
        val allergeFood:HashSet<String> = HashSet()

        for (item in items) {
            when (item) {
                1 -> allergeFood.add("beef")
                2 -> allergeFood.add("lamb")
                3 -> allergeFood.add("duck_meet")
                4 -> allergeFood.add("chicken_meet")
                5 -> allergeFood.add("salmon_tuna")
                6 -> allergeFood.add("like_all")
                else -> ""
            }
        }

        return allergeFood
    }

    private fun getLikeFood(items: List<Int>) : HashSet<String> {
        val likeFood:HashSet<String> = HashSet()

        for (item in items) {
            when (item) {
                1 -> likeFood.add("beef")
                2 -> likeFood.add("lamb")
                3 -> likeFood.add("duck_meet")
                4 -> likeFood.add("chicken_meet")
                5 -> likeFood.add("salmon_tuna")
                6 -> likeFood.add("like_all")
                else -> ""
            }
        }

        return likeFood
    }

    private fun getDisease(items: List<Int>) : HashSet<String> {
        val disease:HashSet<String> = HashSet<String>().apply{ add("") }

        for (item in items) {
            when (item) {
                1 -> disease.add("strong")
                2 -> disease.add("joint")
                3 -> disease.add("skin")
                4 -> disease.add("digestion")
                5 -> disease.add("immunity")
                6 -> disease.add("stone_kidney")
                7 -> disease.add("obesity")
                else -> ""
            }
        }

        return disease
    }

    private fun getPregnant(state: Int) =
        when (state) {
            2 -> FoodRecommentDataModel.PetPregnantMode.NONE
            3 -> FoodRecommentDataModel.PetPregnantMode.PREGNANT
            4 -> FoodRecommentDataModel.PetPregnantMode.BREEDING
            else -> FoodRecommentDataModel.PetPregnantMode.NONE
        }

    private fun getFeedType(item:CustoMealInfoData) : HashMap<String, String> {
        val feeding: HashMap<String, String> = hashMapOf()
        if ("Y" == item.dryFoodYn) {
            feeding["dry"] = if (item.dryFoodName.isNotEmpty()) {
                item.dryFoodName
            } else {
                "건식사료"
            }
        }

        if ("Y" == item.wetFoodYn) {
            feeding["wet"] = if (item.wetFoodName.isNotEmpty()) {
                item.wetFoodName
            } else {
                "습식사료"
            }
        }

        if ("Y" == item.snackYn) {
            feeding["snack"] = if (item.snackName.isNotEmpty()) {
                item.snackName
            } else {
                "스낵(간식)"
            }
        }

        return feeding
    }

    private fun getFeedStatus(item: CustoMealInfoData): BooleanArray {
        val feedFlag = booleanArrayOf(false, false, false)
        if ("Y" == item.dryFoodYn) {
            feedFlag[0] = true
        }

        if ("Y" == item.wetFoodYn) {
            feedFlag[1] = true
        }

        if ("Y" == item.snackYn) {
            feedFlag[2] = true
        }

        return feedFlag
    }
}