package kr.co.petdoc.petdoc.fragment.mypage.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_pet_weight.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetWeightBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CustoMealInfoData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.ScrollRulerView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException

/**
 * petdoc-android
 * Class: MyPetWeightFragment
 * Created by sungminkim on 2020/04/09.
 *
 * Description : 마이페이지 -> 반려 동물 몸무게 입력 화면
 */
class MyPetWeightFragment : BaseFragment() {

    private val TAG = "MyPetWeightFragment"
    private val WEIGHT_REQUEST = "$TAG.weightRequest"
    private val CUSTOMEAL_INFO_REQUEST = "$TAG.custoMealInfoRequest"

    private var foodRecommentDataModel : FoodRecommentDataModel? = null

    private var weightValue = 0f

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        foodRecommentDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentPetWeightBinding>(inflater, R.layout.fragment_pet_weight, container, false)
        databinding.lifecycleOwner = requireActivity()
        databinding.foodreference = foodRecommentDataModel

        return databinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if( foodRecommentDataModel?.matchmode?.value == true ){
            petfood_recommend_weight_back_button.visibility = View.GONE
            mypage_title_text.visibility = View.GONE
            petfood_recomment_weight_save_button.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_몸무게", "Page View", "몸무게 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_몸무게_수정", "Page View", "몸무게 입력 단계 수정 페이지뷰")
        }

        petfood_recommend_weight_back_button.apply{ setOnClickListener { requireActivity().onBackPressed() } }

        petfood_recommend_ruler_area.apply{
            max_range = 99
            callback = object : ScrollRulerView.PositionChangeCallback{
                override fun positionCallback(value: Float) {
                    if (isAdded) {
                        weightValue = value
                        petfood_recommend_weight_input_text.text = String.format("%.1f", value)

                        if (value > 0f) {
                            petfood_recomment_weight_save_button.isEnabled = true
                            petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white))
                        } else {
                            petfood_recomment_weight_save_button.isEnabled = false
                            petfood_recomment_weight_save_button.setTextColor(Helper.readColorRes(R.color.white_alpha))
                        }
                    }
                }
            }
        }

        petfood_recommend_ruler_area.post{
            weightValue = foodRecommentDataModel?.weight?.value?:0f
            if (isAdded) {
                petfood_recommend_ruler_area.moveToWeight(foodRecommentDataModel?.weight?.value?:0f)
            }
        }

        petfood_recomment_weight_save_button.setOnClickListener {
            mApiClient.uploadWeight(
                WEIGHT_REQUEST,
                foodRecommentDataModel?.petId?.value!!,
                foodRecommentDataModel?.cmInfoId?.value!!,
                weightValue.roundTo(1)
            )
        }

        if (foodRecommentDataModel?.matchFoodAgain?.value == true) {
            mApiClient.getCustomealInfo(CUSTOMEAL_INFO_REQUEST, foodRecommentDataModel?.petId?.value!!)
        } else {
            petfood_recommend_weight_input_text.text = foodRecommentDataModel?.weight?.value.toString()
            petfood_recommend_ruler_area.moveToWeight(foodRecommentDataModel?.weight?.value?:0f)
        }

        petfood_recomment_weight_save_button.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(WEIGHT_REQUEST)) {
            mApiClient.cancelRequest(WEIGHT_REQUEST)
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
            WEIGHT_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommentDataModel?.weight?.value = weightValue

                        if(foodRecommentDataModel?.matchmode?.value == true) findNavController().navigate(MyPetWeightFragmentDirections.actionMyPetWeightFragment2ToMyPetBodyTypeFragment2())
                        else requireActivity().onBackPressed()
                    }
                }
            }

            CUSTOMEAL_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val items: MutableList<CustoMealInfoData> = Gson().fromJson(
                            event.response,
                            object : TypeToken<MutableList<CustoMealInfoData>>() {}.type
                    )

                    petfood_recommend_weight_input_text.text = items[0].weight.toString()
                    petfood_recommend_ruler_area.moveToWeight(items[0].weight.toFloat())
                }
            }
        }
    }

    @Throws(NumberFormatException::class)
    private fun Float.roundTo(n: Int) = "%.${n}f".format(this).toFloat()
}