package kr.co.petdoc.petdoc.fragment.mypage.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_pet_pregnant.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetPregnantBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import kr.co.petdoc.petdoc.widget.NothingSelectedSpinnerAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MyPetPregnantFragment : BaseFragment() {

    private val TAG = "MyPetPregnantFragment"
    private val PREGNANT_STATUS_REQUEST = "$TAG.pregnantStatusRequest"

    private var foodRecommendDataModel: FoodRecommentDataModel? = null
    private var pregnantObserver: Observer<FoodRecommentDataModel.PetPregnantMode>? = null

    private var spinnerData: MutableList<String> = mutableListOf()
    private var pregnantStatus = ""
    private var pregnantValue = ""
    private var pregnantState = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel =
            ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val dataBinding = DataBindingUtil.inflate<FragmentPetPregnantBinding>(
            inflater,
            R.layout.fragment_pet_pregnant,
            container,
            false
        )
        dataBinding.lifecycleOwner = requireActivity()
        dataBinding.foodreference = foodRecommendDataModel


        if (foodRecommendDataModel?.matchmode?.value == true) {
            dataBinding.petfoodRecommendPregnantBackButton.visibility = View.GONE
            dataBinding.mypageTitleText.visibility = View.GONE

            dataBinding.petfoodRecommentPregnantButton.apply {
                text = Helper.readStringRes(R.string.food_search_done)
                setBackgroundResource(R.drawable.salmon_solid_round_rect_6)
            }

            FirebaseAPI(requireActivity()).logEventFirebase("진단_임신여부", "Page View", "임신여부 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_임신여부_수정", "Page View", "임신여부 입력 단계 수정 페이지뷰")
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        spinnerData.add("1주")
        spinnerData.add("2주")
        spinnerData.add("3주")
        spinnerData.add("4주")
        spinnerData.add("5주")
        spinnerData.add("6주")
        spinnerData.add("7주")
        spinnerData.add("8주")
        spinnerData.add("9주")
        spinnerData.add("10주")

        pregnantObserver = Observer {
            when (it) {
                FoodRecommentDataModel.PetPregnantMode.NONE -> {
                    petfood_recommend_pregnant_radio_group.check(R.id.petfood_recommend_pregnant_radio_1)
                    pregnantState = 2
                }
                FoodRecommentDataModel.PetPregnantMode.PREGNANT -> {
                    petfood_recommend_pregnant_radio_group.check(R.id.petfood_recommend_pregnant_radio_2)
                    petfood_recommend_pregnant_radio_2.setTextColor(Helper.readColorRes(R.color.dark_grey))
                    layoutPregnantStatus.visibility = View.VISIBLE
                    pregnantState = 3
                }
                FoodRecommentDataModel.PetPregnantMode.BREEDING -> {
                    petfood_recommend_pregnant_radio_group.check(R.id.petfood_recommend_pregnant_radio_3)
                    petfood_recommend_pregnant_radio_3.setTextColor(Helper.readColorRes(R.color.dark_grey))
                    layoutPregnantStatus.visibility = View.GONE
                    pregnantState = 4
                }
            }
        }
        foodRecommendDataModel?.pregnant?.observe(viewLifecycleOwner, pregnantObserver!!)


        petfood_recommend_pregnant_back_button.setOnClickListener { requireActivity().onBackPressed() }
        petfood_recomment_pregnant_button.setOnClickListener {
            mApiClient.uploadPregnantStatus(
                PREGNANT_STATUS_REQUEST,
                foodRecommendDataModel?.petId?.value!!,
                foodRecommendDataModel?.cmInfoId?.value!!,
                pregnantValue,
                pregnantState
            )
        }

        //==========================================================================================

        petfood_recommend_pregnant_radio_group.setOnCheckedChangeListener { group, checkedId ->
            run {
                when (checkedId) {
                    R.id.petfood_recommend_pregnant_radio_1 -> {
                        petfood_recommend_pregnant_radio_1.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        layoutPregnantStatus.visibility = View.GONE
                    }

                    R.id.petfood_recommend_pregnant_radio_2 -> {
                        petfood_recommend_pregnant_radio_2.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        layoutPregnantStatus.visibility = View.VISIBLE
                    }

                    R.id.petfood_recommend_pregnant_radio_3 -> {
                        petfood_recommend_pregnant_radio_3.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        layoutPregnantStatus.visibility = View.GONE
                    }
                }
            }
        }

        //===========================================================================================
        pregnantSpinner.onItemSelectedListener = spinnerItemListener
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, spinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pregnantSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_customeal_pregnant, requireActivity())

        pregnantSpinner.prompt = foodRecommendDataModel?.pregnantStatus?.value.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        foodRecommendDataModel?.pregnant?.removeObserver(pregnantObserver!!)
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
            PREGNANT_STATUS_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommendDataModel?.pregnant?.removeObserver(pregnantObserver!!)

                        foodRecommendDataModel?.pregnantStatus?.value = pregnantStatus
                        foodRecommendDataModel?.pregnant?.value =
                            when (petfood_recommend_pregnant_radio_group.checkedRadioButtonId) {
                                R.id.petfood_recommend_pregnant_radio_1 -> FoodRecommentDataModel.PetPregnantMode.NONE
                                R.id.petfood_recommend_pregnant_radio_2 -> FoodRecommentDataModel.PetPregnantMode.PREGNANT
                                R.id.petfood_recommend_pregnant_radio_3 -> FoodRecommentDataModel.PetPregnantMode.BREEDING
                                else -> FoodRecommentDataModel.PetPregnantMode.NONE
                            }

                        if (foodRecommendDataModel?.matchmode?.value == true) {
                            // 맞춤식 결과 계산 화면
                            findNavController().navigate(MyPetPregnantFragmentDirections.actionMatchfoodToResultProgressing())
                        } else {
                            // 일화성 페이지 화면 나가기
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }

    //==============================================================================================
    private val spinnerItemListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            Logger.d("onNothingSelected")
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            pregnantType.visibility = View.GONE
            pregnantStatus = parent?.getItemAtPosition(position).toString()
            pregnantValue = parent?.getItemAtPosition(position).toString().replace("주", "")
        }
    }
}