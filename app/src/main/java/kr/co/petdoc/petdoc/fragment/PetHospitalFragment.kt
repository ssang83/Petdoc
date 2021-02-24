package kr.co.petdoc.petdoc.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_life_magazine.*
import kotlinx.android.synthetic.main.adapter_hospital_list_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_hospital.*
import kotlinx.android.synthetic.main.fragment_pet_hospital.btnTop
import kotlinx.android.synthetic.main.fragment_pet_hospital.divider1
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalDetailActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalFilterActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalReservationActivity
import kr.co.petdoc.petdoc.activity.hospital.HospitalSearchActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.event.HospitalSearchEvent
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.hospital.HospitalListDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: PetHospitalFragment
 * Created by kimjoonsung on 2020/07/21.
 *
 * Description :
 */
class PetHospitalFragment : Fragment(),
    HospitalListDialogFragment.CallbackListener,
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    private val PERMISSION_LOCATION = 200
    private val REQUEST_DETAIL = 0x2000

    private lateinit var mEventBus: EventBus
    protected lateinit var mApiClient: ApiClient

    private lateinit var hospitalAdater: HospitalAdapter
    private var hospitalItems: MutableList<HospitalItem> = mutableListOf()

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var hospitalMap: GoogleMap? = null
    private var selectedMarker: Marker? = null
    private var selectedMarkerItem: HospitalItem? = null

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var moveLat = 0.0
    private var moveLng = 0.0

    private var page = 1
    private val size = 100
    private var isOpen = true
    private var isReload = false
    private var pageTiggerPoint = Int.MAX_VALUE

    // default marker
    private var markerRoot: View? = null
    private var markerImg: AppCompatImageView? = null

    // sekected marker
    private var selectMarkerImg: AppCompatImageView? = null
    private var selectMarkerTxt: AppCompatTextView? = null
    private var selectMarkerRoot: View? = null

    private var telephone = ""
    private var centerId = -1
    private var petIdList: MutableList<Int> = mutableListOf()
    private var searchMode = false
    private var isFilterEnable = false
    private var keywordLayoutWidth = 0
    private var keywordWidth = 0
    private var keyword = ""
    private var fromDetail = false

    protected lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)
    val scanner: Scanner by inject()
    private val petdocRepository: PetdocRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("tab", "click", "booking", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("병원예약_지도보기", "Page View", "병원 지도 view에 대한 페이지 뷰")

        mEventBus = EventBus.getDefault()
        mApiClient = PetdocApplication.application.apiClient

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = requireContext().isInternetConnected()
        //============================================================================================
        currentLat = PetdocApplication.currentLat
        currentLng = PetdocApplication.currentLng
        Logger.d("currentLat : $currentLat, currentLng : $currentLng")

        layoutInfo.setOnSingleClickListener {
            if (isOpen) {
                layoutInfoDetail.visibility = View.GONE
                btnArrow.setBackgroundResource(R.drawable.ic_map_arrow_open)
                isOpen = false
            } else {
                layoutInfoDetail.visibility = View.VISIBLE
                btnArrow.setBackgroundResource(R.drawable.ic_map_arrow_close)
                isOpen = true
            }
        }

        btnPosition.setOnSingleClickListener {
            hospitalMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(PetdocApplication.currentLat, PetdocApplication.currentLng),
                    16f
                )
            )

            btnPosition.setBackgroundResource(R.drawable.ic_map_position_select)
            hospitalSearch.text = Helper.readStringRes(R.string.search_hint)
        }

        btnFilter.setOnSingleClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            startActivity(Intent(requireActivity(), HospitalFilterActivity::class.java))
        }

        btnHospitalListFilter.setOnSingleClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            startActivity(Intent(requireActivity(), HospitalFilterActivity::class.java))
        }

        btnReservation.setOnSingleClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                if (StorageUtils.loadIntValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_MOBILE_CONFIRM
                    ) == 1
                ) {
                    Airbridge.trackEvent("booking", "click", "book_start", null, null, null)
                    layoutHospital.visibility = View.GONE
                    showHospitalList.visibility = View.VISIBLE

                    startActivity(Intent(requireActivity(), HospitalReservationActivity::class.java).apply {
                        putExtra("centerId", centerId)
                        putExtra("bookingTab", "hospital")
                    })
                } else {
                    startActivity(Intent(requireActivity(), MobileAuthActivity::class.java))
                }
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        layoutHospitalCall.setOnSingleClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telephone}")))
        }

        layoutHospital.setOnSingleClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            Airbridge.trackEvent("booking", "click", "detail", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_상세페이지", "Page View", "병원 상세페이지 뷰")
            startActivity(Intent(requireActivity(), HospitalDetailActivity::class.java).apply {
                putExtra("centerId", centerId)
                putExtra("bookingTab", "hospital")
            })
        }

        showHospitalList.setOnSingleClickListener {
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            hospitalLayout.visibility = View.VISIBLE
            hospitalLayout.startAnimation(slideUp)

            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_목록", "Page View", "병원 목록 view에 대한 페이지 뷰")
        }

        hideHospitalList.setOnSingleClickListener {
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            hospitalLayout.visibility = View.INVISIBLE
            hospitalLayout.startAnimation(slideDown)
            isReload = false
        }

        hospitalSearch.setOnSingleClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_검색", "Click Event", "병원지도보기 화면에서 검색바 클릭")
            startActivity(Intent(requireActivity(), HospitalSearchActivity::class.java))
        }

        btnTop.setOnSingleClickListener { hospitalList.scrollToPosition(0) }

        inputDelete.setOnSingleClickListener {
            it.visibility = View.GONE
            hospitalSearch.text = Helper.readStringRes(R.string.search_hint)

            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE
            keywordLayoutWidth = 0
            keywordWidth = 0
            keyword = ""

            changeSelectedMarker(null, selectedMarkerItem)
            loadHospitalList()
        }

        //==========================================================================================
        hospitalAdater = HospitalAdapter()
        hospitalList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hospitalAdater

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (layoutManager is LinearLayoutManager) {
                        (layoutManager as LinearLayoutManager).apply {
                            val lastItem = findLastVisibleItemPosition()
                            val visibleCount = findFirstVisibleItemPosition()
                            if (visibleCount > 10) {
                                btnTop.visibility = View.VISIBLE
                            } else {
                                btnTop.visibility = View.GONE
                            }

                            synchronized(pageTiggerPoint) {
                                if (lastItem > pageTiggerPoint) {
                                    isReload = true
                                    pageTiggerPoint = Int.MAX_VALUE
                                    ++page

                                    loadHospitalList()
                                }
                            }
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }

        btnArrow.setBackgroundResource(R.drawable.ic_map_arrow_open)
        btnPosition.setBackgroundResource(R.drawable.ic_map_position_select)
        btnFilter.setBackgroundResource(R.drawable.ic_map_filter_default)
        btnHospitalListFilter.setBackgroundResource(R.drawable.ic_hospital_list_filter_default)
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")

        // 내가 이걸 왜 했지?? 큰일이다...사이드 냄새가 난다...ㅠ
//        if (hospitalLayout.isVisible) {
//            hospitalLayout.visibility = View.GONE
//        }

        if (checkLocationPermission()) {
            getCurrentGPSLocation()
        } else {
            requestLocationPermission()
        }

        val items = StorageUtils.loadValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_CLINIC_PET_STATUS,
            ""
        )
        if (items.isNotEmpty()) {
            petIdList = Gson().fromJson(items, Array<Int>::class.java).toMutableList()
        }

        checkFilter()
        retryIfNetworkAbsent {  }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (!menuVisible) { // 화면에서 병원 탭이 안 보이는 경우
            if (selectedMarkerItem != null) {
                selectedMarkerItem = null
            }

            if (selectedMarker != null) {
                selectedMarker = null
            }

            searchMode = false
            isOpen = true
            isReload = false

            keywordWidth = 0
            keywordLayoutWidth = 0

            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE
        }

    }

    override fun onSelectItem(item: HospitalItem) {
        selectedMarkerItem = item
        changeSelectedMarker(selectedMarker, item)
        setHospitalInfo(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {  //지도가 붙고나서,  위에서 세팅한 연동 지점으로 이 콜백이 호출됩니다.
        Logger.d("onMapReady")
        hospitalMap = googleMap

        setMarkerView()
        setSelectedMarkerView()

        //지도 접근은 이 이벤트 콜백이 호출 된 이후부터 접근가능
        hospitalMap?.setOnMarkerClickListener(this)
        hospitalMap?.setOnMapClickListener(this)
        hospitalMap?.setOnCameraMoveListener(this)
        hospitalMap?.setOnCameraIdleListener(this)

        hospitalMap?.isMyLocationEnabled = true
        hospitalMap?.uiSettings?.isMyLocationButtonEnabled = false

        if (searchMode && selectedMarkerItem != null) {
            val position = LatLng(selectedMarkerItem!!.latitude, selectedMarkerItem!!.longitude)
            val center = CameraUpdateFactory.newLatLng(position)
            hospitalMap?.animateCamera(center)
        } else {
            hospitalMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        currentLat,
                        currentLng
                    ), 16f
                )
            )
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (selectedMarker != null && selectedMarkerItem != null) {
            if (selectedMarker!!.position == marker?.position) {
                return true
            }

            addMarker(selectedMarkerItem!!, false)
            selectedMarker!!.remove()
            selectedMarker = null
        }

        val items:MutableList<HospitalItem> = mutableListOf()
        for (i in 0 until hospitalItems.size) {
            if (hospitalItems[i].latitude == marker?.position!!.latitude
                && hospitalItems[i].longitude == marker.position.longitude
            ) {
                items.add(hospitalItems[i])
            }
        }

        if (items.size == 1) {
            selectedMarkerItem = items[0]
            changeSelectedMarker(marker, items[0])
            setHospitalInfo(items[0])
        } else {
            PetdocApplication.mHospitalMarkerItems.clear()
            PetdocApplication.mHospitalMarkerItems.addAll(items)
            selectedMarker = marker
            HospitalListDialogFragment(this@PetHospitalFragment).show(childFragmentManager, "Hospital")
        }

        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        layoutHospital.visibility = View.GONE
        showHospitalList.visibility = View.VISIBLE
        keywordLayoutWidth = 0
        keywordWidth = 0

        if (selectedMarker != null && selectedMarkerItem != null) {
            changeSelectedMarker(null, selectedMarkerItem)
        } else {
            hospitalMap?.clear()
            for (item in hospitalItems) {
                addMarker(item, false)
            }
        }
    }

    override fun onCameraMove() {
        Logger.d("onCameraMove")
        btnPosition.setBackgroundResource(R.drawable.ic_map_position_default)
    }

    override fun onCameraIdle() {
        Logger.d("onCameraIdle")
        if (hospitalLayout.isVisible && fromDetail) {
            fromDetail = false
            return
        }

        val cameraPos = hospitalMap?.cameraPosition
        moveLat = cameraPos?.target?.latitude!!
        moveLng = cameraPos?.target?.longitude!!

        Logger.d("moveLat : $moveLat, moveLng : $moveLng")
        currentLat = moveLat
        currentLng = moveLng

        page = 1
        isReload = false

        hospitalMap?.clear()

        loadHospitalList()
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
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireActivity().packageName}")).apply {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_DETAIL -> {
                if (resultCode == Activity.RESULT_OK) {
                    fromDetail = true
                    Logger.d("isDetail : $fromDetail")
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
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
    fun onEventMainThread(event: HospitalSearchEvent) {
        selectedMarkerItem = event.item
        hospitalSearch.text = event.keyword
        keyword = event.keyword
        searchMode = event.searchMode
        inputDelete.visibility = View.VISIBLE
        hospitalLayout.visibility = View.GONE
    }

    //==============================================================================================
    private fun checkLocationPermission()
            = (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireContext())
                .setMessage("병원 예약 서비스를 이용하려면 위치 권한이 필요합니다.")
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
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()

                    if (currentLat != 0.0 && currentLng != 0.0) {
                        childFragmentManager.findFragmentById(R.id.hospital_map)?.let { it ->
                            if (it is SupportMapFragment) {
                                it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
                            }
                        }
                    }
                } else {
                    Logger.d("latitude : ${location.latitude}, longitude : ${location.longitude}")

                    PetdocApplication.currentLat = location.latitude
                    PetdocApplication.currentLng = location.longitude

                    currentLat = location.latitude
                    currentLng = location.longitude

                    childFragmentManager.findFragmentById(R.id.hospital_map)?.let { it ->
                        if (it is SupportMapFragment) {
                            it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
                        }
                    }
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

            currentLat = mLastLocation.latitude
            currentLng = mLastLocation.longitude
        }
    }

    private fun setMarkerView() {
        markerRoot = layoutInflater.inflate(R.layout.hospital_reservation_marker, null)
        markerImg = markerRoot!!.findViewById(R.id.marker)
    }

    private fun setSelectedMarkerView() {
        selectMarkerRoot = layoutInflater.inflate(R.layout.hospital_click_marker, null)
        selectMarkerImg = selectMarkerRoot!!.findViewById(R.id.markerImg)
        selectMarkerTxt = selectMarkerRoot!!.findViewById(R.id.name)
    }

    /**
     * View를 Bitmap으로 변환
     *
     * @param context
     * @param view
     * @return
     */
    private fun createDrawableFromView(
        context: Context,
        view: View
    ): Bitmap? {
        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun addMarker(item: HospitalItem, isSelectedMarker: Boolean): Marker {
        val position = LatLng(item.latitude, item.longitude)

        var markerOptions: MarkerOptions?
        if (!isSelectedMarker) {
            when (item.bookingType) {
                BookingType.A.name,
                BookingType.R.name,
                BookingType.B.name -> {
                    markerImg?.setBackgroundResource(R.drawable.ic_reservation_marker)
                }

                else -> markerImg?.setBackgroundResource(R.drawable.ic_hospital_basic_pin)
            }

            markerOptions = MarkerOptions().apply {
                position(position)
                icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createDrawableFromView(
                            requireContext(),
                            markerRoot!!
                        )
                    )
                )
            }
        } else {
            when (item.bookingType) {
                BookingType.A.name,
                BookingType.R.name,
                BookingType.B.name -> {
                    selectMarkerImg?.setBackgroundResource(R.drawable.img_hospital_reservation)
                }

                else -> selectMarkerImg?.setBackgroundResource(R.drawable.img_hospital_normal)
            }

            selectMarkerTxt?.text = item.name

            markerOptions = MarkerOptions().apply {
                position(position)
                icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createDrawableFromView(
                            requireContext(),
                            selectMarkerRoot!!
                        )
                    )
                )
            }
        }

        return hospitalMap?.addMarker(markerOptions)!!
    }

    private fun changeSelectedMarker(marker: Marker?, item: HospitalItem?) {
        if (selectedMarker != null) { // 선택했던 마커 되돌리기
            addMarker(item!!, false)
            selectedMarker!!.remove()
            selectedMarker = null
        }

        if (marker != null) {   // 선택한 마커 표시
            selectedMarker = addMarker(item!!, true)
            marker.remove()
        }
    }

    private fun setHospitalInfo(item: HospitalItem?) {
        FirebaseAPI(requireActivity()).logEventFirebase("병원예약_병원정보", "Page View", "병원요약 정보 조회 (하단 패널)")

        if (item != null) {
            layoutHospital.visibility = View.VISIBLE
            showHospitalList.visibility = View.GONE

            centerId = item.centerId
            telephone = item.telephone

            if (item.mainImgUrl != null && item.mainImgUrl!!.isNotEmpty()) {
                hospitalImg.visibility = View.VISIBLE

                Glide.with(requireContext())
                    .load(if (item.mainImgUrl!!.startsWith("http")) item.mainImgUrl!! else "${AppConstants.IMAGE_URL}${item.mainImgUrl!!}")
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
                    .into(hospitalImg)
            } else {
                hospitalImg.visibility = View.GONE
            }

            hospitalName.text = item.name
            distance.apply {
                when {
                    item.distance < 1000 -> text = "${item.distance}m"
                    else -> {
                        val kilometer: Float = item.distance.toFloat() / 1000
                        val distnce = String.format("%.1f km", kilometer)
                        text = distnce
                    }
                }
            }

            address.text = item.location
            status.apply {
                when (item.runStatus) {
                    RunStatus.O.name -> {
                        visibility = View.VISIBLE
                        divider1.visibility = View.VISIBLE
                        text = RunStatus.O.status
                    }
                    RunStatus.C.name -> {
                        visibility = View.VISIBLE
                        divider1.visibility = View.VISIBLE
                        text = RunStatus.C.status
                    }
                    RunStatus.B.name -> {
                        visibility = View.VISIBLE
                        divider1.visibility = View.VISIBLE
                        text = RunStatus.B.status
                    }
                    RunStatus.R.name -> {
                        visibility = View.VISIBLE
                        divider1.visibility = View.VISIBLE
                        text = RunStatus.R.status
                    }
                    else -> {
                        visibility = View.INVISIBLE
                        divider1.visibility = View.GONE
                    }
                }
            }

            medicalTime.apply {
                when {
                    item.allDay == true -> {
                        visibility = View.VISIBLE
                        text = "24시"
                    }
                    item.allDay == false -> {
                        if (item.startTime != null && item.endTime != null) {
                            visibility = View.VISIBLE
                            text = "${item.startTime} ~ ${item.endTime}"
                        } else {
                            visibility = View.GONE
                        }
                    }
                }
            }

            when (item.bookingType) {
                BookingType.A.name -> {
                    btnReservation.visibility = View.VISIBLE
                }

                BookingType.R.name -> {
                    btnReservation.visibility = View.GONE
                }

                BookingType.B.name -> {
                    btnReservation.visibility = View.VISIBLE
                }

                else -> {
                    btnReservation.visibility = View.GONE
                }
            }

            if (item.keywordList.size > 0) {
                hospitalType.visibility = View.VISIBLE
                hospitalType.post {
                    keywordLayoutWidth = hospitalType.width
                    for (i in 0 until item.keywordList.size) {
                        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_hospital_type, null, false)
                        view.type.text = item.keywordList[i]
                        view.measure(0, 0)
                        keywordWidth += view.measuredWidth

                        if (keywordLayoutWidth > keywordWidth) {
                            hospitalType.addView(view)
                        } else {
                            break
                        }
                    }
                }
            } else {
                when (item.bookingType) {
                    BookingType.A.name,
                    BookingType.R.name,
                    BookingType.B.name -> hospitalType.visibility = View.INVISIBLE

                    else -> hospitalType.visibility = View.GONE
                }
            }
        } else {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE
        }
    }

    private fun onItemClicked(item: HospitalItem) {
        centerId = item.centerId

        Airbridge.trackEvent("booking", "click", "detail", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("병원예약_상세페이지", "Page View", "병원 상세페이지 뷰")
        startActivityForResult(Intent(requireActivity(), HospitalDetailActivity::class.java).apply {
            putExtra("centerId", centerId)
            putExtra("bookingTab", "hospital")
        }, REQUEST_DETAIL)
    }

    private fun checkFilter(): Boolean {
        if (StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_CLINIC_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_REGISTER_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_BOOKING_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_BEAUTY_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_HOTEL_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_ALLDAY_STATUS,
                ""
            ) == "Y"
            || StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_PARKING_STATUS,
                ""
            ) == "Y"
            || petIdList.size > 0
        ) {
            btnFilter.setBackgroundResource(R.drawable.ic_map_filter_select)
            btnHospitalListFilter.setBackgroundResource(R.drawable.ic_hospital_list_filter_select)

            isFilterEnable = true
        } else {
            btnFilter.setBackgroundResource(R.drawable.ic_map_filter_default)
            btnHospitalListFilter.setBackgroundResource(R.drawable.ic_hospital_list_filter_default)

            isFilterEnable = false
        }

        return isFilterEnable
    }

    private fun loadHospitalList() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = petdocRepository.fetchHospitalList(
                    HospitalListForm(
                        ownerLatitude = moveLat.toString(),
                        ownerLongitude = moveLng.toString(),
                        latitude = moveLat.toString(),
                        longitude = moveLng.toString(),
                        keyword = keyword,
                        working = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_CLINIC_STATUS,
                            ""
                        ),
                        register = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_REGISTER_STATUS,
                            ""
                        ),
                        booking = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_BOOKING_STATUS,
                            ""
                        ),
                        beauty = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_BEAUTY_STATUS,
                            ""
                        ),
                        hotel = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_HOTEL_STATUS,
                            ""
                        ),
                        allDay = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_ALLDAY_STATUS,
                            ""
                        ),
                        parking = StorageUtils.loadValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_PARKING_STATUS,
                            ""
                        ),
                        healthCheck = "",
                        petIdList = petIdList,
                        size = size,
                        page = page)
                )
                val items = response.resultData.center
                val totalCount = response.resultData.totalCount
                if (items.isNotEmpty()) {
                    refreshHospitalList(items, totalCount.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshHospitalList(items: List<HospitalItem>, totalCount:String) {
        if (!isReload) {
            hospitalItems.clear()
            hospitalItems.addAll(items)
        } else {
            if (items.size > 0) {
                for (item in items) {
                    hospitalItems.add(item)
                }
            }
        }

        hospitalAdater.notifyDataSetChanged()

        pageTiggerPoint = hospitalItems.size - 4

        count.text = totalCount

        Logger.d("searchMode : $searchMode")
        if (searchMode) {
            searchMode = false
            layoutHospital.visibility = View.VISIBLE
            showHospitalList.visibility = View.GONE

            setHospitalInfo(selectedMarkerItem)

            for (item in hospitalItems) {
                if (item.name == selectedMarkerItem?.name) {
                    addMarker(item, true)
                } else {
                    addMarker(item, false)
                }
            }
        } else {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            for (item in hospitalItems) {
                addMarker(item, false)
            }
        }

        selectedMarker = null
        selectedMarkerItem = null

        Airbridge.trackEvent("booking", "click", "list", null, null, null)
    }

    //==============================================================================================
    inner class HospitalAdapter : RecyclerView.Adapter<HospitalHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HospitalHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_hospital_list_item, parent, false)
            )

        override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
            holder.setName(hospitalItems[position].name)
            holder.setLocation(hospitalItems[position].location)
            holder.setDistance(hospitalItems[position].distance)
            holder.setImage(hospitalItems[position].mainImgUrl)
            holder.setStatus(hospitalItems[position].runStatus)
            holder.setBookingType(hospitalItems[position].bookingType)

            holder.setTime(
                hospitalItems[position].allDay,
                hospitalItems[position].startTime,
                hospitalItems[position].endTime
            )

            holder.setKeywordList(hospitalItems[position].keywordList)

            holder.itemView.setOnSingleClickListener {
                onItemClicked(hospitalItems[position])
            }
        }

        override fun getItemCount() = hospitalItems.size
    }

    inner class HospitalHolder(var item: View) : RecyclerView.ViewHolder(item) {
        private var keywordLayoutWidth = 0
        private var keywordWidth = 0

        fun setName(_name: String) {
            item.name.text = _name
        }

        fun setLocation(_location: String) {
            item.location.text = _location
        }

        fun setDistance(_distance: Int) {
            item.distance.apply {
                when {
                    _distance < 1000 -> text = "${_distance}m"
                    else -> {
                        val kilometer: Float = _distance.toFloat() / 1000
                        val distnce = String.format("%.1f km", kilometer)
                        text = distnce
                    }
                }
            }
        }

        fun setImage(_url: String?) {
            if (_url != null && _url.isNotEmpty()) {
                item.image.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(if (_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}${_url}")
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
                    .into(item.image)
            } else {
                item.image.visibility = View.GONE
            }
        }

        fun setStatus(_status: String?) {
            item.hospitalStatus.apply {
                when (_status) {
                    RunStatus.O.name -> {
                        visibility = View.VISIBLE
                        item.statusDivider.visibility = View.VISIBLE
                        text = RunStatus.O.status
                    }
                    RunStatus.C.name -> {
                        visibility = View.VISIBLE
                        item.statusDivider.visibility = View.VISIBLE
                        text = RunStatus.C.status
                    }
                    RunStatus.B.name -> {
                        visibility = View.VISIBLE
                        item.statusDivider.visibility = View.VISIBLE
                        text = RunStatus.B.status
                    }
                    RunStatus.R.name -> {
                        visibility = View.VISIBLE
                        item.statusDivider.visibility = View.VISIBLE
                        text = RunStatus.R.status
                    }
                    else -> {
                        visibility = View.INVISIBLE
                        item.statusDivider.visibility = View.GONE
                    }
                }
            }
        }

        fun setTime(allDay: Boolean, _startTime: String?, _endTime: String?) {
            item.time.apply {
                when {
                    allDay == true -> {
                        visibility = View.VISIBLE
                        text = "24시"
                    }
                    allDay == false -> {
                        if (_startTime != null && _endTime != null) {
                            visibility = View.VISIBLE
                            text = "${_startTime} ~ ${_endTime}"
                        } else {
                            visibility = View.GONE
                        }
                    }
                }
            }
        }

        fun setKeywordList(keywrods:List<String>) {
            if (keywrods.size > 0) {
                item.hospitalCategory.visibility = View.VISIBLE
//                item.hospitalCategory.removeAllViews()

                item.hospitalCategory.post {
                    keywordLayoutWidth = item.hospitalCategory.width
                    for (i in 0 until keywrods.size) {
                        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_hospital_type, null, false)
                        view.type.text = keywrods[i]
                        view.measure(0, 0)
                        keywordWidth += view.measuredWidth

                        if (keywordLayoutWidth > keywordWidth) {
                            item.hospitalCategory.addView(view)
                        } else {
                            break
                        }
                    }
                }
            } else {
                item.hospitalCategory.visibility = View.GONE
            }
        }

        fun setBookingType(type: String?) {
            when (type) {
                BookingType.A.name -> {
                    item.layoutBookingType.visibility = View.VISIBLE
                    item.type.text = Helper.readStringRes(R.string.hospital_type_all)
                }

                BookingType.B.name -> {
                    item.layoutBookingType.visibility = View.VISIBLE
                    item.type.text = Helper.readStringRes(R.string.hospital_type_reservation)
                }

                else -> {
                    item.layoutBookingType.visibility = View.GONE
                }
            }
        }
    }

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
                androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.DefaultAlertDialogStyle)
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