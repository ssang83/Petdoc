package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.RadioGroup
import androidx.core.util.Pair
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_pet_talk_upload.*
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.root
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.petImage
import kotlinx.android.synthetic.main.adapter_register_clinic_type.view.*
import kotlinx.android.synthetic.main.adapter_reservation_beauty_item.view.*
import kotlinx.android.synthetic.main.adapter_time_table_item.view.*
import kotlinx.android.synthetic.main.fragment_hospital_reservation.*
import kotlinx.android.synthetic.main.fragment_hospital_reservation.btnBack
import kotlinx.android.synthetic.main.fragment_hospital_reservation.scrollView
import kotlinx.android.synthetic.main.layout_reservation_all.*
import kotlinx.android.synthetic.main.layout_reservation_type1.*
import kotlinx.android.synthetic.main.layout_reservation_type2.*
import kotlinx.android.synthetic.main.layout_reservation_type3.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.hospital.BeautyReservationActivity
import kr.co.petdoc.petdoc.activity.hospital.ClinicReservationActivity
import kr.co.petdoc.petdoc.activity.hospital.HotelRoomReservationActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalReservationInfoResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.*
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardVisibilityUtils
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.ScrollRulerView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HospitalReservationFragmet
 * Created by kimjoonsung on 2020/06/16.
 *
 * Description :
 */
class HospitalReservationFragment : BaseFragment(), HotelDatePickDialogFragment.CallbackListener {

    private val TAG = "HospitalReservationFragmet"
    private val RESERVATION_INFO_REQUEST = "$TAG.reservationInfoRequest"
    private val RESERVATION_CLINIC_INFO_REQUEST = "$TAG.reservationClinicInfoRequest"
    private val RESERVATION_BEAUTY_INFO_REQUEST = "$TAG.reservationBeautynfoRequest"
    private val RESERVATION_HOTEL_INFO_REQUEST = "$TAG.reservationHotelInfoRequest"

    private lateinit var dataModel: HospitalDataModel
    private var mPetInfoItems: List<PetInfoItem> = listOf()
    private lateinit var petAdapter:PetAdapter

    private var clinicItems:MutableList<CenterClinic> = mutableListOf()
    private lateinit var clinicAdapter:ClinicAdapter

    private var beautyDetailItems:MutableList<Any> = mutableListOf()
    private lateinit var beautyAdapter:BeautyAdapter

    private lateinit var enterTimeAdapter: EnterTimeAdapter
    private var enterTimeItems: MutableList<TimeTable> = mutableListOf()

    private lateinit var outTimeAdapter:OutTimeAdapter
    private var outTimeItems:MutableList<TimeTable> = mutableListOf()

    private var petId = -1
    private var centerId = -1

    private var margin20 = 0
    private var margin4 = 0
    private var margin14 = 0

    private var fromHome = false
    private var hospitalName = ""

    private var clinicIdList : MutableList<Int> = mutableListOf()
    private var clinicAnimalItems: MutableList<ClinicAnimal> = mutableListOf()
    private var beautyAnimalItems:MutableList<BeautyAnimal> = mutableListOf()
    private var hotelAnimalItems:MutableList<HotelAnimal> = mutableListOf()
    private var roomItems:MutableList<Room> = mutableListOf()

    private var clinicBookingType = ""
    private var beautyBookingType = ""
    private var hotelBookingType = ""

    private var weightValue = ""
    private var enterBookingTime = ""
    private var outBookingTime = ""
    private var enterBookingDate = ""
    private var outBookingDate = ""

    private var bookingTab = ""

    private var petKind = ""

