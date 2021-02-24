package kr.co.petdoc.petdoc.fragment.mypage.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_pet_detail_diagnosis_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_detail_diagnosis.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentPetDetailDiagnosisBinding
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.DetailDiagnosisListResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.DiagnosisData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.FoodRecommentDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: MyPetDetailDiagnosisFragment
 * Created by kimjoonsung on 2020/07/13.
 *
 * Description :
 */
class MyPetDetailDiagnosisFragment : BaseFragment() {

    private val TAG = "MyPetDetailDiagnosisFragment"
    private val DETAIL_DIAGNOSIS_REQUEST = "$TAG.detailDiagnosisRequest"
    private val DETAIL_DIAGNOSIS_UPLOAD_REQUEST = "$TAG.detailDiagnosisUploadRequest"

    private var foodRecommendDataModel : FoodRecommentDataModel? = null

    private var dataMap:Map<Int, List<DiagnosisData>> = mapOf()
    private var diagnosisItems:List<DiagnosisData> = listOf()
    private lateinit var mAdapter:DiagnosisAdapter

    private var margin29 = 0
    private var margin67 = 0
    private var page = 1

    private var questionId:HashSet<Int> = hashSetOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        foodRecommendDataModel = ViewModelProvider(requireActivity()).get(FoodRecommentDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentPetDetailDiagnosisBinding>(inflater, R.layout.fragment_pet_detail_diagnosis, container, false)
        databinding.lifecycleOwner = requireActivity()
        databinding.foodreference = foodRecommendDataModel

        if(foodRecommendDataModel?.matchmode?.value == true){
            databinding.btnBack.visibility = View.GONE
            databinding.mypageTitleText.visibility = View.GONE
            databinding.petDetailDiagnosisSaveButton.text = Helper.readStringRes(R.string.pet_add_next)

            FirebaseAPI(requireActivity()).logEventFirebase("진단_정밀진단", "Page View", "정밀진단 입력 단계 페이지뷰")
        } else {
            FirebaseAPI(requireActivity()).logEventFirebase("진단_정밀진단_수정", "Page View", "정밀진단 입력 단계 수정 페이지뷰")
        }

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(foodRecommendDataModel?.matchmode?.value != true) view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        margin29 = Helper.convertDpToPx(requireContext(), 29f).toInt()
        margin67 = Helper.convertDpToPx(requireContext(), 67f).toInt()

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        pet_detail_diagnosis_save_button.setOnClickListener {
            if (foodRecommendDataModel?.diagnosisPage?.value == foodRecommendDataModel?.diagnosisTotalPage?.value) {
                mApiClient.uploadDetailDiagnosis(
                    DETAIL_DIAGNOSIS_UPLOAD_REQUEST,
                    foodRecommendDataModel?.petId?.value!!,
                    foodRecommendDataModel?.cmInfoId?.value!!,
                    questionId
                )
            } else {
                foodRecommendDataModel?.diagnosisPage?.value = page.inc()
                page = page.inc()

                pet_detail_diagnosis_guide.text = foodRecommendDataModel?.detailDiagnosisGuideName()

                diagnosisItems.toMutableList().clear()
                diagnosisItems = if (dataMap.containsKey(page)) {
                    dataMap[page]!!
                } else {
                    listOf()
                }

                mAdapter.notifyDataSetChanged()
            }
        }

        mAdapter = DiagnosisAdapter()
        diagnosisList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mApiClient.getDetailDiagnosisList(
            DETAIL_DIAGNOSIS_REQUEST,
            foodRecommendDataModel?.petId?.value!!,
            foodRecommendDataModel?.cmInfoId?.value!!
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(DETAIL_DIAGNOSIS_REQUEST)) {
            mApiClient.cancelRequest(DETAIL_DIAGNOSIS_REQUEST)
        }

        if (mApiClient.isRequestRunning(DETAIL_DIAGNOSIS_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(DETAIL_DIAGNOSIS_UPLOAD_REQUEST)
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            DETAIL_DIAGNOSIS_REQUEST -> {
                if (response is DetailDiagnosisListResponse) {
                    if ("SUCCESS" == response.code) {
                        foodRecommendDataModel?.diagnosisTotalPage?.value = response.resultData.totPage
                        foodRecommendDataModel?.diagnosisPage?.value = page
                        pet_detail_diagnosis_guide.text = foodRecommendDataModel?.detailDiagnosisGuideName()

                        dataMap = response.resultData.questions.groupBy { it.page }
                        diagnosisItems = if (dataMap.containsKey(page)) {
                            dataMap[page]!!.filter { it.displayYn == "y" }
                        } else {
                            listOf()
                        }

                        mAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            DETAIL_DIAGNOSIS_UPLOAD_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        foodRecommendDataModel?.questionId?.value = questionId

                        if(foodRecommendDataModel?.matchmode?.value == true) {
                            findNavController().navigate(MyPetDetailDiagnosisFragmentDirections.actionMyPetDetailDiagnosisFragmentToMyPetCurrentFeedFragment2())
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                }
            }
        }
    }

    fun checkStepBack() : Boolean {
        if(foodRecommendDataModel?.diagnosisPage?.value == 1) {
            return true
        } else {
            foodRecommendDataModel?.diagnosisPage?.value = page.dec()
            page = page.dec()

            pet_detail_diagnosis_guide.text = foodRecommendDataModel?.detailDiagnosisGuideName()

            diagnosisItems.toMutableList().clear()
            diagnosisItems = if (dataMap.containsKey(page)) {
                dataMap[page]!!.filter { it.displayYn == "y" }
            } else {
                listOf()
            }

            mAdapter.notifyDataSetChanged()

            return false
        }
    }

    //==============================================================================================
    inner class DiagnosisAdapter : RecyclerView.Adapter<DiagnosisHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DiagnosisHolder(layoutInflater.inflate(R.layout.adapter_pet_detail_diagnosis_item, parent, false))

        override fun onBindViewHolder(holder: DiagnosisHolder, position: Int) {
            holder.setContent(diagnosisItems[position].question)

            if ("Y" == diagnosisItems[position].checkedYn) {
                questionId.apply {
                    this.add(diagnosisItems[position].id)

                    holder.itemView.diagnosisCheck.setImageResource(R.drawable.detail_search_check_on)
                    holder.itemView.diagnosisContent.setTextColor(Helper.readColorRes(R.color.dark_grey))
                }
            } else {
                holder.itemView.diagnosisCheck.setImageResource(R.drawable.detail_search_check_off)
                holder.itemView.diagnosisContent.setTextColor(Helper.readColorRes(R.color.reddishgrey))
            }

            if (questionId.isNotEmpty()) {
                if (questionId.contains(diagnosisItems[position].id)) {
                    holder.itemView.diagnosisCheck.setImageResource(R.drawable.detail_search_check_on)
                    holder.itemView.diagnosisContent.setTextColor(Helper.readColorRes(R.color.dark_grey))
                }
            }

            holder.itemView.setOnClickListener {
                questionId.apply {
                    if(this.contains(diagnosisItems[position].id)) {
                        this.remove(diagnosisItems[position].id)

                        holder.itemView.diagnosisCheck.setImageResource(R.drawable.detail_search_check_off)
                        holder.itemView.diagnosisContent.setTextColor(Helper.readColorRes(R.color.reddishgrey))
                    }
                    else{
                        this.add(diagnosisItems[position].id)

                        holder.itemView.diagnosisCheck.setImageResource(R.drawable.detail_search_check_on)
                        holder.itemView.diagnosisContent.setTextColor(Helper.readColorRes(R.color.dark_grey))
                    }
                }

                Logger.d("questionId : $questionId")
            }

            if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    bottomMargin = margin67
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    bottomMargin = margin29
                }
            }
        }

        override fun getItemCount() = diagnosisItems.size
    }

    inner class DiagnosisHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setContent(_content: String) {
            item.diagnosisContent.text = _content
        }
    }
}