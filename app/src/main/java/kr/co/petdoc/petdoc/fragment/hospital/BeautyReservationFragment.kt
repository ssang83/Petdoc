package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_time_table_item.view.*
import kotlinx.android.synthetic.main.fragment_beauty_reservation.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.BeautyReservationCompleteActivity
import kr.co.petdoc.petdoc.adapter.hospital.CalendarAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.BookingSetting
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
 * Class: BeautyReservationFragment
 * Created by kimjoonsung on 2020/06/18.
 *
 * Description :
 */
class BeautyReservationFragment : BaseFragment(), CalendarAdapter.AdapterListener {

    private val TAG = "BeautyReservationFragment"
    private val TIME_TABLE_LIST_REQUEST = "$TAG.timeTableListRequest"
    private val RESERVATION_REQUEST = "$TAG.reservationRequest"
    private val RESERVATION_BEAUTY_INFO_REQUEST = "$TAG.reservationBeautyInfoRequest"

    private lateinit var dataModel: HospitalDataModel

    private lateinit var timeAdapter: TimeAdapter
    private var timeTableItems:MutableList<TimeTable> = mutableListOf()


    private lateinit var calendarAdapter: CalendarAdapter
    private val baseCalendar = BaseCalendar()

    private var margin20 = 0
    private var margin14 = 0

    private var warningCheckValid = booleanArrayOf(false, false)

    private var resultData: JSONObject? = null
    private var bookingSetting: BookingSetting? = null

    // Activity 에서 진입하는 경우 사용하는 변수들...
    private var fromHome = false
    private var centerId = -1
    private var petId = -1
    private var message = ""
    private var hospitalName = ""

    private var bookingDay = ""
    private var bookingTime = ""
    private var currentMonth = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_beauty_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        fromHome = arguments?.getBoolean("fromHome")!!

        if (!fromHome) {
            bookingSetting = dataModel.bookingSetting.value!!
            centerId = dataModel.centerId.value!!
            petId = dataModel.petId.value!!
            message = dataModel.message.value!!

            registerName.text = dataModel.name.value!!.toString()
        } else {
            centerId = arguments?.getInt("centerId")!!
            petId = arguments?.getInt("petId", petId)!!
            message = arguments?.getString("msg")!!
            hospitalName = arguments?.getString("name")!!

            registerName.text = hospitalName

            mApiClient.getHospitalReservationClinicInfo(RESERVATION_BEAUTY_INFO_REQUEST, centerId)
        }

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin14 = Helper.convertDpToPx(requireContext(), 14f).toInt()

        //============================================================================================
        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnReservatioCompleted.setOnClickListener {
            if (validationReservation()) {

            }
        }

        checkAllAgree.setOnClickListener(clickListener)
        checkAgreee1.setOnClickListener(clickListener)
        checkAgreee2.setOnClickListener(clickListener)

        btnNextMonth.setOnClickListener { calendarAdapter.changeToNextMonth() }
        btnBeforeMonth.setOnClickListener { calendarAdapter.changeToPrevMonth() }

        //==========================================================================================
        timeAdapter = TimeAdapter()
        timeTableList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = timeAdapter
        }

        baseCalendar.initBaseCalendar {
            val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
            header.text = sdf.format(it.time)

            val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
            currentMonth = dayFormat.format(it.time)

            Logger.d("current month : $currentMonth")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(TIME_TABLE_LIST_REQUEST)) {
            mApiClient.cancelRequest(TIME_TABLE_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_BEAUTY_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_BEAUTY_INFO_REQUEST)
        }
    }

    override fun onDateClicked(date: String) {
        bookingDay = "${currentMonth}-${date}"

        val date = SimpleDateFormat("yyyyMMdd", Locale.KOREAN).parse(bookingDay.replace("-", ""))
        val transDate = SimpleDateFormat("yyyy년 MM월 dd일 EE", Locale.KOREAN).format(date!!)
        selectedDate.text = transDate.substring(6)

        Logger.d("booking day : $bookingDay, selectedDate : ${transDate.substring(7)}")
        mApiClient.getTimeTableList(TIME_TABLE_LIST_REQUEST, bookingSetting?.centerId!!, bookingDay)
    }

    override fun refreshCurrentMonth(calendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
        header.text = sdf.format(calendar.time)

        val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
        currentMonth = dayFormat.format(calendar.time)
        Logger.d("current month : $currentMonth")
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
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val message = JSONObject(event.response)["messageKey"]
                    if ("SUCCESS" == code) {
                        if (!fromHome) {
                            dataModel.bookingId.value = 100
                            findNavController().navigate(BeautyReservationFragmentDirections.actionBeautyReservationFragmentToBeautyReservationCompleteFragment())
                        } else {
                            startActivity(Intent(requireActivity(), BeautyReservationCompleteActivity::class.java).apply {
                                putExtra("bookingId", 100)
                                putExtra("fromHome", true)
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
            }

            TIME_TABLE_LIST_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        resultData = JSONObject(event.response)["resultData"] as JSONObject
                        val items = resultData?.get("1") as JSONArray
                        timeTableItems = Gson().fromJson(items.toString(), object : TypeToken<MutableList<TimeTable>>() {}.type)
                        timeAdapter.notifyDataSetChanged()

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
            }

            RESERVATION_BEAUTY_INFO_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject
                        bookingSetting = Gson().fromJson(
                            obj["bookingSetting"].toString(),
                            object : TypeToken<BookingSetting>() {}.type
                        )

                        calendarAdapter =
                            CalendarAdapter(baseCalendar).apply { setListener(this@BeautyReservationFragment) }
                        calendarAdapter.setBoodkingSetting(bookingSetting)
                        calendarList.apply {
                            layoutManager =
                                GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
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

        if (!warningCheckValid[0] || !warningCheckValid[1]) {
            AppToast(requireContext()).showToastMessage(
                "안내사항을 확인 및 동의해주세요.",
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
    private var selectedTimePosition = -1

    inner class TimeAdapter : RecyclerView.Adapter<TimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TimeHolder(layoutInflater.inflate(R.layout.adapter_time_table_item, parent, false))

        override fun onBindViewHolder(holder: TimeHolder, position: Int) {
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

            if (selectedTimePosition == position) {
                holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.white))
                holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_select)
            } else {
                if (timeTableItems[position].possible) {
                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey))
                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_default)
                } else {
                    holder.itemView.timeTable.setTextColor(Helper.readColorRes(R.color.light_grey2))
                    holder.itemView.timeTable.setBackgroundResource(R.drawable.bg_time_table_disable)
                }
            }

            holder.itemView.timeTable.text = timeTableItems[position].startTime

            holder.itemView.setOnClickListener {
                if (timeTableItems[position].possible) {
                    selectedTime.text = timeTableItems[position].startTime
                    bookingTime = timeTableItems[position].startTime
                    val hour = timeTableItems[position].startTime.split(":")[0]
                    if (hour.toInt() >= 12) {
                        aMpM.text = "PM"
                    } else {
                        aMpM.text = "AM"
                    }

                    selectedTimePosition = position
                    notifyDataSetChanged()

                    Logger.d("bookingTime : $bookingTime")
                }
            }
        }

        override fun getItemCount() = timeTableItems.size
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
        }
    }
}