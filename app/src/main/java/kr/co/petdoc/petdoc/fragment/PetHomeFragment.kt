package kr.co.petdoc.petdoc.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendbird.android.SendBird
import com.sendbird.desk.android.Ticket
import kotlinx.android.synthetic.main.adapter_home_chat_category_item.view.*
import kotlinx.android.synthetic.main.adapter_launcher_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_home.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.*
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalDetailActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalRegisterActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalReservationActivity
import kr.co.petdoc.petdoc.activity.life.LifeMagazineActivity
import kr.co.petdoc.petdoc.adapter.home.BannerAdapter
import kr.co.petdoc.petdoc.adapter.home.HomeHospitalAdapter
import kr.co.petdoc.petdoc.adapter.home.MagazineAdapter
import kr.co.petdoc.petdoc.adapter.home.PetTalkAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.autoScroll
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.chat.v2.SBConnectionManager
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.makeMarketingAgreeRequestBody
import kr.co.petdoc.petdoc.network.response.*
import kr.co.petdoc.petdoc.network.response.submodel.*
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.BannerViewModel
import kr.co.petdoc.petdoc.viewmodel.Interaction
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: PetHomeFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 펫닥 홈 화면
 */
class PetHomeFragment : BaseFragment(),
    MagazineAdapter.AdapterListener,
    HomeHospitalAdapter.AdapterListener,
    PetTalkAdapter.AdapterListener,
    MarketingAgreeDialogFragment.CallbackListener,
    Interaction {

    private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_TICKET_CHANNEL_LIST"
    private val PERMISSION_LOCATION = 200

    private lateinit var mMagazineAdapter: MagazineAdapter
    private lateinit var mHospitalAdapter: HomeHospitalAdapter
    private lateinit var mCategoryAdapter : CategoryPagerAdapter
    private lateinit var mPetTalkAdapter:PetTalkAdapter
    private lateinit var mLauncherAdapter:LauncherAdapter
    private var mAdapter: BannerAdapter? = null
    private lateinit var bannerViewModel:BannerViewModel

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var animalIdList : MutableList<Int> = mutableListOf()

    private var petInfoItem:PetInfoItem? = null
    private var shortcutItems:MutableList<ShortcutItem> = mutableListOf()

    private val petdocRepository: PetdocRepository by inject()
    private val apiService: PetdocApiService by inject()

    var page = 0
    private val pageOffset = 6
    var isReload = false

    private var latitude = ""
    private var longitude = ""

    private var margin16 = 0
    private var margin10 = 0

    private var isRunning = true

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bannerViewModel = ViewModelProvider(requireActivity()).get(BannerViewModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        margin16 = Helper.convertDpToPx(requireContext(), 16f).toInt()
        margin10 = Helper.convertDpToPx(requireContext(), 10f).toInt()

        Airbridge.trackEvent("tab", "click", "home", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("메인홈", "Page View", "메인홈의 page view")

        btnMyPage.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                startActivity(Intent(requireActivity(), MyPageActivity::class.java))
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
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
                    startActivity(Intent(requireActivity(), MyPageActivity::class.java).apply {
                        putExtra("fromHome", true)
                    })
                } else {
                    startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
                }
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        btnNoti.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                startActivity(Intent(requireActivity(), PushNotiActivity::class.java))
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        layoutMagazineMore.setOnSingleClickListener {
            Airbridge.trackEvent("home", "click", "백과_more", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("반려백과_더보기", "Click Event", "메인홈에서 반려백과 더보기 클릭")
            startActivity(Intent(requireActivity(), LifeMagazineActivity::class.java))
        }

        layoutHospitalMore.setOnSingleClickListener {
            Airbridge.trackEvent("home", "click", "hospital_more", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("홈_병원더보기", "Click Event", "홈에서 동물병원 더보기를 클릭")
            MainActivity.instance.moveToHospital()
//            startActivity(Intent(requireActivity(), HospitalActivity::class.java).apply {
//                putExtra("latitude", latitude)
//                putExtra("longitude", longitude)
//            })
        }

        layoutSearch.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("메인홈_검색", "Click Event", "메인홈에서 검색어 입력필드 클릭")
            startActivity(Intent(requireActivity(), SearchActivity::class.java))
        }

        hospital_tab_1_text.setOnSingleClickListener {
            hospital_tab_1_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
            hospital_tab_2_text.setTextColor(Helper.readColorRes(R.color.light_grey))

            hospital_tab_1_underline.visibility = View.VISIBLE
            hospital_tab_2_underline.visibility = View.INVISIBLE

            loadHospitalList("Y")
        }

        hospital_tab_2_text.setOnSingleClickListener {
            hospital_tab_1_text.setTextColor(Helper.readColorRes(R.color.light_grey))
            hospital_tab_2_text.setTextColor(Helper.readColorRes(R.color.dark_grey))

            hospital_tab_1_underline.visibility = View.INVISIBLE
            hospital_tab_2_underline.visibility = View.VISIBLE

            loadHospitalList("N")
        }

        layoutPetTalkMore.setOnSingleClickListener {
            Airbridge.trackEvent("home", "click", "talk_more", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("홈_펫톡더보기", "Click Event", "홈 내 펫톡 더보기 버튼 클릭")
            startActivity(Intent(requireActivity(), PetTalkActivity::class.java))
        }

        bannerPrice.setOnSingleClickListener {
            Airbridge.trackEvent("home", "click", "price_start", null, null, null)
            startActivity(Intent(requireActivity(), ClinicPriceActivity::class.java))
        }

        //===========================================================================================
        mCategoryAdapter = CategoryPagerAdapter()
        mCategoryAdapter.mCategoryItems = PetdocApplication.mCategoryItem

        mMagazineAdapter = MagazineAdapter().apply { setListener(this@PetHomeFragment) }
        magazineList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = mMagazineAdapter

//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    // 리스트 최 하단 체크한 후 API 호출
//                    if (!isEndofData && !magazineList.canScrollVertically(1)) {
//                        isReload = true
//                        ++page
//
//                        mApiClient.getMagazineList(MAGAZINE_LIST_REQUEST_TAG, "createdAt", pageOffset, page)
//                    }
//                }
//            })
        }

        mPetTalkAdapter = PetTalkAdapter().apply { setListener(this@PetHomeFragment) }
        petTalkList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = mPetTalkAdapter
        }

        mLauncherAdapter = LauncherAdapter()
        launcherList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { orientation = LinearLayoutManager.HORIZONTAL }
            adapter = mLauncherAdapter

            addItemDecoration(
                HorizontalMarginItemDecoration(
                marginStart = margin16,
                marginBetween = margin10,
                marginEnd = margin16
                )
            )
        }

        //============================================================================================
        hospital_tab_1_text.setTextColor(Helper.readColorRes(R.color.dark_grey))
        hospital_tab_2_text.setTextColor(Helper.readColorRes(R.color.light_grey))

        hospital_tab_1_underline.visibility = View.VISIBLE
        hospital_tab_2_underline.visibility = View.INVISIBLE

        retryIfNetworkAbsent {
            refreshProfile()
            loadHospitalInfo()
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        isRunning = true

        refreshProfile()
        registerPushToken()
        checkPushStatus()

        /*if (StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
            connectToSendBird()
        }

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, object : SendBird.ConnectionHandler {
            override fun onReconnectStarted() {
                Logger.d("onReconnectStarted")
            }

            override fun onReconnectSucceeded() {
                Logger.d("onReconnectSucceeded")
                getTickeCount()
            }

            override fun onReconnectFailed() {
                Logger.d("onReconnectFailed")
            }
        })*/
    }

    override fun onPause() {
        super.onPause()
        isRunning = false

        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID)
    }

    // ============================================================================================
    // Banner AdapterListener
    // ============================================================================================
    override fun onBannerItemClicked(bannerItem: BannerItem) {
        Logger.d("item :  ${bannerItem.id}")
        if (bannerItem.type == "shop") {
            val cipherStr = getAESCipherStr()
            if (StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?goodsNo=${bannerItem.url}&referer=true&jsonbody=${cipherStr}")))
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/goods/goods_view.php?goodsNo=${bannerItem.url}")))
            }
        } else {
            Airbridge.trackEvent("banner", "click", "${bannerItem.id}", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("메인홈_배너상세", "Click Event", "메인홈에서 배너 클릭")

            startActivity(Intent(requireActivity(), BannerDetailActivity::class.java).apply {
                putExtra("bannerId", bannerItem.id)
            })
        }
    }

    // ============================================================================================
    // Magazine AdapterListener
    // ============================================================================================
    override fun onMagazineItemClicked(item: MagazineItem) {
        Airbridge.trackEvent("home", "click", "${item.id}", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("메인홈_반려백과상세", "Click Event", "메인홈에서 반려백과 더보기 클릭")
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
    }

    override fun onPetTalkItemClicked(item: HomePetTalkData) {
        Airbridge.trackEvent("home", "click", "talk", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("홈_펫톡_게시글상세", "Click Event", "홈에서 바로 반려백과 상세 페이지를 클릭")
        startActivity(Intent(requireActivity(), PetTalkDetailActivity::class.java).apply {
            putExtra("petTalkId", item.id)
        })

    }

    // ============================================================================================
    // Hospital AdapterListener
    // ============================================================================================
    override fun onHospitalCall(item: HospitalItem) {
        Airbridge.trackEvent("home", "click", "call", null, null, null)
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.telephone}")))
    }

    override fun onReceptionClicked(item: HospitalItem) {
        FirebaseAPI(requireActivity()).logEventFirebase("홈_접수", "Click Event", "홈에서 바로 병원 접수를 클릭")
        Airbridge.trackEvent("home", "click", "receipt", null, null, null)
        startActivity(Intent(requireActivity(), HospitalRegisterActivity::class.java).apply {
            putExtra("centerId", item.centerId)
            putExtra("name", item.name)
            putExtra("location", item.location)
            putExtra("fromHome", true)
        })
    }

    override fun onReservationClicked(item: HospitalItem) {
        Airbridge.trackEvent("home", "click", "book", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("홈_예약", "Click Event", "홈에서 바로 병원 예약을 클릭")

        if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
            if (StorageUtils.loadIntValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_MOBILE_CONFIRM
                ) == 1
            ) {
                startActivity(Intent(requireActivity(), HospitalReservationActivity::class.java).apply {
                    putExtra("centerId", item.centerId)
                })
            } else {
                startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
            }
        }else{
            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
        }
    }

    override fun onHospitalItemClicked(item: HospitalItem) {
        Airbridge.trackEvent("home", "click", "hospital", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("홈_병원상세", "Click Event", "홈에서 바로 병원 상세 페이지를 클릭")
        startActivity(Intent(requireActivity(), HospitalDetailActivity::class.java).apply {
            putExtra("centerId", item.centerId)
        })

    }

    override fun marketingAgree() {
        lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            val response = apiService.putMarketingStatusWithCoroutine(makeMarketingAgreeRequestBody())
            if (response.isSuccessful) {
                val sdf = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
                val date = sdf.format(Date())
                Logger.d("response : ${getBody(response.body()!!)}")
                OneBtnDialog(requireContext()).apply {
                    setTitle("")
                    setMessage("고객님은 ${date}자로 마케팅 정보수신을 허용했습니다.")
                    setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                        dismiss()
                    })
                    show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentGPSLocation()
            } else {
                Logger.d("Permission Denied")

                val isNeverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                Logger.d("isNeverAskAgain : $isNeverAskAgain")
                if (isNeverAskAgain) {
                    Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                } else {
                    Logger.d("Location permission denied")
                }
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = async(Dispatchers.IO) {
                apiService.getBannerListWithCoroutine("main")
            }.await()

            initBannerPager(response.bannerList)
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = async(Dispatchers.IO) {
                apiService.getMagazineListWithCoroutine("createdAt", pageOffset, page*pageOffset)
            }.await()

            mMagazineAdapter.setData(response.resultData)
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = async(Dispatchers.IO) {
                apiService.getHomePetTalkListWithCoroutine() }.await()

            mPetTalkAdapter.setData(response.items)
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = async(Dispatchers.IO) {
                apiService.getHomeShortCutWithCoroutine()
            }.await()

            if (response.isSuccessful) {
                shortcutItems = Gson().fromJson(getBody(response.body()!!), object : TypeToken<MutableList<ShortcutItem>>() {}.type)
                mLauncherAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadPetInfo() {
        try {
            viewLifecycleOwner.lifecycleScope.launch {
                val repository by inject<PetdocRepository>()
                val oldUserId = if (StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                    StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
                } else {
                    0
                }
                val items = repository.retrieveMyPets(oldUserId)
                if (items.isNotEmpty()) {
                    val imageUrl = items[0].imageUrl!!
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(if (imageUrl.startsWith("http")) imageUrl else "${AppConstants.IMAGE_URL}${imageUrl}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(petImage)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.img_pet_profile_default)
                            .apply(RequestOptions.circleCropTransform())
                            .into(petImage)
                    }

                    petInfoItem = items[0]
                    PetdocApplication.mPetInfoList.clear()
                    PetdocApplication.mPetInfoList.addAll(items)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petImage)
                }
            }
        } catch (e: Exception) {
            Logger.p(e)
        } catch (e: IllegalStateException) {
            Logger.p(e)
        }
    }

    private fun refreshProfile() {
        lifecycleScope.launch {
            val respone = withContext(Dispatchers.IO + exceptionHandler) { apiService.getProfileInfoWithCoroutine() }
            if (respone.isSuccessful) {
                val profileItem: UserInfoData = Gson().fromJson(getBody(respone.body()!!), UserInfoData::class.java)
                PetdocApplication.mUserInfo = profileItem

                if (profileItem.marketing_email == 0 || profileItem.marketing_sms == 0) {
                    if(!StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW)) {
                        MarketingAgreeDialogFragment(this@PetHomeFragment).show(childFragmentManager, "Marketing")
                    }
                }

                val data = respone.headers()
                Logger.d("accessToken : ${data["AccessToken"]}, refreshToke : ${data["RefreshToken"]}")
                if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    val accessTokenInHeader = data["AccessToken"]
                    if (accessTokenInHeader != null) {
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, accessTokenInHeader)
                    }
                    val refreshTokenInHeader = data["RefreshToken"]
                    if (refreshTokenInHeader != null) {
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, refreshTokenInHeader)
                    }
                }

                loadPetInfo()
            } else {
                if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    logout()
                }

                loadPetInfo()
            }
        }
    }

    private fun loadHospitalInfo() {
        if(checkLocationPermission()) {
            getCurrentGPSLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun loadHospitalList(bookingYn:String) {
        try {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
                val response = petdocRepository.fetchHospitalList(
                    HospitalListForm(
                        ownerLatitude = latitude,
                        ownerLongitude = longitude,
                        booking = bookingYn,
                        latitude = latitude,
                        longitude = longitude,
                        petIdList = animalIdList,
                        size = 10,
                        page = 1
                    )
                )
                val items = response.resultData.center
                if (items.isNotEmpty()) {
                    refreshHospitalList(items)
                }
            }
        } catch (e: IllegalStateException) {
            Logger.p(e)
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    private fun refreshHospitalList(items: List<HospitalItem>) {
        mHospitalAdapter =
            HomeHospitalAdapter(requireContext()).apply { setListener(this@PetHomeFragment) }
        hospitalList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = mHospitalAdapter
        }

        mHospitalAdapter.setData(items)
    }

    private fun registerPushToken() {
        StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_FCM_PUSH_TOKEN, "").let {
            if (it.isNotEmpty()) {
                mApiClient.registerFCMToken("REGISTER_PUSH_TOKEN", it)
            }
        }
    }

    private fun checkPushStatus() {
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = withContext(Dispatchers.IO) { apiService.getPushUnRead() }
            if (response.code == "SUCCESS") {
                val status = response.resultData as Boolean
                notiUpdate.visibility = if (status) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun checkLocationPermission()
            = (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireContext())
                .setMessage("병원 목록을 가져오려면 위치 권한이 필요합니다.")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_LOCATION
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentGPSLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (isLocationEnabled()) {
            mFusedLocationClient!!.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location:Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    Logger.d("latitude : ${location.latitude}, longitude : ${location.longitude}")

                    PetdocApplication.currentLat = location.latitude
                    PetdocApplication.currentLng = location.longitude

                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()

                    loadHospitalList("Y")
                }
            }
        } else {
            Toast.makeText(requireActivity(), "GPS를 활성화 시켜 주시기 바랍니다.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = requireContext().applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            Logger.d("latitude : ${mLastLocation.latitude}, longitude : ${mLastLocation.longitude}")

            PetdocApplication.currentLat = mLastLocation.latitude
            PetdocApplication.currentLng = mLastLocation.longitude

            latitude = mLastLocation.latitude.toString()
            longitude = mLastLocation.longitude.toString()

            loadHospitalList("Y")
        }
    }

    private fun onLauncherItemClicked(item:ShortcutItem) {
        when (item.id) {
            "HealthCheck" -> {
                startActivity(Intent(requireActivity(), HealthCareActivity::class.java))
            }
            "Customeal" -> MainActivity.instance.moveToShopping()
            "Booking" -> MainActivity.instance.moveToHospital()
            "Chat" -> MainActivity.instance.moveToChat()
            "Scanner" -> {
                Airbridge.trackEvent("home", "click", "care", null, null, null)
                MainActivity.instance.moveToCare()
            }
            "MemoryHealth", "Mind" -> {
                startActivity(Intent(requireActivity(), BannerDetailActivity::class.java).apply {
                    putExtra("bannerId", item.bannerId.toInt())
                })
            }

            else -> {}
        }
    }

    private fun initBannerPager(items: List<BannerItem>) {
        bannerPager.apply {
            var pageOffset = Helper.convertDPResourceToPx(requireContext(), R.dimen.life_top_col_side).toFloat()
            var pageMargin = Helper.convertDPResourceToPx(requireContext(), R.dimen.life_top_col_advertisement_gap).toFloat()

            layoutParams = layoutParams.apply{
                layoutParams.height = (Helper.screenSize(requireActivity())[0] - pageOffset.toInt() * 2) * 110 / 343
            }

            mAdapter = BannerAdapter(this@PetHomeFragment)
            mAdapter?.setItems(items)
            adapter = mAdapter

            clipToPadding = false
            clipChildren = false
            setPadding(Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side), 0, Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side), 0)

            setPageTransformer { page, position ->
                when {
                    position >= 1 -> page.translationX = pageOffset - pageMargin
                    position <= -1 -> page.translationX = pageMargin - pageOffset
                    position >= 0 && position < 1 -> page.translationX = (pageOffset - pageMargin) * position
                    else -> page.translationX = (pageMargin - pageOffset) * (-position)
                }
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    isRunning = true

                    // 직접 유저가 스크롤했을 때!
                    bannerViewModel.setCurrentPosition(position)
                }
            })

            bannerPager.autoScroll(3000L)
        }
    }

    private fun getAESCipherStr(): String {
        val json = JSONObject().apply {
            put("user_id", StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, ""))
            put("name", PetdocApplication.mUserInfo?.name)
        }

        return AES256Cipher.encryptURL(json.toString())
    }

    private fun getBody(responseBody: ResponseBody): String {
        var body = ""

        //ResponseBody responseBody = response.body();
        val source = responseBody.source()
        try {
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()
            var charset = Charset.forName("UTF-8")
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(Charset.forName("UTF-8"))
            }
            if (responseBody.contentLength() != 0L) {
                body = buffer.clone().readString(charset)
            }
        } catch (e: java.lang.Exception) {
            Logger.p(e)
        }
        return body
    }

    // Category View Pager Adapter ===========================================================================================================
    inner class CategoryPagerAdapter : RecyclerView.Adapter<CategoryHolder>() {
        var mCategoryItems:MutableList<CategoryItem> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_chat_category_item, parent, false))

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            holder.setTitle(mCategoryItems[position%mCategoryItems.size].title)
            holder.setTitleColor(mCategoryItems[position % mCategoryItems.size].titleColor)
            holder.setContent(mCategoryItems[position % mCategoryItems.size].content)
            holder.setCategoryImg(mCategoryItems[position%mCategoryItems.size].image)
        }

        override fun getItemCount() = mCategoryItems.size * 1000
    }

    inner class CategoryHolder(var item:View) : RecyclerView.ViewHolder(item) {
        fun setTitle(_text : String){
            item.categoryTitle.text = _text
        }

        fun setTitleColor(_text: String) {
            item.categoryTitle.setTextColor(Color.parseColor(_text))
        }

        fun setContent(_text: String) {
            item.categoryDesc.text = _text
        }

        fun setCategoryImg(_text: String) {
            val id = resources.getIdentifier(_text, "drawable", context?.packageName)
            item.categoryImg.setImageResource(id)
        }
    }

    // Launcher Adapter =============================================================================
    inner class LauncherAdapter : RecyclerView.Adapter<LauncherHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LauncherHolder(layoutInflater.inflate(R.layout.adapter_launcher_item, parent, false))

        override fun onBindViewHolder(holder: LauncherHolder, position: Int) {
            holder.setLauncherItem(shortcutItems[position])
            holder.itemView.setOnClickListener {
                onLauncherItemClicked(shortcutItems[position])
            }
        }

        override fun getItemCount() = shortcutItems.size
    }

    inner class LauncherHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setLauncherItem(shortcutItem:ShortcutItem) =
            when (shortcutItem.id) {
                "HealthCheck" -> {
                    item.launcherImg.apply {
                        when {
                            shortcutItem.highlightYn == "Y" -> {
                                setBackgroundResource(R.drawable.ic_launcher_checkup_on)
                            }
                            else -> setBackgroundResource(R.drawable.ic_launcher_checkup)
                        }
                    }

                    item.launcherTxt.text = shortcutItem.title
                }
                "Customeal" -> {
                    item.launcherImg.apply {
                        when {
                            shortcutItem.highlightYn == "Y" -> {
                                setBackgroundResource(R.drawable.ic_launcher_customeal_on)
                            }
                            else -> setBackgroundResource(R.drawable.ic_launcher_customeal)
                        }
                    }

                    item.launcherTxt.text = shortcutItem.title
                }
                "Booking" -> {
                    item.launcherImg.apply {
                        when {
                            shortcutItem.highlightYn == "Y" -> {
                                setBackgroundResource(R.drawable.ic_launcher_hospital_on)
                            }
                            else -> setBackgroundResource(R.drawable.ic_launcher_hospital)
                        }
                    }

                    item.launcherTxt.text = shortcutItem.title
                }
                "Chat" -> {
                    item.launcherImg.apply {
                        when {
                            shortcutItem.highlightYn == "Y" -> {
                                setBackgroundResource(R.drawable.ic_launcher_clinic_on)
                            }
                            else -> setBackgroundResource(R.drawable.ic_launcher_clinic)
                        }
                    }

                    item.launcherTxt.text = shortcutItem.title
                }
                "Scanner" -> {
                    item.launcherImg.apply {
                        when {
                            shortcutItem.highlightYn == "Y" -> {
                                setBackgroundResource(R.drawable.ic_launcher_scanner_on)
                            }
                            else -> setBackgroundResource(R.drawable.ic_launcher_scanner)
                        }
                    }

                    item.launcherTxt.text = shortcutItem.title
                }

                "MemoryHealth" -> {
                    item.launcherImg.setBackgroundResource(R.drawable.ic_launcher_memory)
                    item.launcherTxt.text = shortcutItem.title
                }

                "Mind" -> {
                    item.launcherImg.setBackgroundResource(R.drawable.ic_launcher_mind)
                    item.launcherTxt.text = shortcutItem.title
                }

                else -> {}
            }
    }

    private fun connectToSendBird() {
        val userId = StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "")
        val nickname = StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, "")
        Logger.d("chat userId : ${userId}, nickName : $nickname")

        SBConnectionManager.login(userId, SendBird.ConnectHandler { user, sendBirdException ->
            if (sendBirdException != null) {
                Logger.d("Error ${sendBirdException.code}, ${sendBirdException.message}")
                return@ConnectHandler
            }

            SendBird.updateCurrentUserInfo(nickname, "", { e ->
                if (e != null) {
                    Logger.d("Error ${e.code}, ${e.message}")
                    return@updateCurrentUserInfo
                }

                getTickeCount()
            })
        })
    }

    private fun getTickeCount() {
        Ticket.getOpenCount(Ticket.GetOpenCountHandler { count, exception ->
            if (exception != null) {
                Logger.d("Error ${exception.code}, ${exception.message}")
                return@GetOpenCountHandler
            }

            Logger.d("Chat Ticket Count : $count")
        })
    }

    private fun logout() {
        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, false)
        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, false)
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, "")
        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, 0)
        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, 0)

        PetdocApplication.mPetInfoList.clear()
    }
}