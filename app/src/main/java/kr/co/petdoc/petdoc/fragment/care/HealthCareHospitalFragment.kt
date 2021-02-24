package kr.co.petdoc.petdoc.fragment.care

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import kotlinx.android.synthetic.main.adapter_health_care_hospital_list_item.view.*
import kotlinx.android.synthetic.main.fragment_health_care_hospital.*
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.hospital.HospitalListDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: HealthCareHospitalFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareHospitalFragment : Fragment(),
    HospitalListDialogFragment.CallbackListener,
    HospitalSelectDialogFragment.CallbackListener,
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    private val PERMISSION_LOCATION = 200

    private lateinit var dataModel:HospitalDataModel

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

    private var petIdList: MutableList<Int> = mutableListOf()
    private var keywordLayoutWidth = 0
    private var keywordWidth = 0

    private val petdocRepository: PetdocRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentLat = PetdocApplication.currentLat
        currentLng = PetdocApplication.currentLng

        dataModel.currentLat.value = currentLat.toString()
        dataModel.currentLng.value = currentLng.toString()
        Logger.d("currentLat : $currentLat, currentLng : $currentLng")

        //==============================================================================================
        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
//            TwoBtnDialog(requireContext()).apply {
//                setMessage("종합검사 병원예약을 취소하시겠습니까?")
//                setConfirmButton("확인", View.OnClickListener {
//                    requireActivity().finish()
//                })
//                setCancelButton("취소", View.OnClickListener {
//                    dismiss()
//                })
//            }.show()
        }

        btnPosition.setOnClickListener {
            hospitalMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(PetdocApplication.currentLat, PetdocApplication.currentLng),
                    16f
                )
            )

            btnPosition.setBackgroundResource(R.drawable.ic_map_position_select)
            hospitalSearch.text = Helper.readStringRes(R.string.search_hint)
        }

        layoutHospital.setOnClickListener {
            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE

            bundleOf("centerId" to dataModel.centerId.value!!.toInt()).let {
                findNavController().navigate(R.id.action_healthCareHospitalFragment_to_healthCareHospitalDetailFragment, it)
            }
        }

        showHospitalList.setOnClickListener {
            val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
            hospitalLayout.visibility = View.VISIBLE
            layoutPurchase.setBackgroundResource(R.color.white)
            hospitalLayout.startAnimation(slideUp)
        }

        hideHospitalList.setOnClickListener {
            val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            hospitalLayout.visibility = View.INVISIBLE
            layoutPurchase.setBackgroundResource(0)
            hospitalLayout.startAnimation(slideDown)
            isReload = false
        }

        hospitalSearch.setOnClickListener {
            showHospitalList.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_healthCareHospitalFragment_to_healthCareHospitalSearchFragment)
        }

        btnPurchase.setOnClickListener {
            showHospitalList.visibility = View.VISIBLE
            if(StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                if (StorageUtils.loadIntValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_MOBILE_CONFIRM
                    ) == 1
                ) {
                    findNavController().navigate(HealthCareHospitalFragmentDirections.actionHealthCareHospitalFragmentToPurchaseFragment())
                } else {
                    requireContext().startActivity<MobileAuthActivity>()
                }
            } else {
                requireContext().startActivity<LoginAndRegisterActivity>()
            }
        }

        inputDelete.setOnClickListener {
            it.visibility = View.GONE
            hospitalSearch.text = Helper.readStringRes(R.string.search_hint)

            layoutHospital.visibility = View.GONE
            showHospitalList.visibility = View.VISIBLE
            keywordLayoutWidth = 0
            keywordWidth = 0
            dataModel.keyword.value = ""

            Logger.d("select marker : $selectedMarker, select marker item : $selectedMarkerItem")
            changeSelectedMarker(null, selectedMarkerItem)

            loadHospitalList()
        }

        //===========================================================================================
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
                                if (pageTiggerPoint > 0 && lastItem > pageTiggerPoint) {
                                    isReload = true
                                    pageTiggerPoint = Int.MAX_VALUE

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

        btnPosition.setBackgroundResource(R.drawable.ic_map_position_select)

        layoutPurchase.apply {
            when {
                dataModel.isPurchseBtnShow.value == false -> {
                    visibility = View.GONE
                }

                else -> visibility = View.VISIBLE
            }
        }

        if (arguments?.getBoolean("hideHospitalList") == true) {
            hospitalLayout.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume")

        if (checkLocationPermission()) {
            getCurrentGPSLocation()
        } else {
            requestLocationPermission()
        }

        if (dataModel.searchMode.value!!) {
            selectedMarkerItem = dataModel.hospitalItem.value
            hospitalSearch.text = dataModel.keyword.value.toString()
            inputDelete.visibility = View.VISIBLE
            hospitalLayout.visibility = View.INVISIBLE
        }
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

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (selectedMarker != null && selectedMarkerItem != null) {
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
            HospitalListDialogFragment(this@HealthCareHospitalFragment).show(childFragmentManager, "Hospital")
        }

        return true
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
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

        hospitalMap?.isMyLocationEnabled = true
        hospitalMap?.uiSettings?.isMyLocationButtonEnabled = false

        if (dataModel.searchMode.value!! && selectedMarkerItem != null) {
            val position = LatLng(selectedMarkerItem!!.latitude, selectedMarkerItem!!.longitude)
            val center = CameraUpdateFactory.newLatLng(position)
            hospitalMap?.animateCamera(center)
        } else {
            hospitalMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLat, currentLng), 16f))
        }
    }

    override fun onCameraMove() {
        Logger.d("onCameraMove")
        if (isAdded) {
            btnPosition.setBackgroundResource(R.drawable.ic_map_position_default)
        }
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

        loadHospitalList()
    }

    override fun onSelectItem(item: HospitalItem) {
        selectedMarkerItem = item
        changeSelectedMarker(selectedMarker, item)
        setHospitalInfo(item)
    }

    override fun onBooking() {
        bundleOf(
            "centerId" to dataModel.hospitalItem.value!!.centerId,
            "petId" to dataModel.petId.value,
            "name" to dataModel.hospitalItem.value!!.name
        ).let {
            findNavController().navigate(
                R.id.action_healthCareHospitalFragment_to_healthCareBookingFragment,
                it
            )
        }
    }

    override fun onHospitalDetail() {
        bundleOf("centerId" to dataModel.hospitalItem.value!!.centerId).let {
            findNavController().navigate(
                R.id.action_healthCareHospitalFragment_to_healthCareHospitalDetailFragment,
                it
            )
        }
    }

    // ============================================================================================
    private fun onItemClicked(item: HospitalItem) {
        dataModel.hospitalItem.value = item

        if (dataModel.healthCareCode.value!!.isNotEmpty()) {
            HospitalSelectDialogFragment(this@HealthCareHospitalFragment).show(childFragmentManager, "Hospital Select")
        } else {
            bundleOf("centerId" to dataModel.hospitalItem.value!!.centerId).let {
                findNavController().navigate(
                    R.id.action_healthCareHospitalFragment_to_healthCareHospitalDetailFragment,
                    it
                )
            }
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

    private fun setHospitalInfo(item: HospitalItem) {
        layoutHospital.visibility = View.VISIBLE
        showHospitalList.visibility = View.GONE

        dataModel.centerId.value = item.centerId
        dataModel.telephone.value = item.telephone

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
            hospitalType.visibility = View.GONE
        }
    }

    private fun loadHospitalList() {
        try {
            viewLifecycleOwner.lifecycleScope.launch {
                val response = petdocRepository.fetchHospitalList(
                    HospitalListForm(
                        ownerLatitude = moveLat.toString(),
                        ownerLongitude = moveLng.toString(),
                        latitude = moveLat.toString(),
                        longitude = moveLng.toString(),
                        keyword = dataModel.keyword.value.toString(),
                        healthCheck = "Y",
                        petIdList = petIdList,
                        size = size,
                        page = page)
                )
                val items = response.resultData.center
                val totalCount = response.resultData.totalCount.toString()
                if (items.isNotEmpty()) {
                    refreshHospitalList(items, totalCount)
                }
            }
        } catch (e: Exception) {
            Logger.p(e)
        } catch (e: IllegalStateException) {
            Logger.p(e)
        }
    }

    private fun refreshHospitalList(items: List<HospitalItem>, totalCount: String) {
        if (!isReload) {
            hospitalItems.clear()
            hospitalItems.addAll(items)
            hospitalAdater.notifyDataSetChanged()
        } else {
            if (items.size > 0) {
                for (item in items) {
                    hospitalItems.add(item)
                    hospitalAdater.notifyItemInserted(hospitalItems.size - 1)
                }
            }
        }

        ++page
        pageTiggerPoint = hospitalItems.size - 4

        count.text = totalCount

        if (dataModel.searchMode.value!!) {
            dataModel.searchMode.value = false
            layoutHospital.visibility = View.VISIBLE
            showHospitalList.visibility = View.GONE

            setHospitalInfo(selectedMarkerItem!!)

            for (item in hospitalItems) {
                if (item.name == selectedMarkerItem?.name) {
                    selectedMarker = addMarker(item, true)
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

            selectedMarker = null
            selectedMarkerItem = null
        }

        Airbridge.trackEvent("booking", "click", "list", null, null, null)
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

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = requireContext().applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentGPSLocation() {
        try {
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
        } catch (e: Exception) {
            Logger.p(e)
        } catch (e: IllegalStateException) {
            Logger.p(e)
        }
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

    //==============================================================================================
    inner class HospitalAdapter : RecyclerView.Adapter<HospitalHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HospitalHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_health_care_hospital_list_item, parent, false)
            )

        override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
            holder.setName(hospitalItems[position].name)
            holder.setLocation(hospitalItems[position].location)
            holder.setDistance(hospitalItems[position].distance)
            holder.setImage(hospitalItems[position].mainImgUrl)
            holder.setStatus(hospitalItems[position].runStatus)

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
    }
}