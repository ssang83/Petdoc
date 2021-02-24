package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_clinic_reservation_room_item.view.*
import kotlinx.android.synthetic.main.adapter_time_table_item.view.*
import kotlinx.android.synthetic.main.fragment_clinic_reservation.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.BookingPolicyActivity
import kr.co.petdoc.petdoc.activity.hospital.ClinicReservationCompleteActivity
import kr.co.petdoc.petdoc.adapter.hospital.CalendarAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.BookingSetting
import kr.co.petdoc.petdoc.network.response.submodel.Room
import kr.co.petdoc.petdoc.network.response.submodel.TimeTable
import kr.co.petdoc.petdoc.utils.BaseCalendar
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ClinicReservation
 * Created by kimjoonsung on 2020/06/16.
 *
 * Description :
 */
class ClinicReservationFragment : BaseFragment(), CalendarAdapter.AdapterListener {

    private val TAG = "ClinicReservationFragment"
    private val TIME_TABLE_LIST_REQUEST = "$TAG.timeTableListRequest"
    private val RESERVATION_REQUEST = "$TAG.reservationRequest"
    private val RESERVATION_CLINIC_INFO_REQUEST = "$TAG.reservationClinicInfoRequest"

    private lateinit var dataModel: HospitalDataModel

    private lateinit var roomAdapter: RoomAdapter
    private var roomItems: MutableList<Room> = mutableListOf()
    private var bookingSetting:BookingSetting? = null

    private var timeAdapter:TimeAdapter? = null

    private lateinit var calendarAdapter: CalendarAdapter
    private val baseCalendar = BaseCalendar()

    private var margin20 = 0
    private var margin14 = 0

    private var warningCheckValid = booleanArrayOf(false, false)

    private var resultData:JSONObject? = null

    // Activity 에서 진입하는 경우 사용하는 변수들...
    private var fromHome = false
    private var centerId = -1
    private var clinicRoomId = -1
    private var petId = -1
    private var clinicId = ""
    private var message = ""
    private var hospitalName = ""

    private var clinicItems:MutableList<Int> = mutableListOf()

    private var bookingDay = ""
    private var bookingTime = ""
    private var currentMonth = ""

    private var bookingTab = ""
    private var petKind = ""

    private var curFocusedGroupPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_clinic_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        fromHome = arguments?.getBoolean("fromHome")!!
        bookingTab = arguments?.getString("bookingTab") ?: bookingTab

        if (!fromHome) {
            roomItems = dataModel.bookingClinicRoomItems.value!!
            bookingSetting = dataModel.bookingSetting.value!!
            centerId = dataModel.centerId.value!!
            petId = dataModel.petId.value!!
            clinicItems = dataModel.clinicIdItems.value!!
            message = dataModel.message.value!!

            registerName.text = dataModel.name.value!!.toString()
        } else {
            centerId = arguments?.getInt("centerId")!!
            petId = arguments?.getInt("petId", petId)!!
            clinicId = arguments?.getString("clinicId")!!
            message = arguments?.getString("msg")!!
            hospitalName = arguments?.getString("name")!!
            petKind = arguments?.getString("petKind") ?: petKind

            registerName.text = hospitalName

            val items = JSONObject(clinicId)["clinicId"] as JSONArray
            clinicItems = Gson().fromJson(items.toString(), object : TypeToken<MutableList<Int>>() {}.type)

            mApiClient.getHospitalReservationClinicInfo(RESERVATION_CLINIC_INFO_REQUEST, centerId)
        }

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin14 = Helper.convertDpToPx(requireContext(), 14f).toInt()

