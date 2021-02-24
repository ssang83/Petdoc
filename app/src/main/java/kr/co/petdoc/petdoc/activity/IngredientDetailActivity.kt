package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_ingredient_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.FeedInfoData
import kr.co.petdoc.petdoc.network.response.submodel.NutritionData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: IngredientDetailActivity
 * Created by kimjoonsung on 2020/07/16.
 *
 * Description :
 */
class IngredientDetailActivity : BaseActivity() {

    private val TAG = "IngredientDetailActivity"
    private val CUSTOMEAL_FOOD_INFO_REQUEST = "$TAG.customealFoodInfoRequest"
    private val NUTRITION_1_INFO_REQUEST = "$TAG.nutrition1InfoRequest"
    private val NUTRITION_2_INFO_REQUEST = "$TAG.nutrition2InfoRequest"

    private var feedData: FeedInfoData? = null
    private var nutritionData1: NutritionData? = null
    private var nutritionData2: NutritionData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha=0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_detail)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        btnBack.setOnClickListener { finish() }

        val foodId = intent?.getIntExtra("foodId", -1) ?: -1
        val pnId1 = intent?.getIntExtra("pnId1", -1) ?: -1
        val pnId2 = intent?.getIntExtra("pnId2", -1) ?: -1
        val type = intent?.getStringExtra("type") ?: ""
        val order = intent?.getIntExtra("order", -1)

        if (type == "food") {
            mApiClient.getFeedInfo(CUSTOMEAL_FOOD_INFO_REQUEST, foodId)
        } else {
            if (order == 1) {
                mApiClient.getNutritionInfo(NUTRITION_1_INFO_REQUEST, pnId1)
            } else {
                mApiClient.getNutritionInfo(NUTRITION_2_INFO_REQUEST, pnId2)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(CUSTOMEAL_FOOD_INFO_REQUEST)) {
            mApiClient.cancelRequest(CUSTOMEAL_FOOD_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(NUTRITION_1_INFO_REQUEST)) {
            mApiClient.cancelRequest(NUTRITION_1_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(NUTRITION_2_INFO_REQUEST)) {
            mApiClient.cancelRequest(NUTRITION_2_INFO_REQUEST)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
            CUSTOMEAL_FOOD_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    feedData = Gson().fromJson(event.response, FeedInfoData::class.java)

                    ingredientTitle.text = feedData?.name!!.replace("브이랩", "")

                    ingredients.text = feedData?.ingredients
                    protein.text = feedData?.protein
                    fat.text = feedData?.fat
                    ash.text = feedData?.ash
                    fiber.text = feedData?.fiber
                    phosphorus.text = feedData?.phosphorus
                    calcium.text = feedData?.calcium
                    water.text = feedData?.water
                }
            }

            NUTRITION_1_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    nutritionData1 = Gson().fromJson(event.response, NutritionData::class.java)

                    ingredientTitle.text = nutritionData1?.name!!.replace("브이랩", "")

                    ingredients.text = nutritionData1?.ingredients
                    protein.text = nutritionData1?.protein
                    fat.text = nutritionData1?.fat
                    ash.text = nutritionData1?.ash
                    fiber.text = nutritionData1?.fiber
                    phosphorus.text = nutritionData1?.phosphorus
                    calcium.text = nutritionData1?.calcium
                    water.text = nutritionData1?.water
                }
            }

            NUTRITION_2_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    nutritionData2 = Gson().fromJson(event.response, NutritionData::class.java)

                    ingredientTitle.text = nutritionData2?.name!!.replace("브이랩", "")

                    ingredients.text = nutritionData2?.ingredients
                    protein.text = nutritionData2?.protein
                    fat.text = nutritionData2?.fat
                    ash.text = nutritionData2?.ash
                    fiber.text = nutritionData2?.fiber
                    phosphorus.text = nutritionData2?.phosphorus
                    calcium.text = nutritionData2?.calcium
                    water.text = nutritionData2?.water
                }
            }
        }
    }
}