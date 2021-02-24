package kr.co.petdoc.petdoc.fragment

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.aramhuvis.lite.base.ACamera
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kotlinx.android.synthetic.main.fragment_care.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.ExternalDeviceDetailActivity
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.PetInfomationActivity
import kr.co.petdoc.petdoc.activity.SimpleWebViewActivity
import kr.co.petdoc.petdoc.activity.SimpleWebViewActivity.Companion.KEY_URL
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.care.*
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.db.care.body.TemperatureDB
import kr.co.petdoc.petdoc.db.care.humidity.HumidityDB
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImageDB
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.event.ScannerDisconnectedEvent
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.CareRecordListResponse
import kr.co.petdoc.petdoc.network.response.submodel.CareRecordData
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.scanner.FirmwareVersionState
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*


/**
 * Petdoc
 * Class: PetCareFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 펫 케어 화면
 */
class PetCareFragment : Fragment() {

    private val TAG = "PetCareFragment"
    private val CARE_RECORD_LIST_REQUEST = "$TAG.careRecordListRequest"
    private val CARE_TEMPERATURE_UPLOAD_REQUEST = "$TAG.temperatureUploadRequest"
    private val CARE_TEMPERATURE_INJECT_UPLOAD_REQUEST = "$TAG.temperatureInjectUploadRequest"
    private val CARE_TEMPERATURE_DELETE_REQUEST = "$TAG.temperatureDeleteRequest"
    private val CARE_HUMIDITY_UPLOAD_REQUEST = "$TAG.humidityUploadRequest"
    private val CARE_HUMIDITY_DELETE_REQUEST = "$TAG.humidityDeleteRequest"
    private val CARE_EAR_IMAGE_UPLOAD_REQUEST = "$TAG.earImageUploadRequest"

    private lateinit var mAdapter: PetAdapter

    private lateinit var mEventBus: EventBus

    private var records:MutableList<CareRecordData> = mutableListOf()

    private var margin20 = 0
    private var margin4 = 0

    private var cdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
    private var checkDate = ""
    private var petId = -1
    private var petKind = ""
    private var petImageUrl = ""

    private var fecesData: CareRecordData? = null
    private var urineData:CareRecordData? = null
    private var weightData:CareRecordData? = null
    private var clinicData: CareRecordData? = null
    private var clinicContentData:CareRecordData? = null
    private var memoData:CareRecordData? = null
    private var walkData: CareRecordData? = null
    private var toothData:CareRecordData? = null
    private var bathData:CareRecordData? = null
    private var loveData: CareRecordData? = null