        //==========================================================================================
        btnBack.setOnClickListener{ requireActivity().onBackPressed() }
        btnReservatioCompleted.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_예약완료", "Click Event", "예약 완료 버튼 클릭")
            if (validationReservation()) {
                mApiClient.postHospitalClinicReservationRequest(
                    RESERVATION_REQUEST,
                    centerId,
                    clinicRoomId,
                    petId,
                    message,
                    "${bookingDay}T${bookingTime}",
                    clinicItems
                )
            }
        }

        checkAllAgree.setOnClickListener(clickListener)
        checkAgreee1.setOnClickListener(clickListener)
        checkAgreee2.setOnClickListener(clickListener)

        btnNextMonth.setOnClickListener{ calendarAdapter.changeToNextMonth() }
        btnBeforeMonth.setOnClickListener { calendarAdapter.changeToPrevMonth() }

        textViewAgreeDesc2.setOnClickListener(clickListener)
        //==========================================================================================

        roomAdapter = RoomAdapter()
        roomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }

        baseCalendar.initBaseCalendar {
            val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
            header.text = sdf.format(it.time)

            val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
            currentMonth = dayFormat.format(it.time)
            Logger.d("current month : $currentMonth")
        }

        textViewAgreeDesc2.paintFlags = textViewAgreeDesc2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(TIME_TABLE_LIST_REQUEST)) {
            mApiClient.cancelRequest(TIME_TABLE_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_CLINIC_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_CLINIC_INFO_REQUEST)
        }

        super.onDestroyView()
    }

    override fun refreshCurrentMonth(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
        header.text = sdf.format(calendar.time)

        val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
        currentMonth = dayFormat.format(calendar.time)
        Logger.d("current month : $currentMonth")
    }

    override fun onDateClicked(date: String) {
        bookingDay = "${currentMonth}-${String.format("%02d", date.toInt())}"

        val date = SimpleDateFormat("yyyyMMdd", Locale.KOREAN).parse(bookingDay.replace("-", ""))
        val transDate = SimpleDateFormat("yyyy년 MM월 dd일 EE", Locale.KOREAN).format(date!!)
        selectedDate.text = transDate.substring(6)

        Logger.d("booking day : $bookingDay, selectedDate : ${transDate.substring(6)}")
        mApiClient.getTimeTableList(TIME_TABLE_LIST_REQUEST, bookingSetting?.centerId!!, bookingDay)
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
            RESERVATION_REQUEST -> {
                try {
                    if ("ok" == event.status) {
                        val code = JSONObject(event.response)["code"]
                        val message = JSONObject(event.response)["messageKey"]
                        if ("SUCCESS" == code) {
                            val obj = JSONObject(event.response)["resultData"] as JSONObject
                            val bookingId = obj["id"] as Int
                            if (!fromHome) {
                                dataModel.bookingId.value = bookingId
                                findNavController().navigate(ClinicReservationFragmentDirections.actionClinicReservationToClinicReservationCompleteFragment())
                            } else {
                                startActivity(
                                    Intent(
                                        requireActivity(),
                                        ClinicReservationCompleteActivity::class.java
                                    ).apply {
                                        putExtra("bookingId", bookingId)
                                        putExtra("fromHome", true)
                                        putExtra("bookingTab", bookingTab)
                                    })
                            }
                        } else {
                            AppToast(requireContext()).showToastMessage(
                                message.toString(),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )
                        }
                    } else {
                        if (event.code == "200") {
                            val message = JSONObject(event.response)["messageKey"]
                            Logger.d("error : ${message}")
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }

            TIME_TABLE_LIST_REQUEST -> {
                try {
                    if ("ok" == event.status) {
                        val code = JSONObject(event.response)["code"]
                        if ("SUCCESS" == code) {
                            resultData = JSONObject(event.response)["resultData"] as JSONObject
                            curFocusedGroupPosition = -1
                            roomAdapter.notifyDataSetChanged()

                            scrollView.setBackgroundResource(R.color.dark_grey)
                            layoutInfo.visibility = View.VISIBLE
                            layoutButton.visibility = View.VISIBLE
                        }
                    } else {
                        if (event.code == "200") {
                            val message = JSONObject(event.response)["messageKey"]
                            Logger.d("error : $message")
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }

            RESERVATION_CLINIC_INFO_REQUEST -> {
                try {
                    if ("ok" == event.status) {
                        val code = JSONObject(event.response)["code"]
                        if ("SUCCESS" == code) {
                            val obj = JSONObject(event.response)["resultData"] as JSONObject

                            val roomArray = obj["roomList"]
                            val rooms: MutableList<Room> = Gson().fromJson(roomArray.toString(), object : TypeToken<MutableList<Room>>() {}.type)
                            roomItems.clear()

                            val selectRoomList: MutableList<Room> = mutableListOf()
                            var clinicIdCount = false
                            for (item in rooms) {
                                if (item.animalList.any { it.name == petKind }) {
                                    for (id in clinicItems) {
                                        if (item.clinicList.any { it.clinicId == id }) {
                                            clinicIdCount = true
                                        } else {
                                            clinicIdCount = false
                                            break
                                        }
                                    }

                                    if (clinicIdCount) {
                                        selectRoomList.add(item)
                                    }
                                }
                            }
                            roomItems.addAll(selectRoomList)

                            bookingSetting = Gson().fromJson(obj["bookingSetting"].toString(), object : TypeToken<BookingSetting>() {}.type)

                            calendarAdapter = CalendarAdapter(baseCalendar).apply { setListener(this@ClinicReservationFragment) }
                            calendarAdapter.setBoodkingSetting(bookingSetting)
                            calendarList.apply {
                                layoutManager = GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
                                adapter = calendarAdapter
                            }
                        } else {
                            if (event.code == "200") {
                                val message = JSONObject(event.response)["messageKey"]
                                Logger.d("error : ${message}")
                            }
                        }
                    } else {
                        if (event.code == "200") {
                            val message = JSONObject(event.response)["messageKey"]
                            Logger.d("error : ${message}")
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }
        }
    }
    //===============================================================================================

    private fun warningCheckAll(status: Boolean) {
        if (status) {
            checkAgreee1.isSelected = true
            checkAgreee2.isSelected = true

            warningCheckValid[0] = true
            warningCheckValid[1] = true
        } else {
            checkAgreee1.isSelected = false
            checkAgreee2.isSelected = false

            warningCheckValid[0] = false
            warningCheckValid[1] = false
        }
    }

    private fun agreeCheck() {
        if (warningCheckValid[0] && warningCheckValid[1]) {
            checkAllAgree.isSelected = true
        } else {
            checkAllAgree.isSelected = false
        }
    }

    private fun validationReservation(): Boolean {
        val result = true

        if (!warningCheckValid[0] && !warningCheckValid[1]) {
            AppToast(requireContext()).showToastMessage(
                "병원예약을 위한 안내사항 및 제공동의를 동의해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        if (!warningCheckValid[0]) {
            AppToast(requireContext()).showToastMessage(
                "올바른 병원예약 에티켓을 위한 안내사항을 동의해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        if (!warningCheckValid[1]) {
            AppToast(requireContext()).showToastMessage(
                "제 3자 제공동의를 동의해주세요",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        if (selectedDate.text.toString().isEmpty()) {
            AppToast(requireContext()).showToastMessage(
                "날짜를 선택해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        if (selectedTime.text.toString().isEmpty()) {
            AppToast(requireContext()).showToastMessage(
                "시간을 선택해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        return result
    }

    //==============================================================================================
    inner class RoomAdapter: RecyclerView.Adapter<RoomHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RoomHolder(layoutInflater.inflate(R.layout.adapter_clinic_reservation_room_item, parent, false))

        override fun onBindViewHolder(holder: RoomHolder, position: Int) {
            val room = roomItems[position]
            holder.setImage(room.imgUrl)
            holder.setName(room.vetName)
            holder.setRoomNumber(room.roomOrder)

            val items = resultData?.get(room.clinicRoomId.toString()) as JSONArray
            if (items.length() > 0) {
                holder.itemView.timeTableList.visibility = View.VISIBLE
                holder.itemView.layoutStatus.visibility = View.INVISIBLE

                val timeSlots: MutableList<TimeTable> = Gson().fromJson(items.toString(), object : TypeToken<MutableList<TimeTable>>() {}.type)
                holder.itemView.timeTableList.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    if (itemDecorationCount < 1) {
                        addItemDecoration(HorizontalMarginItemDecoration(
                            margin20, margin14, margin20
                        ))
                    }

                    adapter = TimeAdapter(
                        items = timeSlots,
                        groupPosition = position,
                        // TimeTable 시간 선택 Event
                        timeClickListener = { groupPosition ->
                            if (curFocusedGroupPosition != groupPosition) {
                                holder.itemView.viewCheck.visibility = View.VISIBLE
                                holder.itemView.btnCheck.visibility = View.VISIBLE
                                if (curFocusedGroupPosition != -1) {
                                    notifyItemChanged(curFocusedGroupPosition)
                                }
                                curFocusedGroupPosition = groupPosition

                                FirebaseAPI(requireActivity()).logEventFirebase(
                                    "병원예약_날짜시간선택",
                                    "Click Event",
                                    "병원예약 페이지에서 날짜시간선택 버튼 클릭"
                                )
                                clinicRoomId = room.clinicRoomId
                            }
                        })
                }
            } else {
                holder.itemView.timeTableList.visibility = View.GONE
                holder.itemView.layoutStatus.visibility = View.VISIBLE
            }

            if (curFocusedGroupPosition != position) {
                holder.itemView.viewCheck.visibility = View.GONE
                holder.itemView.btnCheck.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = roomItems.size
    }

    inner class RoomHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(_url: String?) {
            if (_url.isNullOrBlank()) {
                item.image.visibility = View.GONE
            } else {
                item.image.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                    .apply(RequestOptions.circleCropTransform())
                    .into(item.image)
            }
        }

        fun setName(_name: String?) {
            item.name.text = _name
        }

        fun setRoomNumber(_number: Int) {
            item.roomNumber.text = "${_number}번 진료실"
        }
    }

    //==============================================================================================
    inner class TimeAdapter(
        private val items: List<TimeTable>,
        private val groupPosition: Int,
        private val timeClickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<TimeHolder>() {
        private var prevSelectedTimePosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TimeHolder(layoutInflater.inflate(R.layout.adapter_time_table_item, parent, false))

        override fun onBindViewHolder(holder: TimeHolder, position: Int) {
            when {
                (prevSelectedTimePosition == position) -> {
                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
                }
                items[position].possible -> {
                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
                }
                else -> {
                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey2))
                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_disable)
                }
            }

            holder.itemView.timeTable.text = items[position].startTime
            holder.itemView.setOnClickListener {
                if (items[position].possible.not()) {
                    return@setOnClickListener
                }

                selectedTime.text = items[position].startTime
                bookingTime = items[position].startTime
                val hour = items[position].startTime.split(":")[0]
                if (hour.toInt() >= 12) {
                    aMpM.text = "PM"
                } else {
                    aMpM.text = "AM"
                }

                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
                timeClickListener.invoke(groupPosition)

                if (prevSelectedTimePosition != -1) {
                    notifyItemChanged(prevSelectedTimePosition)
                }
                prevSelectedTimePosition = position
            }
        }

        override fun getItemCount(): Int = items.size
    }

    inner class TimeHolder(item: View) : RecyclerView.ViewHolder(item)

    //==============================================================================================
    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.checkAllAgree -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckAll(true)
                } else {
                    warningCheckAll(false)
                }
            }

            R.id.checkAgreee1 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[0] = true
                } else {
                    warningCheckValid[0] = false
                }

                agreeCheck()
            }

            R.id.checkAgreee2 -> {
                it.isSelected = !it.isSelected

                if (it.isSelected) {
                    warningCheckValid[1] = true
                } else {
                    warningCheckValid[1] = false
                }

                agreeCheck()
            }

            R.id.textViewAgreeDesc2 -> {
                if (!fromHome) {
                    bundleOf("url" to "https://auth.petdoc.co.kr/policyType3").let {
                        findNavController().navigate(R.id.action_clinicReservation_to_bookingPolicyFragment, it)
                    }
                } else {
                    startActivity(Intent(requireActivity(), BookingPolicyActivity::class.java).apply {
                        putExtra("url", "https://auth.petdoc.co.kr/policyType3")
                    })
                }
            }
        }
    }
}