    private lateinit var keyboardVisible: KeyboardVisibilityUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_hospital_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        centerId = arguments?.getInt("centerId")!!
        fromHome = arguments?.getBoolean("fromHome")!!
        bookingTab = arguments?.getString("bookingTab") ?: bookingTab

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin4 = Helper.convertDpToPx(requireContext(), 4f).toInt()
        margin14 = Helper.convertDpToPx(requireContext(), 14f).toInt()

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnClinicDate.setOnClickListener {
            if (fromHome) {
                val obj = JSONObject().apply {
                    put("clinicId", JSONArray(clinicIdList))
                }

                startActivity(Intent(requireActivity(), ClinicReservationActivity::class.java).apply {
                    putExtra("fromHome", true)
                    putExtra("centerId", centerId)
                    putExtra("petId", petId)
                    putExtra("petKind", petKind)
                    putExtra("clinicId", obj.toString())
                    putExtra("msg", editMessage.text.toString())
                    putExtra("name", hospitalName)
                    putExtra("bookingTab", bookingTab)
                })
            } else {
                dataModel.clinicIdItems.value = clinicIdList
                dataModel.petId.value = petId
                dataModel.centerId.value = centerId
                dataModel.message.value = editMessage.text.toString()
                findNavController().navigate(R.id.action_hospitalReservationFragmet_to_clinicReservation)
            }
        }

        btnBeautyDate.setOnClickListener {
            if (fromHome) {
                startActivity(Intent(requireActivity(), BeautyReservationActivity::class.java).apply {
                    putExtra("fromHome", true)
                    putExtra("centerId", centerId)
                    putExtra("petId", petId)
                    putExtra("msg", editMessage.text.toString())
                    putExtra("name", hospitalName)
                })
            } else {
                dataModel.petId.value = petId
                dataModel.centerId.value = centerId
                dataModel.message.value = editMessage.text.toString()
                findNavController().navigate(R.id.action_hospitalReservationFragmet_to_beautyReservationFragment)
            }
        }

        btnRoomSelect.setOnClickListener {
            if (fromHome) {
                startActivity(Intent(requireActivity(), HotelRoomReservationActivity::class.java).apply {
                    putExtra("fromHome", true)
                    putExtra("centerId", centerId)
                    putExtra("petId", petId)
                    putExtra("name", hospitalName)
                    putExtra("enterDate", enterBookingDate)
                    putExtra("outDate", outBookingDate)
                    putExtra("enterTime", enterBookingTime)
                    putExtra("outTime", outBookingTime)
                })
            } else {
                dataModel.petId.value = petId
                dataModel.centerId.value = centerId
                dataModel.enterDate.value = enterBookingDate
                dataModel.enterTime.value = enterBookingTime
                dataModel.outDate.value = outBookingDate
                dataModel.outTime.value = outBookingTime
                findNavController().navigate(R.id.action_hospitalReservationFragmet_to_hotelRoomSelectFragment)
            }
        }

        enterDateLayout.setOnClickListener {
//            HotelDatePickDialogFragment(this).show(childFragmentManager, "datePick")
            val builder = MaterialDatePicker.Builder.dateRangePicker().apply {
                setSelection(Pair(Calendar.getInstance().timeInMillis, Calendar.getInstance().timeInMillis))
                setTitleText(Helper.readStringRes(R.string.hospital_hotel_date_title))
                setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen)
            }

            val picker = builder.build()
            picker.show(childFragmentManager, picker.toString())