    private lateinit var temperatureDb : TemperatureDB
    private lateinit var humidityDB: HumidityDB
    private lateinit var earImageDB:EarImageDB

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    protected lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)
    private val scanner: Scanner by inject()
    private val mApiClient: ApiClient = PetdocApplication.application.apiClient

    private var userId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_care, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("tab", "click", "care", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("펫닥_케어_홈", "Page View", "케어탭 선택 화면 page view")

        userId = if(StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }

        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = requireContext().isInternetConnected()

        mEventBus = EventBus.getDefault()

        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin4 = Helper.convertDpToPx(requireContext(), 4f).toInt()

        btnCareHistory.setOnSingleClickListener(clickListener)
        layoutWalk.setOnSingleClickListener(clickListener)
        layoutTooth.setOnSingleClickListener(clickListener)
        layoutBath.setOnSingleClickListener(clickListener)
        layoutPoop.setOnSingleClickListener(clickListener)
        layoutWeight.setOnSingleClickListener(clickListener)
        layoutClinic.setOnSingleClickListener(clickListener)
        layoutMemo.setOnSingleClickListener(clickListener)
        layoutScannerConnect.setOnSingleClickListener(clickListener)
        layoutHealthCare.setOnSingleClickListener(clickListener)
        layoutCommunion.setOnSingleClickListener(clickListener)
        layoutWeight1.setOnSingleClickListener(clickListener)
        layoutTemperture.setOnSingleClickListener(clickListener)
        layoutEar.setOnSingleClickListener(clickListener)
        layoutHumidity.setOnSingleClickListener(clickListener)

        layoutScannerTitle.setOnSingleClickListener {
            requireContext().startActivity<SimpleWebViewActivity> {
                putExtra(KEY_URL, "https://partner.petdoc.co.kr/scanner/use")
            }
        }

        petImage.setOnSingleClickListener {
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

        btnOffScanner.setOnSingleClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle("펫닥 스캐너 연결됨")
                setMessage("현재 펫닥 스캐너가 연결되어 있습니다.\n연결을 해제하시겠습니까?")
                setCancelable(true)
                setConfirmButton("해제", View.OnClickListener {
                    scanner.disconnect()
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        // =======================================================================================
        mAdapter = PetAdapter()
        petList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }

            adapter = mAdapter

            addItemDecoration(HorizontalMarginItemDecoration(
                marginStart = margin20,
                marginBetween = margin4,
                marginEnd = margin20
            ))
        }

        initCalendar()
    }

    override fun onResume() {
        super.onResume()
        loadPetInfo()

        imageViewEar.isEnabled = false
        imageViewTemp.isEnabled = false
        imageViewHumidity.isEnabled = false
        tv_ear.isEnabled = false
        tv_temp.isEnabled = false
        tv_humidity.isEnabled = false
        if (!StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS)) {
            layoutScannerConnect.visibility = View.VISIBLE
            scannerConnectionStateLayout.visibility = View.GONE
        } else {
            layoutScannerConnect.visibility = View.GONE
            scannerConnectionStateLayout.visibility = View.VISIBLE

            when (scanner.getCurrentLenseType()) {
                ACamera.LENSE_TYPE.BODY_TEMP -> {
                    imageViewTemp.isEnabled = true
                    tv_temp.isEnabled = true
                }
                ACamera.LENSE_TYPE.EAR -> {
                    imageViewEar.isEnabled = true
                    tv_ear.isEnabled = true
                }
                ACamera.LENSE_TYPE.TEMP -> {
                    imageViewHumidity.isEnabled = true
                    tv_humidity.isEnabled = true
                }
            }
        }

        scanner.reBindProcessWithScannerNetwork()
    }

    override fun onPause() {
        super.onPause()
        scanner.releaseNetworkBinding()
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CARE_RECORD_LIST_REQUEST)) {
            mApiClient.cancelRequest(CARE_RECORD_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_TEMPERATURE_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_TEMPERATURE_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_HUMIDITY_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_HUMIDITY_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_EAR_IMAGE_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_EAR_IMAGE_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_TEMPERATURE_INJECT_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(CARE_TEMPERATURE_INJECT_UPLOAD_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_TEMPERATURE_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_TEMPERATURE_DELETE_REQUEST)
        }

        if (mApiClient.isRequestRunning(CARE_HUMIDITY_DELETE_REQUEST)) {
            mApiClient.cancelRequest(CARE_HUMIDITY_DELETE_REQUEST)
        }

        EarImageDB.destroyInstance()
        TemperatureDB.destroyInstance()
        HumidityDB.destroyInstance()

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
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            CARE_RECORD_LIST_REQUEST -> {
                if (response is CareRecordListResponse) {
                    records.clear()
                    records.addAll(response.careRecord.filter {
                        it.logDate?.replace("-", "").equals(checkDate)
                    })

                    clearRecord()
                    checkRecord(records)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ScannerDisconnectedEvent) {
        try {
            layoutScannerConnect.visibility = View.VISIBLE
            scannerConnectionStateLayout.visibility = View.GONE
            imageViewEar.isEnabled = false
            imageViewTemp.isEnabled = false
            imageViewHumidity.isEnabled = false
            tv_ear.isEnabled = false
            tv_temp.isEnabled = false
            tv_humidity.isEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ScannerEvent) {
        try {
            if (event.batteryStatus != -1) {
                if (event.scannerStatus) {
                    Airbridge.trackEvent("care", "click", "connect_done", null, null, null)

                    layoutScannerConnect.visibility = View.GONE
                    scannerConnectionStateLayout.visibility = View.VISIBLE

                    checkScannerBattery(event.batteryStatus)

                    when (event.lenseType) {
                        ACamera.LENSE_TYPE.BODY_TEMP -> {
                            imageViewTemp.isEnabled = true
                            tv_temp.isEnabled = true
                        }
                        ACamera.LENSE_TYPE.EAR -> {
                            imageViewEar.isEnabled = true
                            tv_ear.isEnabled = true
                        }
                        ACamera.LENSE_TYPE.TEMP -> {
                            imageViewHumidity.isEnabled = true
                            tv_humidity.isEnabled = true
                        }
                        ACamera.LENSE_TYPE.NONE -> {
                            imageViewEar.isEnabled = false
                            imageViewTemp.isEnabled = false
                            imageViewHumidity.isEnabled = false
                            tv_ear.isEnabled = false
                            tv_temp.isEnabled = false
                            tv_humidity.isEnabled = false
                        }
                    }
                } else {
                    layoutScannerConnect.visibility = View.VISIBLE
                    scannerConnectionStateLayout.visibility = View.GONE

                    imageViewEar.isEnabled = false
                    imageViewTemp.isEnabled = false
                    imageViewHumidity.isEnabled = false
                    tv_ear.isEnabled = false
                    tv_temp.isEnabled = false
                    tv_humidity.isEnabled = false
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        petId = -1
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            mPetInfoItems = items
            if(mPetInfoItems.isNotEmpty()) {
                loginLayout.visibility = View.VISIBLE
                notLoginLayout.visibility = View.GONE

                mAdapter.notifyDataSetChanged()

                if(petId == -1) {
                    petId = mPetInfoItems[0].id
                    petKind = mPetInfoItems[0].kind!!
                    petImageUrl = mPetInfoItems[0].imageUrl!!

                    checkPetKind(mPetInfoItems[0])
                    checkDate = cdf.format(Date()).replace("-", "")
                    mApiClient.getCareRecordList(CARE_RECORD_LIST_REQUEST, mPetInfoItems[0].id, checkDate)
                } else {
                    if(checkDate.isEmpty()) {
                        checkDate = cdf.format(Date()).replace("-", "")
                    }

                    mApiClient.getCareRecordList(CARE_RECORD_LIST_REQUEST, petId, checkDate)
                }

                uploadTemperatureRecord()
                uploadHumidityRecord()
                uploadEarImageRecord()
            } else {
                loginLayout.visibility = View.GONE
                notLoginLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun checkScannerBattery(btVal: Int) {
        when (btVal) {
            5 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_100)
            4 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_80)
            3 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_60)
            2 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_40)
            1 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_20)
            0 -> scannerBattery.setBackgroundResource(R.drawable.ic_scanner_battery_00)
        }
    }

    private fun initCalendar() {
        val today = DateTime().withDayOfWeek(DateTimeConstants.MONDAY)
        ymwTxtView.text = "${today.year}.${String.format("%02d", today.monthOfYear)}"
        weekCalendar.setOnDateClickListener { date ->
            loadCareRecord(date)
        }
        weekCalendar.setOnWeekChangeListener { firstDayOfTheWeek, forward ->
            ymwTxtView.text = "${firstDayOfTheWeek.year}.${String.format("%02d", firstDayOfTheWeek.monthOfYear)}"
        }
        loadCareRecord(today)
    }

    private fun loadCareRecord(date: DateTime) {
        retryIfNetworkAbsent {
            checkDate = "${date.year}${String.format("%02d", date.monthOfYear)}${String.format("%02d", date.dayOfMonth)}"/*date.replace("-", "")*/
            mApiClient.getCareRecordList(CARE_RECORD_LIST_REQUEST, petId, checkDate)
        }
    }

    private fun uploadTemperatureRecord() {
        temperatureDb = TemperatureDB.getInstance(requireContext())!!

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val temperatureItems = async {
                val items = temperatureDb.temperatureDao().loadAll(petId, "N")
                items
            }.await()

            val tempDeleteItems = async {
                val items = temperatureDb.temperatureDao().loadAll(petId, "Y")
                items
            }.await()

            Logger.d("temperature items : ${temperatureItems.size}, delete items : ${tempDeleteItems.size}")
            if (temperatureItems.size > 0) {
                mApiClient.uploadTemperatureRecord(CARE_TEMPERATURE_UPLOAD_REQUEST, temperatureItems)
                mApiClient.uploadInjectionRecord(CARE_TEMPERATURE_INJECT_UPLOAD_REQUEST, petId, temperatureItems)
            }

            if (tempDeleteItems.size > 0) {
                mApiClient.deleteTemperatureRecord(CARE_TEMPERATURE_DELETE_REQUEST, petId, tempDeleteItems)
            }
        }
    }

    private fun uploadHumidityRecord() {
        humidityDB = HumidityDB.getInstance(requireContext())!!

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val humidityItems = async {
                val items = humidityDB.humidityDao().loadAll(petId, "N")
                items
            }.await()

            val humidityDeleteItems = async {
                val items = humidityDB.humidityDao().loadAll(petId, "Y")
                items
            }.await()

            Logger.d("humidity items : ${humidityItems.size}, delete items : ${humidityDeleteItems.size}")
            if (humidityItems.size > 0) {
                mApiClient.uploadHumidityRecord(CARE_HUMIDITY_UPLOAD_REQUEST, humidityItems)
            }

            if (humidityDeleteItems.size > 0) {
                mApiClient.deleteHumidityRecord(CARE_HUMIDITY_DELETE_REQUEST, petId, humidityDeleteItems)
            }
        }
    }

    private fun uploadEarImageRecord() {
        earImageDB = EarImageDB.getInstance(requireContext())!!

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val earImageItems = async {
                val items = earImageDB.earImageDao().loadAll(petId)
                items
            }.await()

            Logger.d("earImageItems : ${earImageItems.size}")
            if (earImageItems.size > 0) {
                mApiClient.uploadEarImageRecord(CARE_EAR_IMAGE_UPLOAD_REQUEST, petId, earImageItems)
            }
        }
    }

    //===============================================================================================
    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.btnCareHistory -> {
                Airbridge.trackEvent("care", "click", "list", null, null, null)
                FirebaseAPI(requireActivity()).logEventFirebase("케어_기록전체보기버튼", "Click Event", "케어 기록 전체보기 버튼 클릭")

                startActivity(Intent(requireActivity(), CareHistoryActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("petKind", petKind)
                })
            }

            R.id.layoutWalk -> {
                startActivity(Intent(requireActivity(), ActionWalkActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", walkData)
                })
            }

            R.id.layoutCommunion -> {
                startActivity(Intent(requireActivity(), ActionCommunionActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", loveData)
                })
            }

            R.id.layoutTooth -> {
                startActivity(Intent(requireActivity(), ActionToothActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", toothData)
                })
            }

            R.id.layoutBath -> {
                startActivity(Intent(requireActivity(), ActionBathActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", bathData)
                })
            }

            R.id.layoutPoop -> {
                startActivity(Intent(requireActivity(), ActionPoopActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("feces", fecesData)
                    putExtra("urine", urineData)
                })
            }

            R.id.layoutWeight,
            R.id.layoutWeight1 -> {
                startActivity(Intent(requireActivity(), CareWeightActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", weightData)
                })
            }

            R.id.layoutClinic -> {
                startActivity(Intent(requireActivity(), CareClinicActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", clinicData)
                    putExtra("content", clinicContentData)
                })
            }

            R.id.layoutMemo -> {
                startActivity(Intent(requireActivity(), CareMemoActivity::class.java).apply {
                    putExtra("petId", petId)
                    putExtra("date", checkDate)
                    putExtra("item", memoData)
                })
            }

            R.id.layoutEar -> {
                if (imageViewEar.isEnabled) {
                    startActivity(Intent(requireActivity(), EarActivity::class.java).apply {
                        putExtra("petId", petId)
                        putExtra("petImage", petImageUrl)
                    })
                }
            }

            R.id.layoutTemperture -> {
                if (imageViewTemp.isEnabled) {
                    startActivity(Intent(requireActivity(), BodyTemperatureActivity::class.java).apply {
                        putExtra("petId", petId)
                        putExtra("petKind", petKind)
                    })
                }
            }

            R.id.layoutHumidity -> {
                if (imageViewHumidity.isEnabled) {
                    startActivity(Intent(requireActivity(), HumidityActivity::class.java).apply {
                        putExtra("petId", petId)
                    })
                }
            }

            R.id.layoutScannerConnect -> {
                val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (!wifiManager.isWifiEnabled) {
                    AppToast(requireContext()).showToastMessage(
                        "WIFI를 켜주세요.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                    return@OnClickListener
                }

                if (StorageUtils.loadBooleanValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS
                    )
                ) {
                    AppToast(requireContext()).showToastMessage(
                        "스캐너가 이미 연결되어있습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                } else {
                    Airbridge.trackEvent("care", "click", "connect_start", null, null, null)
                    val scannerName = StorageUtils.loadValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                        ""
                    )
                    if (scannerName.isEmpty()) {
                        startActivity(Intent(requireActivity(), ConnectScannerActivity::class.java))
                    } else {
                        lifecycleScope.launch {
                            // TODO. 펌웨어 업데이트 작업 내용 (기능 적용시 아래 주석 코드로 적용)
                            //startReconnectActivityIfLatestVersion()
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    ReConnectScannerActivity::class.java
                                )
                            )
                        }
                    }
                }
            }

            R.id.layoutHealthCare -> {
                startActivity(Intent(requireActivity(), HealthCareActivity::class.java))
            }
        }
    }

    private suspend fun startReconnectActivityIfLatestVersion() {
        val scanner: Scanner by inject()
        if (scanner.getFirmwareState() != FirmwareVersionState.LATEST_VERSION) {
            TwoBtnDialog(requireContext()).apply {
                setTitle("펌웨어 업데이트")
                setMessage("새로운 버전이 출시되었습니다.\n다운로드 하시겠습니까?")
                setConfirmButton("다운로드", View.OnClickListener {
                    requireContext().startActivity<ExternalDeviceDetailActivity> {
                        putExtra("downloadFirmware", true)
                    }
                    dismiss()
                })
                setCancelButton("다음에 하기", View.OnClickListener {
                    startActivity(Intent(requireActivity(), ReConnectScannerActivity::class.java))
                    dismiss()
                })
            }.show()
        } else {
            startActivity(Intent(requireActivity(), ReConnectScannerActivity::class.java))
        }
    }

    private fun onItemClicked(item: PetInfoItem) {
        checkPetKind(item)

        petId = item.id
        petKind = item.kind!!
        petImageUrl = item.imageUrl!!

        mApiClient.getCareRecordList(CARE_RECORD_LIST_REQUEST, item.id, checkDate)

        uploadTemperatureRecord()
        uploadHumidityRecord()
        uploadEarImageRecord()
    }

    private fun checkPetKind(item:PetInfoItem) {
        when {
            item.kind == "강아지" || item.kind == "고양이" -> {
                scannerLayout.visibility = View.VISIBLE
                layout1.visibility = View.VISIBLE

                if (item.kind == "고양이") {
                    layoutWalk.visibility = View.GONE
                    layoutBath.visibility = View.GONE
                    layoutWeight.visibility = View.GONE
                    layoutCommunion.visibility = View.VISIBLE
                    layoutWeight1.visibility = View.VISIBLE
                } else {
                    layoutWalk.visibility = View.VISIBLE
                    layoutBath.visibility = View.VISIBLE
                    layoutWeight.visibility = View.VISIBLE
                    layoutCommunion.visibility = View.GONE
                    layoutWeight1.visibility = View.GONE
                }
            }

            else -> {
                scannerLayout.visibility = View.GONE
                layout1.visibility = View.GONE
                layoutWeight.visibility = View.VISIBLE
                layoutWeight1.visibility = View.GONE
            }
        }
    }

    private fun checkRecord(items: MutableList<CareRecordData>) {
        for (item in items) {
            when (item.type) {
                "walk" -> {
                    layoutWalkCheck.visibility = View.VISIBLE
                    walkData = item
                }
                "weight" -> {
                    if (petKind == "고양이") {
                        layoutWeightCheck1.visibility = View.VISIBLE
                        weight1.text = item.value
                    } else {
                        layoutWeightCheck.visibility = View.VISIBLE
                        weight.text = item.value
                    }

                    weightData = item
                }
                "teeth" -> {
                    layoutToothCheck.visibility = View.VISIBLE
                    toothData = item
                }
                "bath" -> {
                    layoutBathCheck.visibility = View.VISIBLE
                    bathData = item
                }
                "feces" -> {
                    layoutPoopCheck.visibility = View.VISIBLE
                    fecesData = item
                }
                "urine" -> {
                    layoutPoopCheck.visibility = View.VISIBLE
                    urineData = item
                }
                "memo" -> {
                    layoutMemoCheck.visibility = View.VISIBLE
                    memoData = item
                }
                "clinic" -> {
                    layoutClinicCheck.visibility = View.VISIBLE
                    clinicData = item
                }
                "clinic_content" -> {
                    layoutClinicCheck.visibility = View.VISIBLE
                    clinicContentData = item
                }
                "communion" -> {
                    layoutLoveCheck.visibility = View.VISIBLE
                    loveData = item
                }
            }
        }
    }

    private fun clearRecord() {
        // UI cleare
        try {
            layoutWalkCheck.visibility = View.GONE
            layoutWeightCheck.visibility = View.GONE
            layoutWeightCheck1.visibility = View.GONE
            layoutToothCheck.visibility = View.GONE
            layoutBathCheck.visibility = View.GONE
            layoutPoopCheck.visibility = View.GONE
            layoutMemoCheck.visibility = View.GONE
            layoutClinicCheck.visibility = View.GONE
            layoutLoveCheck.visibility = View.GONE
        } catch (e: Exception) { }
        finally {
            // data clear
            fecesData = null
            urineData = null
            weightData = null
            clinicData = null
            clinicContentData = null
            memoData = null
            walkData = null
            toothData = null
            bathData = null
            loveData = null
        }

    }

    //==============================================================================================

    //==============================================================================================
    private var mPetInfoItems: List<PetInfoItem> = listOf()

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
                                if (requireContext().isInternetConnected()) {
                                    startActivity(Intent(requireActivity(), PetAddActivity::class.java).apply {
                                        putExtra("type", PetAddType.ADD.ordinal)
                                    })
                                } else {
                                    AppToast(requireContext()).showToastMessage(
                                        "네트워크 연결을 확인해주세요.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM)
                                }
                            } else {
                                startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
                            }
                        } else {
                            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                        }
                    }

                    holder.itemView.layoutPetInfo.setOnClickListener {
                        if (StorageUtils.loadBooleanValueFromPreference(
                                requireContext(),
                                AppConstants.PREF_KEY_LOGIN_STATUS
                            )
                        ) {
                            startActivity(Intent(requireActivity(), PetInfomationActivity::class.java))
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

    fun retryIfNetworkAbsent(invokeIfNetworkExist: Boolean = true, callback: () -> Unit) {
        val isScannerConnected = scanner.isConnected()
        val hasInternet = isNetworkAvailable.value?:true
        if (isScannerConnected && hasInternet.not()) {
            // 스캐너 연결 해제 및 네트웍 연결 확인 팝업
            TwoBtnDialog(requireContext()).apply {
                setTitle("펫닥 스캐너 연결 해제")
                setMessage("이용 가능한 네트워크가 없습니다.\n연결을 해제하시겠습니까?")
                setCancelable(false)
                setConfirmButton("해제", View.OnClickListener {
                    scanner.disconnect()
                    retryIfNetworkAbsent {
                        callback.invoke()
                    }
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()
        } else {
            if (hasInternet) {
                if (invokeIfNetworkExist) {
                    callback.invoke()
                }
            } else {
                // 네트웍 연결 확인 팝업
                AlertDialog.Builder(requireContext(), R.style.DefaultAlertDialogStyle)
                    .setMessage("네트워크가 연결되어 있지 않습니다.\n확인 후 다시 시도해주세요.")
                    .setCancelable(false)
                    .setPositiveButton(R.string.caption_retry) { _, _ ->
                        retryIfNetworkAbsent {
                            callback.invoke()
                        }
                    }.also {
                        it.show()
                    }
            }
        }
    }
}