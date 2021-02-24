package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_match_food.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.event.PetInfoRefreshEvent
import kr.co.petdoc.petdoc.fragment.mypage.food.*
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CustoMealInfoData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.resetMyPetsDirty
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MatchFoodActivity : BaseActivity() {

    private val TAG = "MatchFoodActivity"
    private val CUSTOMEAL_INFO_REQUEST = "$TAG.custoMealInfoRequest"

    var foodRecommentDataModel : FoodRecommentDataModel? = null
    var petInfoItem: PetInfoItem? = null

    private var matchFoodAgain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        foodRecommentDataModel = ViewModelProvider(this).get(FoodRecommentDataModel::class.java)

        petInfoItem = intent?.getParcelableExtra("item")
        matchFoodAgain = intent?.getBooleanExtra("matchFoodAgain", matchFoodAgain) ?: matchFoodAgain

        setContentView(R.layout.activity_match_food)

        match_food_activity_canvas.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        foodRecommentDataModel?.matchmode?.value = true
        foodRecommentDataModel?.petname?.value = petInfoItem?.name
        foodRecommentDataModel?.petimage?.value = petInfoItem?.imageUrl
        foodRecommentDataModel?.cmInfoId?.value = petInfoItem?.cmInfoId
        foodRecommentDataModel?.petId?.value = petInfoItem?.id
        foodRecommentDataModel?.species?.value = petInfoItem?.species
        foodRecommentDataModel?.gender?.value = petInfoItem?.gender
        foodRecommentDataModel?.neuter?.value = petInfoItem?.isNeutralized
        foodRecommentDataModel?.petInfo?.value = petInfoItem
        foodRecommentDataModel?.matchFoodAllStep?.value = petInfoItem?.regInfoAllStep
        foodRecommentDataModel?.matchFoodAgain?.value = matchFoodAgain

        findNavController(R.id.nav_match_food_fragment)?.let{
            it.addOnDestinationChangedListener { controller, destination, arguments ->

                petfood_recommend_weight_back_button.visibility = View.VISIBLE
                mypage_title_text.visibility = View.VISIBLE
                stepper.visibility = View.VISIBLE
                match_food_activity_canvas.setBackgroundResource(R.color.colorpetfood)

                if(destination.id == R.id.petFoodRecommedProgressingFragment){
                    petfood_recommend_weight_back_button.visibility = View.GONE
                    mypage_title_text.visibility = View.GONE
                    stepper.visibility = View.GONE
                }
                else if(destination.id == R.id.myPetFoodRecommendResult2){
                    match_food_activity_canvas.setBackgroundResource(R.color.white)
                    stepper.visibility = View.GONE
                }
                else{
                    petfood_recommend_weight_back_button.setImageResource(R.drawable.backbutton)
                    mypage_title_text.text = Helper.readStringRes(R.string.pet_info_food_research)

                    when (destination.id) {
                        R.id.myPetWeightFragment2 -> {
                            stepProgressView(1, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetBodyTypeFragment2 -> {
                            stepProgressView(2, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetWalkingHourFragment2 -> {
                            stepProgressView(3, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetLikeFoodFragment2 -> {
                            stepProgressView(4, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetAllergeFoodFragment2 -> {
                            stepProgressView(5, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetDetailDiagnosisFragment -> {
                            stepProgressView(6, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetCurrentFeedFragment2 -> {
                            stepProgressView(7, petInfoItem?.regInfoAllStep!!)
                        }

                        R.id.myPetPregnantFragment2 -> {
                            stepProgressView(8, petInfoItem?.regInfoAllStep!!)
                        }
                    }
                }
            }
        }

        petfood_recommend_weight_back_button.setOnClickListener { onBackPressed() }

        if (petInfoItem?.regInfoStep != 0) {
            mApiClient.getCustomealInfo(CUSTOMEAL_INFO_REQUEST, petInfoItem?.id!!)
        }

        resetMyPetsDirty()
        EventBus.getDefault().post(PetInfoRefreshEvent())
    }


    override fun onBackPressed() {
        findNavController(R.id.nav_match_food_fragment)?.let{
            if(it.currentDestination?.id == R.id.myPetFoodRecommendResult2) {
                finish()
                return
            }

            if (it.currentDestination?.id == R.id.myPetDetailDiagnosisFragment) {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_match_food_fragment)
                val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0) as? MyPetDetailDiagnosisFragment
                if (fragment?.checkStepBack()!!) {
                    Logger.d("step back")
                    super.onBackPressed()
                    return
                } else {
                    Logger.d("question back")
                    return
                }
            }

        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CUSTOMEAL_INFO_REQUEST)) {
            mApiClient.cancelRequest(CUSTOMEAL_INFO_REQUEST)
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
            CUSTOMEAL_INFO_REQUEST -> {
                val items: MutableList<CustoMealInfoData> = Gson().fromJson(
                    event.response,
                    object : TypeToken<MutableList<CustoMealInfoData>>() {}.type
                )

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

                if (!matchFoodAgain) {
                    moveToStep(petInfoItem?.regInfoStep!!)
                }
            }
        }
    }

    private fun stepProgressView(step:Int, allStep:Int) {
        (stepProgress.layoutParams as ConstraintLayout.LayoutParams).matchConstraintPercentWidth =
            step.toFloat() / allStep.toFloat()

        stepProgress.requestLayout()
    }

    private fun moveToStep(step: Int) {
        when (step) {
            7 -> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetLikeFoodFragmentDirections.actionMatchfoodLikefoodToAllergefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetAllergeFoodFragmentDirections.actionMyPetAllergeFoodFragment2ToMyPetDetailDiagnosisFragment()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetDetailDiagnosisFragmentDirections.actionMyPetDetailDiagnosisFragmentToMyPetCurrentFeedFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetCurrentFeedFragmentDirections.actionMyPetCurrentFeedFragment2ToMyPetPregnantFragment2()
                )
            }

            6-> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetLikeFoodFragmentDirections.actionMatchfoodLikefoodToAllergefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetAllergeFoodFragmentDirections.actionMyPetAllergeFoodFragment2ToMyPetDetailDiagnosisFragment()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetDetailDiagnosisFragmentDirections.actionMyPetDetailDiagnosisFragmentToMyPetCurrentFeedFragment2()
                )
            }

            5-> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetLikeFoodFragmentDirections.actionMatchfoodLikefoodToAllergefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetAllergeFoodFragmentDirections.actionMyPetAllergeFoodFragment2ToMyPetDetailDiagnosisFragment()
                )
            }

            4 -> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetLikeFoodFragmentDirections.actionMatchfoodLikefoodToAllergefood()
                )
            }

            3 -> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood()
                )
            }

            2 -> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetBodyTypeFragmentDirections.actionMyPetBodyTypeFragment2ToMyPetWalkingHourFragment2()
                )
            }

            1 -> {
                findNavController(R.id.nav_match_food_fragment).navigate(
                    MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2()
                )
            }
        }
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

    private fun getFeedType(item: CustoMealInfoData) : HashMap<String, String> {
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