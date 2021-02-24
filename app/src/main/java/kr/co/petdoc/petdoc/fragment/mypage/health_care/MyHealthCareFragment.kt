package kr.co.petdoc.petdoc.fragment.mypage.health_care

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.adapter_my_health_care_item.view.*
import kotlinx.android.synthetic.main.fragment_my_health_care.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.HealthCareResultItem
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: MyHealthCareFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class MyHealthCareFragment : BaseFragment() {

    private val LOGTAG = "MyHealthCareFragment"
    private val RESULT_LIST_REQUEST = "$LOGTAG.resultListRequest"

    private lateinit var dataModel : MyPageInformationModel

    private lateinit var petAdapter: PetAdapter
    private var resultAdapter: ResultAdapter? = null
    private var resultItems:MutableList<HealthCareResultItem> = mutableListOf()

    private var margin20 = 0
    private var margin4 = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java)
        return inflater.inflate(R.layout.fragment_my_health_care, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin4 = Helper.convertDpToPx(requireContext(), 4f).toInt()

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnGoHealthCare.setOnClickListener { startActivity(Intent(requireActivity(), HealthCareActivity::class.java)) }

        //==========================================================================================
        petAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { orientation = LinearLayoutManager.HORIZONTAL }
            adapter = petAdapter

            addItemDecoration(
                HorizontalMarginItemDecoration(
                marginStart = margin20,
                marginBetween = margin4,
                marginEnd = margin20
            )
            )
        }

        //==========================================================================================

        loadPetInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        when(event.tag) {
            RESULT_LIST_REQUEST -> {
                if (event.status == "ok") {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    val resultData = JSONObject(event.response)["resultData"] as JSONObject
                    if ("SUCCESS" == code) {
                        try {
                            resultItems.clear()
                            val jsonArray = resultData["dnaResultSendList"] as JSONArray
                            resultItems = Gson().fromJson(
                                jsonArray.toString(),
                                object : TypeToken<MutableList<HealthCareResultItem>>() {}.type
                            )

                            if (resultItems.size > 0) {
                                emptyLayout.visibility = View.GONE
                                resultList.visibility = View.VISIBLE

                                resultAdapter = ResultAdapter()
                                resultList.apply { adapter = resultAdapter }

                                resultAdapter?.notifyDataSetChanged()
                            } else {
                                emptyLayout.visibility = View.VISIBLE
                                resultList.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } else {
                        Logger.d("error : $mesageKey")
                    }
                }
            }
        }
    }

    private fun onItemClicked(item: PetInfoItem) {
        dataModel.petId.value = item.id
        dataModel.petName.value = item.name
        dataModel.petImage.value = item.imageUrl

        mApiClient.getHealthCareResultForPet(RESULT_LIST_REQUEST, item.id)
    }

    private fun onResultItemCliced(item: HealthCareResultItem) {
        dataModel.visitDate.value = item.requestDate.split(" ")[0]
        dataModel.resultDate.value = item.resultSendDate.split(" ")[0]
        dataModel.centerName.value = item.centerName
        dataModel.bookingId.value = item.bookingId

        bundleOf(HEALTH_CARE_RESULT_CONFIG to MyHeathCareResultConfig(
            item.bookingId,
            dataModel.petName.value?:"",
            dataModel.resultDate.value?:"",
            dataModel.petImage.value?:""
        )).let {
            findNavController().navigate(R.id.action_myHealthCareFragment_to_myHealthCareResultFragment, it)
        }

        //findNavController().navigate(MyHealthCareFragmentDirections.actionMyHealthCareFragmentToMyHealthCareResultFragment())
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            try {
                val items = repository.retrieveMyPets(oldUserId)
                mPetInfoItems = items.filter { it.kind == "강아지" || it.kind == "고양이" }.toMutableList()

                if(dataModel.deepLink.value!!) {
                    for(i in 0 until mPetInfoItems.size) {
                        if(dataModel.petId.value == mPetInfoItems[i].id) {
                            petAdapter.setPetSelection(i)
                            mApiClient.getHealthCareResultForPet(RESULT_LIST_REQUEST, dataModel.petId.value!!)
                        }
                    }
                } else {
                    if(mPetInfoItems.size > 0) {
                        dataModel.petId.value = mPetInfoItems[0].id
                        dataModel.petName.value = mPetInfoItems[0].name
                        dataModel.petImage.value = mPetInfoItems[0].imageUrl
                        petAdapter.notifyDataSetChanged()

                        mApiClient.getHealthCareResultForPet(RESULT_LIST_REQUEST, mPetInfoItems[0].id)
                    } else {
                        emptyLayout.visibility = View.VISIBLE
                        resultList.visibility = View.GONE
                        petList.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    //==============================================================================================
    private var mPetInfoItems:MutableList<PetInfoItem> = mutableListOf()

    inner class PetAdapter : RecyclerView.Adapter<PetHolder>() {
        var selectedPosition = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PetHolder(layoutInflater.inflate(R.layout.adapter_care_pet_item, parent, false))

        override fun onBindViewHolder(holder: PetHolder, position: Int) {
            holder.setIsRecyclable(false)

            if (position == 0) {
                holder.itemView.petCrown.visibility = View.VISIBLE
            } else {
                holder.itemView.petCrown.visibility = View.GONE
            }

            if (selectedPosition == position) {
                holder.itemView.border.setBackgroundResource(R.drawable.orange_circle_stroke)
            } else {
                holder.itemView.border.setBackgroundResource(R.drawable.grey_full_round_stroke_rect)
            }

            holder.setName(mPetInfoItems[position].name!!)
            holder.setImage(mPetInfoItems[position].imageUrl!!)

            holder.itemView.setOnClickListener {
                onItemClicked(mPetInfoItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = mPetInfoItems.size

        fun setPetSelection(position: Int) {
            this.selectedPosition = position
            notifyDataSetChanged()
        }
    }

    inner class PetHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setName(_name: String) {
            item.petName.text = _name
        }

        fun setImage(_url: String) {
            item.petImage.apply {
                when {
                    _url.isNotEmpty() -> {
                        Glide.with(requireContext())
                            .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.petImage)
                    }

                    else -> setBackgroundResource(R.drawable.img_pet_profile_default)
                }
            }
        }
    }

    //==============================================================================================
    inner class ResultAdapter : RecyclerView.Adapter<ResultHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ResultHolder(layoutInflater.inflate(R.layout.adapter_my_health_care_item, parent, false))

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            val date = resultItems[position].resultSendDate.split(" ")
            holder.setDate(date[0].replace("-", "."))
            holder.setNewBadge(resultItems[position].readYn)

            if (resultItems[position].clinicId == 99) {
                holder.setTestType("혈액/알러지 검사")
                holder.setDescription("혈액검사 44개 항목, 알러지검사 105개 항목")
            } else {
                holder.setTestType("유전자 검사")
                holder.setDescription("유전 질병 검사 27개 항목")
            }

            holder.itemView.setOnClickListener {
                onResultItemCliced(resultItems[position])
            }
        }

        override fun getItemCount() = resultItems.size
    }

    class ResultHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setDate(_date: String) {
            item.date.text = _date
        }

        fun setTestType(_type: String) {
            item.testType.text = _type
        }

        fun setDescription(_desc: String) {
            item.description.text = _desc
        }

        fun setNewBadge(_readYn: String) {
            item.newBadge.apply {
                when {
                    _readYn == "N" -> visibility = View.VISIBLE
                    else -> visibility = View.GONE
                }
            }
        }
    }
}