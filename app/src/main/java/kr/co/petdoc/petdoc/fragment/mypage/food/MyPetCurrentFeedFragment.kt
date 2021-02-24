package kr.co.petdoc.petdoc.fragment.mypage.food

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_current_feeding.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentCurrentFeedingBinding
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * petdoc-android
 * Class: MyPetCurrentFeedFragment
 * Created by sungminkim on 2020/04/13.
 *
 * Description : 맞춤식 진단 정보 - 펫 푸드 설정
 */
class MyPetCurrentFeedFragment : BaseFragment(){

    private val TAG = "MyPetCurrentFeedFragment"
    private val CURRENT_FEED_REQUEST = "$TAG.currentFeedRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null
    private var feedingObserver : Observer<HashMap<String,String>>? = null
    private var feedingFlagObserver : Observer<BooleanArray>? = null

    private var foodSet : HashMap<String, String>? = null

    private lateinit var screenSizeListener : ViewTreeObserver.OnGlobalLayoutListener
    private val screenChangeRect = Rect()
    private var availableHeight = 0

    private var dryFoodYn = "N"
    private var wetFoodYn = "N"
    private var snackYn = "N"
    private var dryFoodName = ""
    private var wetFoodName = ""
    private var snackName = ""

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)
        foodSet = foodRecommendDataModel?.feeding?.value?.clone() as HashMap<String, String>

        val databinding = DataBindingUtil.inflate<FragmentCurrentFeedingBinding>(inflater, R.layout.fragment_current_feeding, container, false)
            databinding.lifecycleOwner = requireActivity()
            databinding.foodreference = foodRecommendDataModel

        if(foodRecommendDataModel?.matchmode?.value == true){
            databinding.petfoodRecommendCurrentFeedingBackButton.visibility = View.GONE
            databinding.mypageTitleText.visibility = View.GONE
            databinding.petfoodRecommentFeedingSaveButton.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_급여사료", "Page View", "현재급여사료 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_급여사료_수정", "Page View", "현재급여사료 입력 단계 수정 페이지뷰")
        }

        availableHeight = (Helper.screenSize(requireActivity())[1] - Helper.getStatusBarHeight(requireActivity()) - Helper.convertDPResourceToPx(requireContext(), R.dimen.login_close_button_size)) / 100 * 85

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommendDataModel?.matchmode?.value != true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        feedingObserver = Observer<HashMap<String, String>> {
            updateFeedingUI()
        }
        foodRecommendDataModel?.feeding?.observe(viewLifecycleOwner, feedingObserver!!)

        feedingFlagObserver = Observer { updateFeedingUI() }
        foodRecommendDataModel?.feedingFlag?.observe(viewLifecycleOwner, feedingFlagObserver!!)


        screenSizeListener = ViewTreeObserver.OnGlobalLayoutListener {
            current_food_canvas.getWindowVisibleDisplayFrame(screenChangeRect)
            petfood_recomment_feeding_save_button.apply{
                visibility = if(screenChangeRect.bottom - screenChangeRect.top < availableHeight) View.GONE else View.VISIBLE
            }
        }


        petfood_recommend_current_feeding_back_button.setOnSingleClickListener { requireActivity().onBackPressed() }
        petfood_recomment_feeding_save_button.setOnSingleClickListener {
            // -------------------------------------------------------------------------------------
            if(foodSet!=null && food_recommend_dry_food_text.text.toString().isNullOrBlank()) foodSet?.remove("dry")
            else foodSet!!["dry"] = food_recommend_dry_food_text.text.toString()

            if(foodSet!=null && food_recommend_wet_food_text.text.toString().isNullOrBlank()) foodSet?.remove("wet")
            else foodSet!!["wet"] = food_recommend_wet_food_text.text.toString()

            if(foodSet!=null && food_recommend_snack_text.text.toString().isNullOrBlank()) foodSet?.remove("snack")
            else foodSet!!["snack"] = food_recommend_snack_text.text.toString()
            // -------------------------------------------------------------------------------------

            if(foodSet?.contains("dry")==true){
                dryFoodName = foodSet!!["dry"].toString()
            }

            if(foodSet?.contains("wet")==true){
                wetFoodName = foodSet!!["wet"].toString()
            }

            if(foodSet?.contains("snack")==true){
                snackName = foodSet!!["snack"].toString()
            }

            mApiClient.uploadCurrentFeed(
                CURRENT_FEED_REQUEST,
                foodRecommendDataModel?.petId?.value!!,
                foodRecommendDataModel?.cmInfoId?.value!!,
                dryFoodYn,
                wetFoodYn,
                snackYn,
                dryFoodName,
                wetFoodName,
                snackName
            )
        }

        petfood_recommend_tab_dry_food_image.setOnSingleClickListener {
            foodRecommendDataModel?.feedingFlag?.value = foodRecommendDataModel?.feedingFlag?.value?.apply{
                this[0] = !this[0]

                dryFoodYn = if (this[0]) {
                    "Y"
                } else {
                    "N"
                }
            }

            layoutFoodDry.visibility = View.VISIBLE
        }

        petfood_recommend_wet_food_image.setOnSingleClickListener {
            foodRecommendDataModel?.feedingFlag?.value = foodRecommendDataModel?.feedingFlag?.value?.apply{
                this[1] = !this[1]

                wetFoodYn = if (this[1]) {
                    "Y"
                } else {
                    "N"
                }
            }

            layoutFoodWet.visibility = View.VISIBLE
        }

        petfood_recommend_tab_snack_image.setOnSingleClickListener {
            foodRecommendDataModel?.feedingFlag?.value = foodRecommendDataModel?.feedingFlag?.value?.apply{
                this[2] = !this[2]

                snackYn = if (this[2]) {
                    "Y"
                } else {
                    "N"
                }
            }

            layoutFoodSnack.visibility = View.VISIBLE
        }

        food_recommend_dry_food_text_cancel.setOnSingleClickListener {
            foodSet?.remove("dry")
            food_recommend_dry_food_text.setText("")
        }

        food_recommend_dry_food_text.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               if(s.isNullOrBlank()){
                   food_recommend_dry_food_text_cancel.visibility = View.GONE
               }else{
                   food_recommend_dry_food_text_cancel.visibility = View.VISIBLE
               }
            }
        })

        food_recommend_wet_food_text_cancel.setOnSingleClickListener {
            foodSet?.remove("wet")
            food_recommend_wet_food_text.setText("")
        }

        food_recommend_wet_food_text.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.isNullOrBlank()){
                    food_recommend_wet_food_text_cancel.visibility = View.GONE
                }else{
                    food_recommend_wet_food_text_cancel.visibility = View.VISIBLE
                }
            }
        })



        food_recommend_snack_text_cancel.setOnSingleClickListener {
            foodSet?.remove("snack")
            food_recommend_snack_text.setText("")
        }

        food_recommend_snack_text.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.isNullOrBlank()){
                    food_recommend_snack_text_cancel.visibility = View.GONE
                }else{
                    food_recommend_snack_text_cancel.visibility = View.VISIBLE
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        current_food_canvas.viewTreeObserver.addOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onPause() {
        super.onPause()
        current_food_canvas.viewTreeObserver.removeOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        foodRecommendDataModel?.feeding?.removeObserver(feedingObserver!!)
        foodRecommendDataModel?.feedingFlag?.removeObserver(feedingFlagObserver!!)
    }

    private fun updateFeedingUI(){
        foodRecommendDataModel?.feedingFlag?.value?.let{

            petfood_recommend_tab_dry_food_image.setImageResource(R.drawable.dried_food_icon_off)
            petfood_recommend_tab_dry_food_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
            petfood_recommend_tab_dry_food_text.setTypeface(null, Typeface.NORMAL)
            petfood_recommend_wet_food_image.setImageResource(R.drawable.wet_food_icon_off)
            petfood_recommend_wet_dry_food_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
            petfood_recommend_wet_dry_food_text.setTypeface(null, Typeface.NORMAL)
            petfood_recommend_tab_snack_image.setImageResource(R.drawable.snack_icon_off)
            petfood_recommend_tab_snack_text.setTextColor(Helper.readColorRes(R.color.reddishgrey))
            petfood_recommend_tab_snack_text.setTypeface(null, Typeface.NORMAL)

            food_recommend_dry_food_guide.visibility = View.GONE
            food_option_1.visibility = View.GONE
            food_recommend_dry_food_text.visibility = View.GONE

            food_recommend_wet_food_guide.visibility = View.GONE
            food_option_2.visibility = View.GONE
            food_recommend_wet_food_text.visibility = View.GONE

            food_recommend_snack_guide.visibility = View.GONE
            food_option_3.visibility = View.GONE
            food_recommend_snack_text.visibility = View.GONE

            if(it[0]){
                petfood_recommend_tab_dry_food_image.setImageResource(R.drawable.dried_food_icon)
                petfood_recommend_tab_dry_food_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
                petfood_recommend_tab_dry_food_text.setTypeface(null, Typeface.BOLD)

                food_recommend_dry_food_guide.visibility = View.VISIBLE
                food_option_1.visibility = View.VISIBLE
                food_recommend_dry_food_text.visibility = View.VISIBLE
                layoutFoodDry.visibility = View.VISIBLE
            }
            if(it[1]){
                petfood_recommend_wet_food_image.setImageResource(R.drawable.wet_food_icon)
                petfood_recommend_wet_dry_food_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
                petfood_recommend_wet_dry_food_text.setTypeface(null, Typeface.BOLD)

                food_recommend_wet_food_guide.visibility = View.VISIBLE
                food_option_2.visibility = View.VISIBLE
                food_recommend_wet_food_text.visibility = View.VISIBLE
                layoutFoodWet.visibility = View.VISIBLE
            }
            if(it[2]){
                petfood_recommend_tab_snack_image.setImageResource(R.drawable.snack_icon)
                petfood_recommend_tab_snack_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
                petfood_recommend_tab_snack_text.setTypeface(null, Typeface.BOLD)

                food_recommend_snack_guide.visibility = View.VISIBLE
                food_option_3.visibility = View.VISIBLE
                food_recommend_snack_text.visibility = View.VISIBLE
                layoutFoodSnack.visibility = View.VISIBLE
            }
        }


        if(foodSet?.contains("dry")==true){
            food_recommend_dry_food_text.setText(foodSet!!["dry"])
        }

        if(foodSet?.contains("wet")==true){
            food_recommend_wet_food_text.setText(foodSet!!["wet"])
        }

        if(foodSet?.contains("snack")==true){
            food_recommend_snack_text.setText(foodSet!!["snack"])
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
            CURRENT_FEED_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommendDataModel?.feeding?.value = foodSet?.clone() as HashMap<String, String>

                        if(foodRecommendDataModel?.matchmode?.value == true) {
                            if (foodRecommendDataModel?.gender?.value.toString() == "man" || foodRecommendDataModel?.neuter?.value == true) {
                                findNavController().navigate(MyPetCurrentFeedFragmentDirections.actionMyPetCurrentFeedFragment2ToPetFoodRecommedProgressingFragment())
                            } else {
                                findNavController().navigate(MyPetCurrentFeedFragmentDirections.actionMyPetCurrentFeedFragment2ToMyPetPregnantFragment2())
                            }
                        }
                        else requireActivity().onBackPressed()
                    }
                }
            }
        }
    }
}