package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_hotel_room_item.view.*
import kotlinx.android.synthetic.main.fragment_hotel_room_select.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.HotelReservationCompleteActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Petdoc
 * Class: HotelRoomSelectFragment
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class HotelRoomSelectFragment : BaseFragment() {

    private val TAG = "HotelRoomSelectFragment"
    private val ROOM_LIST_REQUEST = "$TAG.roomListRequest"
    private val RESERVATION_REQUEST = "$TAG.reservationRequest"
    private val RESERVATION_HOTEL_INFO_REQUEST = "$TAG.reservationHotelInfoRequest"

    private lateinit var dataModel:HospitalDataModel

    private lateinit var roomAdapter:RoomAdapter
    private var roomItems:MutableList<Any> = mutableListOf()

    private var warningCheckValid = booleanArrayOf(false, false)
    private var resultData: JSONObject? = null


    // Activity 에서 진입하는 경우 사용하는 변수들...
    private var fromHome = false
    private var centerId = -1
    private var petId = -1
    private var hospitalName = ""

    private var enterDate = ""
    private var outDate = ""
    private var enterTime = ""
    private var outTime = ""

    private var feedType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hotel_room_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        fromHome = arguments?.getBoolean("fromHome")!!

        if (!fromHome) {
            centerId = dataModel.centerId.value!!
            petId = dataModel.petId.value!!
            enterDate = dataModel.enterDate.value!!
            outDate = dataModel.outDate.value!!
            enterTime = dataModel.enterTime.value!!
            outTime = dataModel.outTime.value!!

            registerName.text = dataModel.name.value!!.toString()
        } else {
            centerId = arguments?.getInt("centerId")!!
            petId = arguments?.getInt("petId", petId)!!
            hospitalName = arguments?.getString("name")!!
            enterDate = arguments?.getString("enterDate")!!
            outDate = arguments?.getString("outDate")!!
            enterTime = arguments?.getString("enterTime")!!
            outTime = arguments?.getString("outTime")!!

            registerName.text = hospitalName

            //TODO : 호텔 정보 API 호출
        }

        //==========================================================================================
        btnBack.setOnClickListener{ requireActivity().onBackPressed() }
        btnReservatioCompleted.setOnClickListener {
            if (validationReservation()) {

            }
        }

        checkAllAgree.setOnClickListener(clickListener)
        checkAgreee1.setOnClickListener(clickListener)
        checkAgreee2.setOnClickListener(clickListener)

        defaultFeed.setOnClickListener {
            feedType = "기존사료"
        }

        hotelFeed.setOnClickListener {
            feedType = "호텔사료"
        }

        //==========================================================================================
        roomAdapter = RoomAdapter()
        roomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(ROOM_LIST_REQUEST)) {
            mApiClient.cancelRequest(ROOM_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_REQUEST)
        }

        if (mApiClient.isRequestRunning(RESERVATION_HOTEL_INFO_REQUEST)) {
            mApiClient.cancelRequest(RESERVATION_HOTEL_INFO_REQUEST)
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            RESERVATION_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val message = JSONObject(event.response)["messageKey"]
                    if ("SUCCESS" == code) {
                        if (!fromHome) {
                            startActivity(Intent(requireActivity(), HotelReservationCompleteActivity::class.java).apply {
                                putExtra("bookingId", 100)
                                putExtra("fromHome", true)
                            })
                        } else {
                            dataModel.bookingId.value = 100
                            findNavController().navigate(HotelRoomSelectFragmentDirections.actionHotelRoomSelectFragmentToHotelReservationCompleteFragment())
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

            ROOM_LIST_REQUEST ->{

            }

            RESERVATION_HOTEL_INFO_REQUEST -> {

            }
        }
    }

    //==============================================================================================
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

        return result
    }

    //=============================================================================================
    private var selectedRoomPosition = -1

    inner class RoomAdapter : RecyclerView.Adapter<RoomHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RoomHolder(layoutInflater.inflate(R.layout.adapter_hotel_room_item, parent, false))

        override fun onBindViewHolder(holder: RoomHolder, position: Int) {
            if (selectedRoomPosition == position) {
                holder.itemView.btnCheck.visibility = View.VISIBLE
                holder.itemView.viewCheck.visibility = View.VISIBLE
                holder.itemView.setBackgroundResource(R.color.grey_alpha_50)
            } else {
                holder.itemView.btnCheck.visibility = View.GONE
                holder.itemView.viewCheck.visibility = View.GONE
                holder.itemView.setBackgroundResource(R.color.white)
            }

            holder.itemView.setOnClickListener {
                holder.itemView.btnCheck.visibility = View.VISIBLE
                holder.itemView.viewCheck.visibility = View.VISIBLE
                holder.itemView.setBackgroundResource(R.color.grey_alpha_50)

                selectedRoomPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = 2
    }

    inner class RoomHolder(var item: View) : RecyclerView.ViewHolder(item) {

    }

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