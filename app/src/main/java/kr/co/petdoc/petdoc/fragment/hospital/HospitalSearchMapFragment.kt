package kr.co.petdoc.petdoc.fragment.hospital

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.adapter_hospital_list_item.view.*
import kotlinx.android.synthetic.main.fragment_hospital_search_map.*
import kotlinx.android.synthetic.main.fragment_hospital_search_map.address
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnArrow
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnFilter
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnHospitalListFilter
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnPosition
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnReception
import kotlinx.android.synthetic.main.fragment_hospital_search_map.btnReservation
import kotlinx.android.synthetic.main.fragment_hospital_search_map.count
import kotlinx.android.synthetic.main.fragment_hospital_search_map.distance
import kotlinx.android.synthetic.main.fragment_hospital_search_map.divider1
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hideHospitalList
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalImg
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalLayout
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalList
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalName
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalSearch
import kotlinx.android.synthetic.main.fragment_hospital_search_map.hospitalType
import kotlinx.android.synthetic.main.fragment_hospital_search_map.layoutHospital
import kotlinx.android.synthetic.main.fragment_hospital_search_map.layoutHospitalCall
import kotlinx.android.synthetic.main.fragment_hospital_search_map.layoutInfo
import kotlinx.android.synthetic.main.fragment_hospital_search_map.layoutInfoDetail
import kotlinx.android.synthetic.main.fragment_hospital_search_map.medicalTime
import kotlinx.android.synthetic.main.fragment_hospital_search_map.showHospitalList
import kotlinx.android.synthetic.main.fragment_hospital_search_map.status
import kotlinx.android.synthetic.main.fragment_pet_hospital.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalListResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HospitalSearchMapFragment : BaseFragment(),
    HospitalListDialogFragment.CallbackListener,
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    private val TAG = "HospitalSearchMapFragment"
    private val HOSPITAL_LIST_REQUEST = "${TAG}.hospitalListRequest"

    private lateinit var dataModel: HospitalDataModel
    private lateinit var hospitalAdater:HospitalAdapter
    private var hospitalItems: MutableList<HospitalItem> = mutableListOf()

    private var hospitalMap: GoogleMap? = null
    private var selectedMarker: Marker? = null
    private var selectedMarkerItem:HospitalItem? = null

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var moveLat = 0.0
    private var moveLng = 0.0

    private var page = 1
    private val size = 50
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_search_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)
        Logger.d("onViewCreated")

        Airbridge.trackEvent("tab", "click", "booking", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("병원예약_지도보기", "Page View", "병원 지도 view에 대한 페이지 뷰")

        currentLat = dataModel.currentLat.value!!.toDouble()
        currentLng = dataModel.currentLng.value!!.toDouble()
        Logger.d("currentLat : $currentLat, currentLng : $currentLng")

        dataModel.apply {
            hospitalItem.observe(requireActivity(), Observer {
                Logger.d("hospital name : ${it?.name}")
                if (it != null) {
                    selectedMarkerItem = it
                }
            })
        }

        //==========================================================================================
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        layoutInfo.setOnClickListener {
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

        btnPosition.setOnClickListener {
            hospitalMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(dataModel.currentLat.value!!.toDouble(), dataModel.currentLng.value!!.toDouble()),
                    16f
                )
            )

            btnPosition.setBackgroundResource(R.drawable.ic_map_position_select)
            hospitalSearch.text = Helper.readStringRes(R.string.search_hint)
        }

        btnFilter.setOnClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            findNavController()
                .navigate(HospitalSearchMapFragmentDirections.actionHospitalSearchMapFragmentToHospitalFilterFragment())
        }

        btnHospitalListFilter.setOnClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            findNavController()
                .navigate(HospitalSearchMapFragmentDirections.actionHospitalSearchMapFragmentToHospitalFilterFragment())
        }

        btnReservation.setOnClickListener {
            Airbridge.trackEvent("booking", "click", "book_start", null, null, null)

            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            bundleOf(
                "centerId" to selectedMarkerItem?.centerId
            ).let {
                findNavController().navigate(R.id.action_hospitalSearchMapFragment_to_hospitalReservationFragmet, it)
            }
        }

        btnReception.setOnClickListener {
            Airbridge.trackEvent("booking", "click", "receipt_start", null, null, null)
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            bundleOf(
                "centerId" to selectedMarkerItem?.centerId,
                "name" to selectedMarkerItem?.name,
                "location" to selectedMarkerItem?.location
            ).let {
                findNavController().navigate(R.id.action_hospitalSearchMapFragment_to_hospitalRegisterFragment, it)
            }
        }

        layoutHospitalCall.setOnClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${dataModel.telephone.value!!}")))
        }

        layoutHospital.setOnClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            Airbridge.trackEvent("booking", "click", "detail", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_상세페이지", "Page View", "병원 상세페이지 뷰")

            bundleOf("centerId" to dataModel.centerId.value!!.toInt()).let {
                findNavController().navigate(R.id.action_hospitalSearchMapFragment_to_hospitalDetailFragment, it)
            }
        }

        showHospitalList.setOnClickListener {
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            hospitalLayout.visibility = View.VISIBLE
            hospitalLayout.startAnimation(slideUp)

            FirebaseAPI(requireActivity()).logEventFirebase("병원예약_목록", "Page View", "병원 목록 view에 대한 페이지 뷰")
        }

        hideHospitalList.setOnClickListener {
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            hospitalLayout.visibility = View.INVISIBLE
            hospitalLayout.startAnimation(slideDown)
            isReload = false
        }

        hospitalSearch.setOnClickListener {
            showHospitalList.visibility = View.VISIBLE

            findNavController().navigate(HospitalSearchMapFragmentDirections.actionHospitalSearchMapFragmentToHospitalSearchBeforeFragment())
        }

        //==========================================================================================
        hospitalAdater = HospitalAdapter()
        hospitalList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hospitalAdater

            addOnScrollListener(object:RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(layoutManager is LinearLayoutManager){
                        (layoutManager as LinearLayoutManager).apply{
                            val lastItem = findLastVisibleItemPosition()
                            synchronized(pageTiggerPoint) {
                                if (lastItem > pageTiggerPoint) {
                                    isReload = true
                                    pageTiggerPoint = Int.MAX_VALUE

                                    mApiClient.getHomeHospitalList(
                                        HOSPITAL_LIST_REQUEST,
                                        currentLat.toString(),
                                        currentLng.toString(),
                                        "",
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_CLINIC_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_REGISTER_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_BOOKING_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_BEAUTY_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_HOTEL_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_ALLDAY_STATUS,
                                            ""
                                        ),
                                        StorageUtils.loadValueFromPreference(
                                            requireContext(),
                                            AppConstants.PREF_KEY_PARKING_STATUS,
                                            ""
                                        ),
                                        "",
                                        petIdList,
                                        size,
                                        page
                                    )
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

        if (dataModel.searchMode.value!!) {
            hospitalSearch.text = selectedMarkerItem?.name
        }

        StorageUtils.loadValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_CLINIC_PET_STATUS,
            ""
        ).let {
            if (it.isNotEmpty()) {
                petIdList = Gson().fromJson(it, Array<Int>::class.java).toMutableList()
            }
        }

        checkFilter()

        childFragmentManager.findFragmentById(R.id.hospital_map)?.let { it ->
            if (it is SupportMapFragment) {
                it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(HOSPITAL_LIST_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_LIST_REQUEST)
        }
    }

    override fun onSelectItem(item: HospitalItem) {
        selectedMarkerItem = item
        changeSelectedMarker(selectedMarker, item)
        setHospitalInfo(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {  //지도가 붙고나서,  위에서 세팅한 연동 지점으로 이 콜백이 호출됩니다.
        Logger.d("onMapReady")
        hospitalMap = googleMap
        dataModel.map.value = googleMap

        setMarkerView()
        setSelectedMarkerView()

        //지도 접근은 이 이벤트 콜백이 호출 된 이후부터 접근가능
        hospitalMap?.setOnMarkerClickListener(this)
        hospitalMap?.setOnMapClickListener(this)
        hospitalMap?.setOnCameraMoveListener(this)
        hospitalMap?.setOnCameraIdleListener(this)

        if (dataModel.searchMode.value!! && selectedMarkerItem != null) {
            val position = LatLng(selectedMarkerItem!!.latitude, selectedMarkerItem!!.longitude)
            val center = CameraUpdateFactory.newLatLng(position)
            hospitalMap?.animateCamera(center)
            setHospitalInfo(selectedMarkerItem!!)

            dataModel.searchMode.value = false
        } else {
            hospitalMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLat, currentLng), 16f))
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
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
            HospitalListDialogFragment(this@HospitalSearchMapFragment).show(childFragmentManager, "Hospital")
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
        val cameraPos = hospitalMap?.cameraPosition
        moveLat = cameraPos?.target?.latitude!!
        moveLng = cameraPos?.target?.longitude!!

        Logger.d("moveLat : $moveLat, moveLng : $moveLng")
        currentLat = moveLat
        currentLng = moveLng

        page = 1
        isReload = false
        hospitalMap?.clear()

        if (!searchMode) {
            selectedMarkerItem = null
        }

        mApiClient.getHomeHospitalList(
            HOSPITAL_LIST_REQUEST,
            moveLat.toString(),
            moveLng.toString(),
            "",
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_CLINIC_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_REGISTER_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_BOOKING_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_BEAUTY_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_HOTEL_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_ALLDAY_STATUS,
                ""
            ),
            StorageUtils.loadValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_PARKING_STATUS,
                ""
            ),
            "",
            petIdList,
            size,
            page
        )
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
            HOSPITAL_LIST_REQUEST -> {
                if (response is HospitalListResponse) {
                    if (!isReload) {
                        hospitalItems.clear()
                        hospitalItems.addAll(response.resultData.center)
                        hospitalAdater.notifyDataSetChanged()
                    } else {
                        if(response.resultData.center.size>0) {
                            for (item in response.resultData.center) {
                                hospitalItems.add(item)
                                hospitalAdater.notifyItemInserted(hospitalItems.size - 1)
                            }
                        }
                    }

                    ++page
                    pageTiggerPoint = hospitalItems.size - 4

                    count.text = response.resultData.totalCount.toString()

                    for (item in hospitalItems) {
                        if (item.name == selectedMarkerItem?.name) {
                            addMarker(item, true)
                        } else {
                            addMarker(item, false)
                        }
                    }

                    if(searchMode) {
                        searchMode = false
                    }

                    Airbridge.trackEvent("booking", "click", "list", null, null, null)
                }
            }
        }
    }

    //==============================================================================================
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

                else -> markerImg?.setBackgroundResource(R.drawable.blue_green_circle)
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

    private fun setHospitalInfo(item: HospitalItem) {
        FirebaseAPI(requireActivity()).logEventFirebase("병원예약_병원정보", "Page View", "병원요약 정보 조회 (하단 패널)")

        layoutHospital.visibility = View.VISIBLE
        showHospitalList.visibility = View.GONE

        dataModel.centerId.value = item.centerId
        dataModel.telephone.value = item.telephone

        if (item.mainImgUrl != null && item.mainImgUrl!!.isNotEmpty()) {
            hospitalImg.visibility = View.VISIBLE

            Glide.with(requireContext())
                .load(if(item.mainImgUrl!!.startsWith("http")) item.mainImgUrl!! else "${AppConstants.IMAGE_URL}${item.mainImgUrl!!}")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
                .into(hospitalImg)
        } else {
            hospitalImg.visibility = View.GONE
        }

        hospitalName.text = item.name
        distance.apply {
            when{
                item.distance < 1000 -> text = "${item.distance}m"
                else -> {
                    val kilometer:Float = item.distance.toFloat() / 1000
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
                btnReception.visibility = View.VISIBLE
            }

            BookingType.R.name -> {
                btnReservation.visibility = View.GONE
                btnReception.visibility = View.VISIBLE
            }

            BookingType.B.name -> {
                btnReception.visibility = View.GONE
                btnReservation.visibility = View.VISIBLE
            }

            else -> {
                btnReservation.visibility = View.GONE
                btnReception.visibility = View.GONE
            }
        }

        if (item.keywordList.size > 0) {
            hospitalType.visibility = View.VISIBLE
            hospitalType.removeAllViews()

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
            if (item.mainImgUrl.isNullOrEmpty()) {
                hospitalType.visibility = View.GONE
            } else {
                hospitalType.visibility = View.INVISIBLE
            }
        }
    }

    private fun onItemClicked(item: HospitalItem) {
        selectedMarkerItem = item

        val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        hospitalLayout.visibility = View.INVISIBLE
        hospitalLayout.startAnimation(slideDown)
        isReload = false

        val position = LatLng(item.latitude, item.longitude)
        val center = CameraUpdateFactory.newLatLng(position)
        hospitalMap?.animateCamera(center)
        setHospitalInfo(item)
    }

    private fun checkFilter():Boolean {
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

    //==============================================================================================
    inner class HospitalAdapter : RecyclerView.Adapter<HospitalHolder> () {
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

            holder.itemView.setOnClickListener {
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
                when{
                    _distance < 1000 -> text = "${_distance}m"
                    else -> {
                        val kilometer:Float = _distance.toFloat() / 1000
                        val distnce = String.format("%.1f km", kilometer)
                        text = distnce
                    }
                }
            }
        }

        fun setImage(_url: String?) {
            if (_url != null && _url.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}${_url}")
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
                    .into(item.image)
            } else {
                Glide.with(requireContext())
                    .load("")
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(30)))
                    .into(item.image)
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

        fun setTime(allDay:Boolean, _startTime: String?, _endTime:String?) {
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
                item.hospitalCategory.removeAllViews()

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

                BookingType.R.name -> {
                    item.layoutBookingType.visibility = View.VISIBLE
                    item.type.text = Helper.readStringRes(R.string.hospital_type_receiption)
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
}