            picker.addOnNegativeButtonClickListener { picker.dismiss() }
            picker.addOnPositiveButtonClickListener { Logger.d("selected date range : ${it.first} - ${it.second}") }
        }

        editMessage.setOnTouchListener(fun(v: View, event: MotionEvent): Boolean {
            if (editMessage.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_SCROLL) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
            return false
        })

        //==========================================================================================
        petAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }

            adapter = petAdapter

            addItemDecoration(
                HorizontalMarginItemDecoration(
                marginStart = margin20,
                marginBetween = margin4,
                marginEnd = margin20
                )
            )
        }

        clinicAdapter = ClinicAdapter()
        clinicList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
            adapter = clinicAdapter
        }

        beautyAdapter = BeautyAdapter()
        beautyDetailList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = beautyAdapter
        }

        enterTimeAdapter = EnterTimeAdapter()
        enterTimeList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = enterTimeAdapter
        }

        outTimeAdapter = OutTimeAdapter()
        outTimeList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = outTimeAdapter
        }

        //==========================================================================================
        rulerArea.apply {
            max_range = 30
            callback = object : ScrollRulerView.PositionChangeCallback{
                override fun positionCallback(value: Float) {
                    weightValue = String.format("%.1f", value)
                    inputWeight.setText(weightValue)
                    Logger.d("weight value : $weightValue")

                    dataModel.petWeight.value = weightValue
                }
            }
        }

        rulerArea.post{
            rulerArea.moveToWeight(5f)
        }

        //==========================================================================================
        keyboardVisible = KeyboardVisibilityUtils(requireActivity().window,
                onShowKeyboard = { keyboardHeight ->
                    Logger.d("keyboradHeight : $keyboardHeight")
                    scrollView.run {
                        smoothScrollTo(scrollX, scrollY + keyboardHeight)
                    }
                })

        //==========================================================================================
        btnClinicDate.isEnabled = false
        btnBeautyDate.isEnabled = false
        btnRoomSelect.isEnabled = false

        retryIfNetworkAbsent {
            mApiClient.getHospitalReservationInfo(RESERVATION_INFO_REQUEST, centerId)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPetInfo()
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(RESERVATION_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_CLINIC_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_CLINIC_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_BEAUTY_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_BEAUTY_INFO_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_HOTEL_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_HOTEL_INFO_REQUEST)
        }

        keyboardVisible.detachKeyboardListeners()

        super.onDestroyView()
    }

    override fun onDateSelect() {
        Logger.d("onDateSelect")
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
            RESERVATION_INFO_REQUEST -> {
                if ("SUCCESS" == response.code) {
                    if (response is HospitalReservationInfoResponse) {
                        nameTxt.text = response.resultData.name
                        locationTxt.text = response.resultData.location

                        hospitalName = response.resultData.name

                        clinicAnimalItems.addAll(response.resultData.clinicAnimalList)
                        beautyAnimalItems.addAll(response.resultData.beautyAnimalList)
                        hotelAnimalItems.addAll(response.resultData.hotelAnimalList)

                        clinicBookingType = response.resultData.clinicBookingType
                        beautyBookingType = response.resultData.beautyBookingType
                        hotelBookingType = response.resultData.hotelBookingType

                        dataModel.name.value = response.resultData.name
                    }
                } else {
                    Logger.d("error : ${response.messageKey}")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            RESERVATION_CLINIC_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject

                        // clinic list parsing
                        val clinicArray = obj["centerClinicList"]
                        val items: MutableList<CenterClinic> = Gson().fromJson(clinicArray.toString(), object : TypeToken<MutableList<CenterClinic>>() {}.type)
                        clinicItems.clear()
                        clinicItems.addAll(items)
                        clinicAdapter.notifyDataSetChanged()

                        // room list parsing
                        val roomArray = obj["roomList"]
                        roomItems = Gson().fromJson(roomArray.toString(), object : TypeToken<MutableList<Room>>() {}.type)

                        val bookingSetting: BookingSetting = Gson().fromJson(obj["bookingSetting"].toString(), object : TypeToken<BookingSetting>() {}.type)
                        dataModel.bookingSetting.value = bookingSetting
                    } else {
                        val message = JSONObject(event.response)["messageKey"]
                        Logger.d("error : ${message}")
                    }
                }
            }

            RESERVATION_BEAUTY_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {

                    } else {
                        val message = JSONObject(event.response)["messageKey"]
                        Logger.d("error : ${message}")
                    }
                }
            }

            RESERVATION_HOTEL_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {

                    } else {
                        val message = JSONObject(event.response)["messageKey"]
                        Logger.d("error : ${message}")
                    }
                }
            }

        }
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if (StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            if (items.isNotEmpty()) {
                mPetInfoItems = items
                petAdapter.notifyDataSetChanged()
                petId = mPetInfoItems[0].id
            }
        }
    }

    //==============================================================================================
    private fun onItemClicked(item: PetInfoItem) {
        petKind = item.kind.toString()

        val isClinic = clinicAnimalItems.any { it.name == item.kind}
        val isBeauty = beautyAnimalItems.any { it.name == item.kind }
        val isHotel = hotelAnimalItems.any { it.name == item.kind }

        if (isClinic || isBeauty || isHotel) {
            layoutReservationItem.visibility = View.VISIBLE
            layoutReservationInfo.visibility = View.VISIBLE
            noPetReservationTxt.visibility = View.GONE

            setBookingType(
                clinicBookingType,
                beautyBookingType,
                hotelBookingType
            )

            petId = item.id

        } else {
            layoutReservationItem.visibility = View.VISIBLE
            layoutReservationInfo.visibility = View.GONE
            noPetReservationTxt.visibility = View.VISIBLE
        }
    }

    private fun setBookingType(clinicType: String, beautyType: String, hotelType: String) {
        if (((clinicType == BookingType.A.name || clinicType == BookingType.B.name)
                    && beautyType == BookingType.B.name && hotelType == BookingType.B.name)) {
            setReservationTypeAll()
        } else if ((clinicType == BookingType.A.name || clinicType == BookingType.B.name)
                    && beautyType == BookingType.B.name) {
            setReservationClinicAndBeauty()
        } else if ((clinicType == BookingType.A.name || clinicType == BookingType.B.name)
                    && hotelType == BookingType.B.name) {
            setReservationClinicAndHotel()
        } else if (beautyType == BookingType.B.name && hotelType == BookingType.B.name) {
            setReservationBeautyAndHotel()
        } else {
            setReservationSingleType(clinicType, beautyType, hotelType)
        }
    }

    private fun setReservationTypeAll() {
        reservationMultiType.visibility = View.VISIBLE
        reservationType.visibility = View.GONE

        reservationMultiType.removeAllViews()
        val view = layoutInflater.inflate(R.layout.layout_reservation_all, null)
        reservationMultiType.addView(view)

        typeAllLayout.setOnCheckedChangeListener(typeAllcheckedListener)
    }

    private fun setReservationClinicAndBeauty() {
        reservationMultiType.visibility = View.VISIBLE
        reservationType.visibility = View.GONE

        reservationMultiType.removeAllViews()
        val view = layoutInflater.inflate(R.layout.layout_reservation_type1, null)
        reservationMultiType.addView(view)

        type1Layout.setOnCheckedChangeListener(type1checkedListener)
    }

    private fun setReservationClinicAndHotel() {
        reservationMultiType.visibility = View.VISIBLE
        reservationType.visibility = View.GONE

        reservationMultiType.removeAllViews()
        val view = layoutInflater.inflate(R.layout.layout_reservation_type2, null)
        reservationMultiType.addView(view)

        type2Layout.setOnCheckedChangeListener(type2checkedListener)
    }

    private fun setReservationBeautyAndHotel() {
        reservationMultiType.visibility = View.VISIBLE
        reservationType.visibility = View.GONE

        reservationMultiType.removeAllViews()
        val view = layoutInflater.inflate(R.layout.layout_reservation_type3, null)
        reservationMultiType.addView(view)

        type3Layout.setOnCheckedChangeListener(type3checkedListener)
    }

    private fun setReservationSingleType(clinicType: String, beautyType: String, hotelType: String) {
        reservationMultiType.visibility = View.GONE
        reservationType.visibility = View.VISIBLE

        if (clinicType == BookingType.A.name || clinicType == BookingType.B.name) {
            typeImg.setBackgroundResource(R.drawable.ic_reservation_clinic)
            typeName.text = Helper.readStringRes(R.string.hospital_reservation_clinic)

            clinicInfoLayout.visibility = View.VISIBLE

            mApiClient.getHospitalReservationClinicInfo(RESERVATION_CLINIC_INFO_REQUEST, centerId)
        } else if (beautyType == BookingType.B.name) {
            typeImg.setBackgroundResource(R.drawable.ic_reservation_groom)
            typeName.text = Helper.readStringRes(R.string.hospital_reservation_beauty)

            beautyInfoLayout.visibility = View.VISIBLE

            mApiClient.getHospitalReservationBeautyInfo(RESERVATION_BEAUTY_INFO_REQUEST, centerId)
        } else if(hotelType == BookingType.B.name) {
            typeImg.setBackgroundResource(R.drawable.ic_reservation_hotel)
            typeName.text = Helper.readStringRes(R.string.hospital_reservation_hotel)

            hotelInfoLayout.visibility = View.VISIBLE

            val currentDate = Calendar.getInstance().time
            val nextDate = Calendar.getInstance().apply {
                add(Calendar.DATE, +1)
            }

            enterBookingDate = SimpleDateFormat("yyyy년 MM월 dd일 EE", Locale.KOREAN).format(currentDate).substring(6)
            outBookingDate = SimpleDateFormat("yyyy년 MM월 dd일 EE", Locale.KOREAN).format(nextDate.time).substring(6)

            Logger.d("current day : $enterBookingDate, next day : $outBookingDate")

            enterDate.text = enterBookingDate
            enterTime.text = enterBookingDate
            outDate.text = outBookingDate
            outTime.text = outBookingDate

            //TODO : 호텔 예약 정보 API 호출
        } else {
            reservationMultiType.visibility = View.GONE
            reservationType.visibility = View.GONE
        }
    }

    private fun checkClinic() {
        val selectRoomList: MutableList<Room> = mutableListOf()
        var clinicIdCount = false
        for (item in roomItems) {
            if (item.animalList.any { it.name == petKind }) {
                for (id in clinicIdList) {
                    if (item.clinicList.any { it.clinicId == id }) {
                        clinicIdCount = true
                    } else {
                        clinicIdCount = false
                        break
                    }
                }

                if (clinicIdCount) {
                    selectRoomList.add(item)
                    dataModel.bookingClinicRoomItems.value = selectRoomList
                }
            }
        }

        if (selectRoomList.size == 0) {
            noClinicTxt.visibility = View.VISIBLE
            layoutMessage.visibility = View.GONE
            editMessage.visibility = View.GONE

            btnClinicDate.apply {
                isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.grey_round_rect)
            }
        } else {
            noClinicTxt.visibility = View.GONE
            layoutMessage.visibility = View.VISIBLE
            editMessage.visibility = View.VISIBLE

            btnClinicDate.apply {
                isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.main_color_round_rect)
            }
        }
    }

    //==============================================================================================
    inner class PetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1

        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_ITEM -> PetHolder(layoutInflater.inflate(R.layout.adapter_care_pet_item, parent, false))
                else -> FooterHolder(layoutInflater.inflate(R.layout.adapter_care_pet_footer, parent, false))
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
                        onItemClicked(mPetInfoItems[position])
                        selectedPosition = position
                        notifyDataSetChanged()
                    }
                }

                else -> {
                    if (itemCount == 0) {
                        (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                            leftMargin = margin20
                        }
                    } else {
                        (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                            leftMargin = margin4
                            rightMargin = margin20
                        }
                    }

                    (holder as FooterHolder).itemView.layoutAdd.setOnClickListener {
                        if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                            if(StorageUtils.loadIntValueFromPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM) == 1) {
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

                    holder.itemView.layoutPetInfo.visibility = View.GONE
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

    //==============================================================================================
    inner class ClinicAdapter : RecyclerView.Adapter<ClinicHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ClinicHolder(layoutInflater.inflate(R.layout.adapter_register_clinic_type, parent, false))

        override fun onBindViewHolder(holder: ClinicHolder, position: Int) {
            holder.setClinicName(clinicItems[position].name)

            holder.itemView.btnCheckClinic.setOnClickListener {
                it.isSelected = !it.isSelected
                val id = clinicItems[position].clinicId
                if (it.isSelected) {
                    clinicIdList.add(id)
                } else {
                    clinicIdList.remove(id)
                }

                Logger.d("clinic id list : $clinicIdList")

                if (clinicIdList.size > 0) {
                    btnClinicDate.isEnabled = true

                    btnClinicDate.setTextColor(Helper.readColorRes(R.color.orange))
                    btnClinicDate.setBackgroundResource(R.drawable.main_color_round_rect)
                } else {
                    btnClinicDate.isEnabled = false

                    btnClinicDate.setTextColor(Helper.readColorRes(R.color.light_grey3))
                    btnClinicDate.setBackgroundResource(R.drawable.grey_round_rect)
                }

                if (clinicIdList.size > 0) {
                    checkClinic()
                }
            }
        }

        override fun getItemCount() = clinicItems.size
    }

    inner class ClinicHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setClinicName(_clinic: String) {
            item.clinicName.text = _clinic
        }
    }

    //=============================================================================================
    inner class BeautyAdapter : RecyclerView.Adapter<BeautyHolder>() {
        private var selectedPosition = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            BeautyHolder(layoutInflater.inflate(R.layout.adapter_reservation_beauty_item, parent, false))

        override fun onBindViewHolder(holder: BeautyHolder, position: Int) {
            holder.setImage("")
            holder.setBeautyType("목욕")
            holder.setDescription("예상 소요시간 : 1시간")

            if (selectedPosition == position) {
                holder.itemView.description.visibility = View.VISIBLE
                holder.itemView.btnCheck.visibility = View.VISIBLE
                holder.itemView.viewCheck.visibility = View.VISIBLE
                holder.itemView.setBackgroundResource(R.color.grey_alpha_50)
            } else {
                holder.itemView.description.visibility = View.GONE
                holder.itemView.btnCheck.visibility = View.GONE
                holder.itemView.viewCheck.visibility = View.GONE
                holder.itemView.setBackgroundResource(R.color.white)
            }

            holder.itemView.setOnClickListener {
                holder.itemView.description.visibility = View.VISIBLE
                holder.itemView.btnCheck.visibility = View.VISIBLE
                holder.itemView.viewCheck.visibility = View.VISIBLE
                holder.itemView.setBackgroundResource(R.color.grey_alpha_50)

                btnBeautyDate.isEnabled = true
                btnBeautyDate.setTextColor(Helper.readColorRes(R.color.orange))
                btnBeautyDate.setBackgroundResource(R.drawable.main_color_round_rect)

                selectedPosition = position
                notifyDataSetChanged()
            }
        }

//        override fun getItemCount() = beautyDetailItems.size
        override fun getItemCount() = 3
    }

    inner class BeautyHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(_url: String) {
            item.petImage.apply {
                when {
                    _url.isNotEmpty() -> {
                        visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.petImage)
                    }

                    else -> visibility = View.GONE
                }
            }
        }

        fun setBeautyType(_type: String) {
            item.beautyType.text = _type
        }

        fun setDescription(_desc: String) {
            item.description.text = _desc
        }
    }

    //==============================================================================================
    private var selectedEnterTimePosition = -1

    inner class EnterTimeAdapter : RecyclerView.Adapter<EnterTimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            EnterTimeHolder(layoutInflater.inflate(R.layout.adapter_time_table_item, parent, false))

        override fun onBindViewHolder(holder: EnterTimeHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin20
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin20
                    leftMargin = margin14
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin14
                    rightMargin = 0
                }
            }

