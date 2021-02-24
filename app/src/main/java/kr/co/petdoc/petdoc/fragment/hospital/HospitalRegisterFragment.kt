package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.adapter_clinic_room_item.view.*
import kotlinx.android.synthetic.main.adapter_register_clinic_type.view.*
import kotlinx.android.synthetic.main.fragment_hospital_register.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.hospital.RegisterCompleteActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.enum.RoomStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.CenterClinic
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.network.response.submodel.Room
import kr.co.petdoc.petdoc.network.response.submodel.RoomInfoAnimal
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: HospitalRegisterFragment
 * Created by kimjoonsung on 2020/06/15.
 *
 * Description :
 */
class HospitalRegisterFragment : BaseFragment() {

    private val TAG = "HospitalRegisterFragment"
    private val REGISTER_INFO_REQUEST = "${TAG}.registerInfoRequest"
    private val REGISTER_REQUEST = "$TAG.registerRequest"

    private lateinit var dataModel: HospitalDataModel
    private var mPetInfoItems: List<PetInfoItem> = listOf()
    private lateinit var petAdapter:PetAdapter

    private var roomItems: MutableList<Room> = mutableListOf()
    private lateinit var roomAdapter:RoomAdapter

    private var clinicItems:MutableList<CenterClinic> = mutableListOf()
    private lateinit var clinicAdapter:ClinicAdapter

    private var roomStatusMap:JSONObject? = null
    private var roomClinicMap:JSONObject? = null

    private var margin20 = 0
    private var margin11 = 0

    private var warningCheckValid = booleanArrayOf(false, false)

    private var hospitalName = ""
    private var hospitalLocation = ""
    private var fromHome = false

    private var centerId = -1
    private var petId = -1
    private var clinicRoomId = -1
    private var clinicIdList : MutableList<Int> = mutableListOf()
    private var roomAnimalItems : MutableList<RoomInfoAnimal> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin11 = Helper.convertDpToPx(requireContext(), 11f).toInt()

        hospitalName = arguments?.getString("name") ?: hospitalName
        hospitalLocation = arguments?.getString("location") ?: hospitalLocation
        centerId = arguments?.getInt("centerId")!!
        fromHome = arguments?.getBoolean("fromHome", fromHome)!!

        nameTxt.text = hospitalName
        registerName.text = hospitalName
        locationTxt.text = hospitalLocation

        //===========================================================================================

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnRegisterCompleted.setOnClickListener {
            if (validationRegister()) {
                mApiClient.postHospitalClinicRegisterRequest(
                    REGISTER_REQUEST,
                    centerId,
                    clinicRoomId,
                    petId,
                    editMessage.text.toString(),
                    clinicIdList
                )
            }

//            if (!fromHome) {
//                dataModel.bookingId.value = 103
//                findNavController().navigate(HospitalRegisterFragmentDirections.actionHospitalRegisterFragmentToRegisterCompleteFragment())
//            } else {
//                startActivity(Intent(requireActivity(), RegisterCompleteActivity::class.java).apply {
//                    putExtra("bookingId", 100)
//                })
//
//            }
        }

        btnInfo.setOnClickListener {
            ClinicStatusDialogFragment().show(childFragmentManager, ClinicStatusDialogFragment().tag)
        }

        checkAllAgree.setOnClickListener(clickListener)
        checkAgreee1.setOnClickListener(clickListener)
        checkAgreee2.setOnClickListener(clickListener)

        //==========================================================================================

        petAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }

            adapter = petAdapter
        }

        clinicAdapter = ClinicAdapter()
        clinicList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
            adapter = clinicAdapter
        }

        roomAdapter = RoomAdapter()
        clinicRoomList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = roomAdapter
        }

        //==========================================================================================

        loadPetInfo()
        mApiClient.getHospitalRegisterInfo(REGISTER_INFO_REQUEST, centerId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(REGISTER_INFO_REQUEST)) {
            mApiClient.cancelRequest(REGISTER_INFO_REQUEST)
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
            REGISTER_REQUEST -> {
                if (event.status == "ok") {
                    val code = JSONObject(event.response)["code"]
                    val message = JSONObject(event.response)["messageKey"]
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject
                        val registerId = obj["id"] as Int
                        if (!fromHome) {
                            dataModel.registerId.value = registerId
                            findNavController().navigate(HospitalRegisterFragmentDirections.actionHospitalRegisterFragmentToRegisterCompleteFragment())
                        } else {
                            startActivity(Intent(requireActivity(), RegisterCompleteActivity::class.java).apply {
                                putExtra("registerId", registerId)
                            })
                        }
                    } else {
                        Logger.d("error : ${message}")
                    }
                }
            }

            REGISTER_INFO_REQUEST -> {
                if (event.status == "ok") {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject

                        // clinic list parsing
                        val clinicArray = obj["centerClinicList"]
                        val items:MutableList<CenterClinic> = Gson().fromJson(clinicArray.toString(), object : TypeToken<MutableList<CenterClinic>>() {}.type)
                        clinicItems.clear()
                        clinicItems.addAll(items)
                        clinicAdapter.notifyDataSetChanged()

                        // room list parsing
                        val roomArray = obj["roomList"]
                        val datas:MutableList<Room> = Gson().fromJson(roomArray.toString(), object : TypeToken<MutableList<Room>>() {}.type)
                        roomItems.clear()
                        roomItems.addAll(datas)
                        roomAdapter.notifyDataSetChanged()

                        roomStatusMap = obj["roomStatusMap"] as JSONObject
                        roomClinicMap = obj["roomClinicMap"] as JSONObject

                        Logger.d("roomStatusMap :$roomStatusMap, roomClinicMap : $roomClinicMap")
                    } else {
                        val message = JSONObject(event.response)["messageKey"]
                        Logger.d("error : ${message}")
                    }
                }
            }
        }
    }

    //=============================================================================================
    private fun onItemClicked(item: PetInfoItem) {
        Logger.d("pet kind : ${item.kind}")
        if (roomAnimalItems.any { it.name == item.kind}) {
            layoutClinicPet.visibility = View.VISIBLE
            layoutButton.visibility = View.VISIBLE
            noClinicPetTxt.visibility = View.GONE
            layoutRegisterInfo.setBackgroundResource(R.drawable.white_bottom_round_35)

            petId = item.id
            Logger.d("pet id : ${petId}")
        } else {
            layoutClinicPet.visibility = View.GONE
            layoutButton.visibility = View.GONE
            noClinicPetTxt.visibility = View.VISIBLE
            layoutRegisterInfo.setBackgroundResource(R.color.white)
        }
    }

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

    private fun checkingRoom(roomId: String) : Boolean {
        val clinic = roomClinicMap?.get(roomId) as JSONArray
        val roomClinic:MutableList<Int> = Gson().fromJson(clinic.toString(), object : TypeToken<MutableList<Int>>() {}.type)

        return roomClinic.containsAll(clinicIdList)
    }

    private fun validationRegister(): Boolean {
        val result = true

        if (clinicIdList.size == 0) {
            AppToast(requireContext()).showToastMessage(
                "진료항목을 선택해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

        if (clinicRoomId == -1) {
            AppToast(requireContext()).showToastMessage(
                "진료실을 선택해주세요.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            return false
        }

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

    //==============================================================================================

    inner class PetAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val TYPE_ITEM = 0
        val TYPE_FOOTER = 1

        var selectedPosition = 0

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
                        holder.itemView.border.visibility = View.VISIBLE
                    } else {
                        holder.itemView.border.visibility = View.GONE
                    }

                    (holder as PetHolder).setName(mPetInfoItems[position].name!!)
                    holder.setImage(mPetInfoItems[position].imageUrl!!)

                    if (position == 0) {
                        (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                            leftMargin = margin20
                        }
                    } else if (position == itemCount - 1) {
                        (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                            rightMargin = margin20
                        }
                    } else {
                        (holder.itemView.itemRoot.layoutParams as RecyclerView.LayoutParams).apply {
                            leftMargin = margin11
                        }
                    }

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
                            leftMargin = margin11
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
    inner class RoomAdapter : RecyclerView.Adapter<RoomHolder>() {
        private var seletedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RoomHolder(layoutInflater.inflate(R.layout.adapter_clinic_room_item, parent, false))

        override fun onBindViewHolder(holder: RoomHolder, position: Int) {
            holder.setImage(roomItems[position].imgUrl)
            holder.setName(roomItems[position].vetName)
            holder.setRoomNumber(roomItems[position].roomOrder)
            holder.setRoomStatus(roomItems[position].clinicRoomId.toString())

            roomAnimalItems.addAll(roomItems[position].animalList)

            if (seletedPosition == position) {
                holder.itemView.viewCheck.visibility = View.VISIBLE
                holder.itemView.btnCheck.visibility = View.VISIBLE

                holder.itemView.setBackgroundResource(R.color.grey_alpha_50)
            } else {
                holder.itemView.viewCheck.visibility = View.GONE
                holder.itemView.btnCheck.visibility = View.GONE

                holder.itemView.setBackgroundResource(R.color.white)
            }

            holder.itemView.setOnClickListener {
                when (roomStatusMap?.get(roomItems[position].clinicRoomId.toString())) {
                    RoomStatus.SMOOTH.name,
                    RoomStatus.BUSY.name,
                    RoomStatus.NORMAL.name -> {
                        clinicRoomId = roomItems[position].clinicRoomId
                        if (checkingRoom(roomItems[position].clinicRoomId.toString())) {
                            holder.itemView.viewCheck.visibility = View.VISIBLE
                            holder.itemView.btnCheck.visibility = View.VISIBLE
                            holder.itemView.setBackgroundResource(R.color.grey_alpha_50)

                            seletedPosition = position
                            notifyDataSetChanged()
                        } else {
                            AppToast(requireContext()).showToastMessage(
                                "진료 항목이 일치하지 않습니다.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )
                        }
                    }

                    else -> {
                        AppToast(requireContext()).showToastMessage(
                            "진료 시간이 아니거나 휴진입니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }

        override fun getItemCount() = roomItems.size
    }

    inner class RoomHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(_url: String?) {
            if (_url != null) {
                item.image.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                    .apply(RequestOptions.circleCropTransform())
                    .into(item.image)
            } else {
                item.image.visibility = View.VISIBLE
            }
        }

        fun setName(_name: String?) {
            item.name.text = _name
        }

        fun setRoomNumber(_number: Int) {
            item.roomNumber.text = "${_number}번 진료실"
        }

        fun setRoomStatus(_roomId: String) {
            val status = roomStatusMap?.get(_roomId)
            item.roomStatus.apply {
                when (status) {
                    RoomStatus.BUSY.name -> {
                        text = RoomStatus.BUSY.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.orange_circle)
                    }

                    RoomStatus.NORMAL.name -> {
                        text = RoomStatus.NORMAL.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.room_status_normal_circle)
                    }

                    RoomStatus.RECESS.name -> {
                        text = RoomStatus.RECESS.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.light_grey3_circle)
                    }

                    RoomStatus.REG_CANT.name -> {
                        text = RoomStatus.REG_CANT.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.light_grey3_circle)
                    }

                    RoomStatus.REG_END.name -> {
                        text = RoomStatus.REG_END.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.light_grey3_circle)
                    }

                    RoomStatus.SMOOTH.name -> {
                        text = RoomStatus.SMOOTH.text
                        item.roomStatusColor.setBackgroundResource(R.drawable.room_status_smooth_circle)
                    }
                }
            }
        }
    }

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
            }
        }

        override fun getItemCount() = clinicItems.size
    }

    inner class ClinicHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setClinicName(_clinic: String) {
            item.clinicName.text = _clinic
        }
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