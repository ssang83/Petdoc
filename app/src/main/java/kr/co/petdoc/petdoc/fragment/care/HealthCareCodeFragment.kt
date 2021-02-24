package kr.co.petdoc.petdoc.fragment.care

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.fragment_care.*
import kotlinx.android.synthetic.main.fragment_health_care_code.*
import kotlinx.android.synthetic.main.fragment_health_care_code.petList
import kotlinx.android.synthetic.main.layout_health_care_bottom.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.GodoMallOutlinkActivity
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.db.care.health_care.HealthCare
import kr.co.petdoc.petdoc.db.care.health_care.HealthCareDB
import kr.co.petdoc.petdoc.dialog.FindCareCodeDialog
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.AuthCodeStatusResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.AuthCodeStatusItem
import kr.co.petdoc.petdoc.network.response.submodel.DnaBookingData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.utils.screenRectPx
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HealthCareCodeFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareCodeFragment : BaseFragment() {

    private val TAG = "HealthCareCodeFragment"
    private val CODE_AUTH_REQUEST = "$TAG.codeAuthRequest"
    private val HEALTH_CARE_STATUS_REQUEST = "$TAG.healthCareStatusRequest"
    private val HEALTH_CARE_BOOKING_REQUEST = "$TAG.healthCareBookingRequest"

    private lateinit var dataModel:HospitalDataModel
    private lateinit var healthCareDb : HealthCareDB
    private lateinit var mAdapter:PetAdapter
    private lateinit var mPetLayoutManager: LinearLayoutManager
    private var mPetInfoItems:MutableList<PetInfoItem> = mutableListOf()

    private var selectPetItem:PetInfoItem? = null
    private var authKeyStatusData: AuthCodeStatusItem? = null
    private var healthCare:HealthCare? = null
    private var marginStart = 0
    private var isReload = false

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    private var margin12 = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        marginStart = (screenRectPx.width() / 2) - (resources.getDimensionPixelSize(R.dimen.health_carepet_image) / 2)

        healthCareDb = HealthCareDB.getInstance(requireContext())!!

        margin12 = Helper.convertDpToPx(requireContext(), 12f).toInt()

        //==========================================================================================
        btnClose.setOnClickListener { requireActivity().onBackPressed() }
        btnConfirm.setOnClickListener {
            mApiClient.checkAuthCode(CODE_AUTH_REQUEST, editCode.text.toString(), dataModel.petId.value!!)
        }

        findCodeLayer.setOnClickListener { FindCareCodeDialog(requireContext()).show() }
        purchaseLayer.setOnClickListener {
            startActivity(Intent(requireActivity(), GodoMallOutlinkActivity::class.java).apply {
                putExtra(GodoMallOutlinkActivity.INTENT_EXTRA_KEY_URL, "https://www.vlab.kr/goods/goods_view.php?goodsNo=1000187124")
            })
        }

        editCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnConfirm.apply {
                    when {
                        s?.length!! == 7 -> {
                            isEnabled = true
                            setTextColor(Helper.readColorRes(R.color.orange))
                            setBackgroundResource(R.drawable.main_color_round_rect)
                        }

                        else -> {
                            isEnabled = false
                            setTextColor(Helper.readColorRes(R.color.light_grey3))
                            setBackgroundResource(R.drawable.grey_round_rect)
                        }
                    }
                }
            }
        })

        editCode.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.action?.let {
                    if (MotionEvent.ACTION_DOWN == it) {
                        if (selectPetItem == null) {
                            AppToast(requireContext()).showToastMessage(
                                "반려동물을 먼저 선택해주세요.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )

                            return true
                        }
                    }
                }
                return false
            }
        })

        //==========================================================================================
        mAdapter = PetAdapter()
        mPetLayoutManager = LinearLayoutManager(requireContext()).apply { orientation = LinearLayoutManager.HORIZONTAL }
        petList.apply {
            layoutManager = mPetLayoutManager
            adapter = mAdapter

            addItemDecoration(HorizontalMarginItemDecoration(
                marginStart = marginStart,
                marginBetween = margin12,
                marginEnd = margin12
            ))
        }

        btnConfirm.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        loadPetInfo()
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(HEALTH_CARE_BOOKING_REQUEST)) {
            mApiClient.cancelRequest(HEALTH_CARE_BOOKING_REQUEST)
        }

        if (mApiClient.isRequestRunning(HEALTH_CARE_STATUS_REQUEST)) {
            mApiClient.cancelRequest(HEALTH_CARE_STATUS_REQUEST)
        }

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
            CODE_AUTH_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val messageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        saveAuthCode()
                        dataModel.healthCareCode.value = editCode.text.toString()
                        dataModel.isPurchseBtnShow.value = false
                        findNavController().navigate(HealthCareCodeFragmentDirections.actionHealthCareCodeFragmentToHealthCareHospitalFragment())
                    } else {
                        Logger.d("error : $messageKey")
                        val message = if ((messageKey == "error.key.wrongkey") ||
                                (messageKey == "error.key.length")) {
                            "사용할 수 없는 식별코드입니다. 다시 확인해주세요."
                        } else {
                            "유효하지 않은 코드 입니다. 검사 코드를 다시 확인해 주세요."
                        }
                        AppToast(requireContext()).showToastMessage(
                            message,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }

            HEALTH_CARE_BOOKING_REQUEST -> {
                if (event.status == "ok") {
                    try {
                        val code = JSONObject(event.response)["code"]
                        val mesageKey = JSONObject(event.response)["messageKey"].toString()
                        val resultData = JSONObject(event.response)["resultData"] as JSONObject
                        try {
                            if ("SUCCESS" == code) {
                                if (!resultData.isNull("bookingData")) {
                                    val json = resultData["bookingData"] as JSONObject
                                    val bookingData:DnaBookingData = Gson().fromJson(json.toString(), object : TypeToken<DnaBookingData>() {}.type)
                                    dataModel.bookingId.value = bookingData.bookingId
                                    hospitalName.text = bookingData.centerName

                                    val time = bookingData.bookingDate.split(" ")
                                    bookingTime.text = "${calculateDate(time[0])} ${calculateTime(time[1])}"
                                    Logger.d("bookingData : ${bookingData}")
                                } else {
                                    Logger.d("bookingData is null")
                                }
                            } else {
                                Logger.d("error : $mesageKey")
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            HEALTH_CARE_STATUS_REQUEST -> {
                if (response is AuthCodeStatusResponse) {
                    if ("SUCCESS" == response.code) {
                        authKeyStatusData = response.resultData
                        when (response.resultData.goodsPosition) {
                            3,5,6 -> { // 병원 예약 완료 상태
                                inputCodeLayer.visibility = View.GONE
                                bookingCompleteLayer.visibility = View.VISIBLE
                            }
                            else -> {
                                inputCodeLayer.visibility = View.VISIBLE
                                bookingCompleteLayer.visibility = View.GONE

                                if(response.resultData.goodsPosition != 7) {
                                    editCode.setText(response.resultData.authKey)
                                }
                            }
                        }

                        mApiClient.getBookingIdForAuthCode(HEALTH_CARE_BOOKING_REQUEST, response.resultData.authKey)
                    } else {
                        Logger.d("error : ${response.messageKey}")
                        inputCodeLayer.visibility = View.VISIBLE
                        bookingCompleteLayer.visibility = View.GONE
                    }
                }
            }
        }
    }

    //==============================================================================================
    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            try {
                PetdocApplication.mPetInfoList.clear()
                PetdocApplication.mPetInfoList.addAll(items)

                mPetInfoItems.clear()
                mPetInfoItems.addAll(items.filter { it.regPetStep == 6 })
                mAdapter.notifyDataSetChanged()

                if (isReload) {
                    Handler().postDelayed({petList.smoothScrollToPosition(mAdapter.itemCount)}, 100)
                    isReload = false
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    private fun saveAuthCode() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            withContext(Dispatchers.Default) {
                HealthCare().apply {
                    petId = dataModel.petId.value!!
                    authCode = editCode.text.toString()
                    deleteYN = "N"
                }.let {
                    if (dataModel.authCodeUpdate.value!!) {
                        healthCareDb.healthCareDAO().updateAuthCode(dataModel.petId.value!!, editCode.text.toString())
                    } else {
                        healthCareDb.healthCareDAO().insert(it)
                    }
                }
            }
        }
    }

    private fun onItemClicked(item: PetInfoItem, view:View) {
        dataModel.petId.value = item.id
        dataModel.petImage.value = item.imageUrl
        selectPetItem = item

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            healthCare = async {
                healthCareDb.healthCareDAO().load(item.id, "N")
            }.await()

            Logger.d("authCode : ${healthCare?.authCode}")

            if(healthCare != null) {
                dataModel.authCodeUpdate.postValue(true)
                dataModel.healthCareCode.postValue(healthCare!!.authCode)
            } else {
                dataModel.authCodeUpdate.postValue(false)
            }
        }

        scrollToCenter(view)
        editCode.setText("")

        mApiClient.getAuthCodeStatus(HEALTH_CARE_STATUS_REQUEST, item.id)
    }

    private fun scrollToCenter(view: View) {
        val itemToScroll = petList.getChildAdapterPosition(view)
        val centerOfScreen = petList.width / 2 - view.width / 2
        mPetLayoutManager.scrollToPositionWithOffset(itemToScroll, centerOfScreen)
    }

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    private fun calculateTime(time: String) : String {
        val format1 = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
        val format = SimpleDateFormat("a kk:mm", Locale.KOREA)

        val time = format1.parse(time)

        return format.format(time)
    }

    //==============================================================================================
    inner class PetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1

        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                when (viewType) {
                    TYPE_ITEM -> PetHolder(layoutInflater.inflate(R.layout.adapter_health_care_pet_item, parent, false))
                    else -> FooterHolder(layoutInflater.inflate(R.layout.adapter_pet_list_footer, parent, false))
                }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                TYPE_ITEM -> {
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

                    (holder as PetHolder).setName(mPetInfoItems[position].name!!)
                    holder.setImage(mPetInfoItems[position].imageUrl!!)

                    holder.itemView.setOnClickListener {
                        if (mPetInfoItems[position].kind == "강아지" || mPetInfoItems[position].kind == "고양이") {
                            onItemClicked(mPetInfoItems[position], holder.itemView)
                            selectedPosition = position
                            notifyDataSetChanged()
                        } else {
                            OneBtnDialog(requireContext()).apply {
                                setTitle("특수동물 선택 불가")
                                setMessage("아쉽게도, 반려견과 반려묘만 혈액, 알러지 검사가 가능합니다. 특수동물도 검사할 수 있게 빠르게 개선하겠습니다.")
                                setConfirmButton(
                                    Helper.readStringRes(R.string.confirm),
                                    View.OnClickListener {
                                        dismiss()
                                    })
                                show()
                            }
                        }
                    }
                }

                else -> {
                    (holder as FooterHolder).itemView.layoutAdd.setOnClickListener {
                        if (StorageUtils.loadBooleanValueFromPreference(
                                        requireContext(),
                                        AppConstants.PREF_KEY_LOGIN_STATUS
                                )
                        ) {
                            if (StorageUtils.loadIntValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_MOBILE_CONFIRM
                                    ) == 1
                            ) {
                                isReload = true
                                startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                                    putExtra("type", PetAddType.ADD.ordinal)
                                })
                            } else {
                                startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
                            }
                        } else {
                            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                        }
                    }
                }
            }
        }

        override fun getItemCount() =
                if (mPetInfoItems.size == 0) {
                    1
                } else {
                    mPetInfoItems.size + 1
                }

        override fun getItemViewType(position: Int): Int {
            if (mPetInfoItems.size == 0) {
                return TYPE_FOOTER
            } else {
                if (position == mPetInfoItems.size) {
                    return TYPE_FOOTER
                } else {
                    return TYPE_ITEM
                }
            }
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

    inner class FooterHolder(view:View) : RecyclerView.ViewHolder(view)
}