//            if (selectedEnterTimePosition == position) {
//                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
//                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
//            } else {
//                if (enterTimeItems[position].possible) {
//                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
//                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
//                } else {
//                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey2))
//                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_disable)
//                }
//            }
//
//            holder.setTime(enterTimeItems[position].startTime)
//
//            holder.itemView.setOnClickListener {
//                if (enterTimeItems[position].possible) {
//                    enterBookingTime = enterTimeItems[position].startTime
//
//                    selectedEnterTimePosition = position
//                    notifyDataSetChanged()
//
//                    Logger.d("enter bookingTime : $enterBookingTime")
//
//                    btnRoomSelect.isEnabled = true
//                    btnRoomSelect.setTextColor(Helper.readColorRes(R.color.orange))
//                    btnRoomSelect.setBackgroundResource(R.drawable.main_color_round_rect)
//                }
//            }

            if (selectedEnterTimePosition == position) {
                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
            } else {
                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
            }

            holder.setTime("11:00")

            holder.itemView.setOnClickListener {
                selectedEnterTimePosition = position
                notifyDataSetChanged()

                btnRoomSelect.isEnabled = true
                btnRoomSelect.setTextColor(Helper.readColorRes(R.color.orange))
                btnRoomSelect.setBackgroundResource(R.drawable.main_color_round_rect)
            }
        }

