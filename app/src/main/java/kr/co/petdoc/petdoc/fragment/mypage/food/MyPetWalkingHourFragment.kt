package kr.co.petdoc.petdoc.fragment.mypage.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_walking_time.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetWalkingTimeBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.regex.Pattern


/**
 * petdoc-android
 * Class: MyPetWalkingHourFragment
 * Created by sungminkim on 2020/04/13.
 *
 * Description : 맞춤식 진단 중, 산책시간 입력
 */
class MyPetWalkingHourFragment : BaseFragment() {

    private val TAG = "MyPetWalkingHourFragment"
    private val WALKING_REQUEST = "$TAG.walkingRequest"

    private var foodRecommentDataModel : FoodRecommentDataModel? = null
    private var walkingTimeObserver : Observer<Int>? = null

    private val onlyNumberPattern = Pattern.compile("[0-9]{1,5}")

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommentDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentPetWalkingTimeBinding>(inflater, R.layout.fragment_pet_walking_time, container, false)
            databinding.lifecycleOwner = requireActivity()
            databinding.foodreference = foodRecommentDataModel

        if(foodRecommentDataModel?.matchmode?.value == true){
            databinding.petfoodRecommendWalkingBackButton.visibility = View.GONE
            databinding.mypageTitleText.visibility = View.GONE

            databinding.petfoodRecommentWalkingSaveButton.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_산책", "Page View", "산책 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_산책_수정", "Page View", "산책 입력 단계 수정 페이지뷰")
        }

        return databinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommentDataModel?.matchmode?.value != true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        walkingTimeObserver = Observer { walkingHour ->
            updateWalkingTime()
        }
        foodRecommentDataModel?.activate?.observe(viewLifecycleOwner, walkingTimeObserver!!)

        petfood_recomment_walking_time_decrease.setOnClickListener {
            var current = foodRecommentDataModel?.activate?.value?:0
            foodRecommentDataModel?.activate?.value = if(current-1<=0) 0 else current-1
        }

        petfood_recomment_walking_time_increase.setOnClickListener {
            var current = foodRecommentDataModel?.activate?.value?:0
            foodRecommentDataModel?.activate?.value = current + 1
        }

        petfood_recommend_walking_back_button.apply{
            setOnClickListener { requireActivity().onBackPressed() }
        }

        petfood_recomment_walking_save_button.setOnClickListener {

            val inputValue = petfood_recommend_walking_input_text.text.toString()

            if( onlyNumberPattern.matcher(inputValue).matches() ){
                val parsed = inputValue.toIntOrNull()
                if(parsed!=null){
                    Helper.hideKeyboard(requireContext(), petfood_recomment_walking_save_button)
                    foodRecommentDataModel?.activate?.value = inputValue.toInt()

                    mApiClient.uploadWalkTime(
                        WALKING_REQUEST,
                        foodRecommentDataModel?.petId?.value!!,
                        foodRecommentDataModel?.cmInfoId?.value!!,
                        inputValue
                    )
                }
            }else{
                AppToast(requireContext()).showToastMessage("NUMBER PATTERN ERROR")
                updateWalkingTime()
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        foodRecommentDataModel?.activate?.removeObserver(walkingTimeObserver!!)

        if (mApiClient.isRequestRunning(WALKING_REQUEST)) {
            mApiClient.cancelRequest(WALKING_REQUEST)
        }
    }

    private fun updateWalkingTime(){
        petfood_recommend_walking_input_text.text = "${foodRecommentDataModel?.activate?.value}"
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
            WALKING_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        if(foodRecommentDataModel?.matchmode?.value == true) findNavController().navigate(MyPetWalkingHourFragmentDirections.actionMatchfoodWalkingtimeToLikefood())
                        else requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}