//        override fun getItemCount() = enterTimeItems.size
        override fun getItemCount() = 6
    }

    inner class EnterTimeHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setTime(_time: String) {
            item.timeTable.text = _time
        }
    }

    //=============================================================================================
    private var selectedOutTimePosition = -1

    inner class OutTimeAdapter : RecyclerView.Adapter<OutTimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            OutTimeHolder(layoutInflater.inflate(R.layout.adapter_time_table_item, parent, false))

        override fun onBindViewHolder(holder: OutTimeHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin20
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin20
                    leftMargin = margin14
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin14
                    rightMargin = 0
                }
            }

//            if (selectedOutTimePosition == position) {
//                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
//                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
//            } else {
//                if (outTimeItems[position].possible) {
//                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
//                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
//                } else {
//                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey2))
//                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_disable)
//                }
//            }
//
//            holder.setTime(outTimeItems[position].startTime)
//
//            holder.itemView.setOnClickListener {
//                if (outTimeItems[position].possible) {
//                    outBookingTime = outTimeItems[position].startTime
//
//                    selectedOutTimePosition = position
//                    notifyDataSetChanged()
//
//                    Logger.d("out bookingTime : $outBookingTime")
//
//                    btnRoomSelect.isEnabled = true
//                    btnRoomSelect.setTextColor(Helper.readColorRes(R.color.orange))
//                    btnRoomSelect.setBackgroundResource(R.drawable.main_color_round_rect)
//                }
//            }

            if (selectedOutTimePosition == position) {
                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
            } else {
                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
            }

            holder.setTime("11:00")

            holder.itemView.setOnClickListener {
                selectedOutTimePosition = position
                notifyDataSetChanged()

                btnRoomSelect.isEnabled = true
                btnRoomSelect.setTextColor(Helper.readColorRes(R.color.orange))
                btnRoomSelect.setBackgroundResource(R.drawable.main_color_round_rect)
            }
        }

        //        override fun getItemCount() = enterTimeItems.size
        override fun getItemCount() = 6
    }

    inner class OutTimeHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setTime(_time: String) {
            item.timeTable.text = _time
        }
    }

    //=============================================================================================
    private val typeAllcheckedListener = object : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                R.id.btnTypeAllClinic -> {
                    clinicInfoLayout.visibility = View.VISIBLE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.GONE
                }

                R.id.btnTypeAllBeauty -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.VISIBLE
                    hotelInfoLayout.visibility = View.GONE
                }

                R.id.btnTypeAllHotel -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private val type1checkedListener = object : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                R.id.btnClinicType1 -> {
                    clinicInfoLayout.visibility = View.VISIBLE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.GONE
                }

                R.id.btnBeautyType1 -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.VISIBLE
                    hotelInfoLayout.visibility = View.GONE
                }
            }
        }
    }

    private val type2checkedListener = object : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                R.id.btnClinicType2 -> {
                    clinicInfoLayout.visibility = View.VISIBLE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.GONE
                }

                R.id.btnHotelType2 -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private val type3checkedListener = object : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when (checkedId) {
                R.id.btnBeautyType3 -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.VISIBLE
                    hotelInfoLayout.visibility = View.GONE
                }

                R.id.btnHotelType3 -> {
                    clinicInfoLayout.visibility = View.GONE
                    beautyInfoLayout.visibility = View.GONE
                    hotelInfoLayout.visibility = View.VISIBLE
                }
            }
        }
    }
}