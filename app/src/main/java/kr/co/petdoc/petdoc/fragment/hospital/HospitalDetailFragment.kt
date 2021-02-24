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
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.adapter_hospital_detail_doctor_item.view.*
import kotlinx.android.synthetic.main.adapter_hospital_price_item.view.*
import kotlinx.android.synthetic.main.fragment_hospital_detail.*
import kotlinx.android.synthetic.main.hospital_detail_image_item.view.*
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.hospital.*
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalDetailResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.*
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: HospitalDetailFragment
 * Created by kimjoonsung on 2020/06/09.
 *
 * Description :
 */
class HospitalDetailFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private val TAG = "HospitalDetailFragment"
    private val HOSPITAL_DETAIL_REQUEST = "${TAG}.hospitalDetailRequest"
    private val HOSPITAL_ADD_BOOKMARK_REQUEST = "${TAG}.hospitalAddBookmarkRequest"
    private val HOSPITAL_CANCEL_BOOKMARK_REQUEST = "${TAG}.hospitalCancelBookmarkRequest"
    private val HOSPITAL_REQUIRE_BOOKING_REQUEST = "$TAG.hospitalRequireBookingRequest"
    private val HOSPITAL_BOOKMARK_STATUS_REQUEST = "${TAG}.hospitalBookmarkStatusRequest"

    private val DOUBLE_QOUTE = "&#39;"

    private lateinit var imageAdapter: ImageAdapter
    private var hospitalImagItems: MutableList<Img> = mutableListOf()

    private var doctorAdapter:DoctorAdapter? = null
    private var doctorItems:MutableList<Vet> = mutableListOf()

    private lateinit var priceAdapter: PriceAdapter
    private var priceItems:MutableList<PriceItem> = mutableListOf()

    private var hospitalMap: GoogleMap? = null

    private lateinit var dataModel: HospitalDataModel

    private var centerId = -1
    private var phoneNumber = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var bookingType: String = ""

    private var markerImage: AppCompatImageView? = null
    private var markerRoot: View? = null

    private var totalCount = 0

    private var hospitalName = ""
    private var address = ""
    private var fromHome = false
    private var clinicTimeMap:MutableMap<String, String> = mutableMapOf()

    private var margin16 = 0
    private var bookingTab = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        margin16 = Helper.convertDpToPx(requireContext(), 16f).toInt()

        centerId = arguments?.getInt("centerId") ?: centerId
        fromHome = arguments?.getBoolean("fromHome") ?: fromHome
        bookingTab = arguments?.getString("bookingTab") ?: bookingTab
        Logger.d("centerId : $centerId, fromHome : $fromHome, bookingTab : $bookingTab")

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        layoutHospitalCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber}")))
        }

        layoutHospitalCall1.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber}")))
        }

        layoutBookmarkBtn.setOnClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                if (!btnBookmark.isSelected) {
                    mApiClient.addBookmarkHospital(HOSPITAL_ADD_BOOKMARK_REQUEST, centerId)
                } else {
                    mApiClient.cancelBookmarkHospital(HOSPITAL_CANCEL_BOOKMARK_REQUEST, centerId)
                }
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        layoutHospitalRegister.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://partner.petdoc.co.kr/serviceApply")))
        }

        layoutHospitalEdit.setOnClickListener {
            if (!fromHome) {
                bundleOf(
                    "centerId" to centerId,
                    "name" to hospitalName,
                    "address" to address
                ).let {
                    findNavController().navigate(
                        R.id.action_hospitalDetailFragment_to_hospitalInfoEditFragment,
                        it
                    )
                }
            } else {
                startActivity(
                    Intent(
                        requireActivity(),
                        HospitalInfoEditActivity::class.java
                    ).apply {
                        putExtra("name", hospitalName)
                        putExtra("address", address)
                        putExtra("centerId", centerId)
                    })
            }
        }

        layoutDoctor.setOnClickListener {
            if (!fromHome) {
                findNavController().navigate(R.id.action_hospitalDetailFragment_to_doctorFragment)
            } else {
                startActivity(
                    Intent(
                        requireActivity(),
                        DoctorActivity::class.java
                    ).apply {
                        putExtra("fromHome", fromHome)
                        putExtra("centerId", centerId)
                    })
            }
        }

        btnPetdocBookingRequest.setOnClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                TwoBtnDialog(requireContext()).apply {
                    setTitle(hospitalName)
                    setMessage("펫닥 예약 기능은 동물병원에서 기다림 없이 편리하게 진료를 받을 수 있는 기능입니다.\n해당 동물병원의 펫닥 예약 기능을\n요청하시겠습니까?")
                    setConfirmButton("예", View.OnClickListener {
                        if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                            dismiss()
                            mApiClient.postHospitalBookingRequire(HOSPITAL_REQUIRE_BOOKING_REQUEST, centerId)
                        } else {
                            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                        }
                    })
                    setCancelButton("아니오", View.OnClickListener {
                        dismiss()
                    })
                }.show()
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }

        btnOnlyReservation.setOnClickListener(clickListener)
        //==========================================================================================

        mApiClient.getHospitalDetail(HOSPITAL_DETAIL_REQUEST, centerId)
        mApiClient.getHospitalBookmarkStatus(HOSPITAL_BOOKMARK_STATUS_REQUEST, centerId)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(HOSPITAL_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_DETAIL_REQUEST)
        }

        if (mApiClient.isRequestRunning(HOSPITAL_ADD_BOOKMARK_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_ADD_BOOKMARK_REQUEST)
        }

        if (mApiClient.isRequestRunning(HOSPITAL_CANCEL_BOOKMARK_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_CANCEL_BOOKMARK_REQUEST)
        }

        if (mApiClient.isRequestRunning(HOSPITAL_REQUIRE_BOOKING_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_REQUIRE_BOOKING_REQUEST)
        }
        super.onDestroyView()
    }

    override fun onMapReady(map: GoogleMap?) {
        Logger.d("onMapReady, latitude : $latitude, longitude : $longitude")

        hospitalMap = map

        //지도 접근은 이 이벤트 콜백이 호출 된 이후부터 접근가능
        setCustomMarkerView()

        hospitalMap?.setOnMapClickListener(this)

        val HOSPITAL = LatLng(latitude, longitude)

        when (bookingType) {
            BookingType.A.name,
            BookingType.R.name,
            BookingType.B.name -> {
                markerImage?.setBackgroundResource(R.drawable.img_hospital_reservation)
            }

            else -> markerImage?.setBackgroundResource(R.drawable.img_hospital_normal)
        }

        hospitalMap?.apply {
            val markerOptions = MarkerOptions().apply {
                position(HOSPITAL)
                icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createDrawableFromView(
                            requireContext(),
                            markerRoot!!
                        )
                    )
                )
            }

            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLngZoom(HOSPITAL, 16f))
        }
    }

    override fun onMapClick(latLng: LatLng?) {
        if (!fromHome) {
            findNavController().navigate(R.id.action_hospitalDetailFragment_to_hospitalLocationFragment)
        } else {
            startActivity(
                Intent(
                    requireActivity(),
                    HospitalLocationActivity::class.java
                ).apply {
                    putExtra("latitude", latitude.toString())
                    putExtra("longitude", longitude.toString())
                    putExtra("bookingType", bookingType)
                })
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    /**
     * response
     *
     * @param response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            HOSPITAL_DETAIL_REQUEST -> {
                if (response is HospitalDetailResponse) {
                    if (response.resultData.animalList.size == 0
                        && response.resultData.clinicList.size == 0
                        && response.resultData.centerTime == null
                    ) {
                        laoutPetdocBookingRequest.visibility = View.VISIBLE
                        layoutHospitalRegister.visibility = View.VISIBLE
                        divider.visibility = View.VISIBLE
                        layoutCategory.visibility = View.GONE
                        layoutDescription.visibility = View.GONE
                        layoutTime.visibility = View.GONE
                        layoutDoctorIntroduce.visibility = View.GONE
                        layoutTreatment.visibility = View.GONE
                        layoutAnimal.visibility = View.GONE
                        layoutHospitalEdit.visibility = View.GONE
                        layoutButton.visibility = View.GONE
                        btnTop.visibility = View.GONE
                    } else {
                        layoutHospitalRegister.visibility = View.GONE
                        divider.visibility = View.GONE
                        layoutTime.visibility = View.VISIBLE
                        layoutTreatment.visibility = View.VISIBLE
                        layoutAnimal.visibility = View.VISIBLE
                        layoutHospitalEdit.visibility = View.VISIBLE
                        btnTop.visibility = View.VISIBLE

                        setCenterTime(response.resultData.centerTime!!)
                    }

                    setHospitalData(response.resultData)
                    setDoctorList(response.resultData.vetList)
                    setKeyworkList(response.resultData.keywordList)
                    setClinicPrice(response.resultData.expenseList)

                    hospitalTitle.text = response.resultData.name
                    name.text = response.resultData.name
                    location.text = response.resultData.location
                    location1.text = response.resultData.location

                    phoneNumber = response.resultData.telephone
                    latitude = response.resultData.latitude
                    longitude = response.resultData.longitude
                    bookingType = response.resultData.bookingType

                    if (response.resultData.imgList.size > 0) {
                        layoutHospitalImage.visibility = View.VISIBLE

                        imageAdapter = ImageAdapter()
                        hospitalImagePager.apply {
                            adapter = imageAdapter
                            orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        }

                        hospitalImagItems.addAll(response.resultData.imgList)
                        hospitalImagePager.offscreenPageLimit = response.resultData.imgList.size

                        totalCount = response.resultData.imgList.size
                        count.text = "1 / ${totalCount}"

                        hospitalImagePager.registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                super.onPageSelected(position)
                                count.text = "${position + 1} / ${totalCount}"
                            }
                        })

                        dataModel.hospitalImageItems.value =
                            response.resultData.imgList as MutableList<Img>
                    } else {
                        layoutHospitalImage.visibility = View.GONE
                    }

                    childFragmentManager.findFragmentById(R.id.hospitalMap)?.let { it ->
                        if (it is SupportMapFragment) {
                            it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
                        }
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            HOSPITAL_ADD_BOOKMARK_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if("SUCCESS" == code) {
                        btnBookmark.isSelected = true
                    }
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            HOSPITAL_CANCEL_BOOKMARK_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        btnBookmark.isSelected = false
                    }
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            HOSPITAL_REQUIRE_BOOKING_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        AppToast(requireContext()).showToastMessage(
                            "펫닥 예약 기능 요청이 완료되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

//                        btnPetdocBookingRequest.setBackgroundResource(R.drawable.pale_grey_solid_6)
//                        btnPetdocBookingRequest.setTextColor(Helper.readColorRes(R.color.dark_grey))
//                        btnPetdocBookingRequest.isEnabled = false
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            "펫닥 예약 기능 요청이 실패되었습니다.\n다시 시도해 주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }

            HOSPITAL_BOOKMARK_STATUS_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        btnBookmark.isSelected = true
                    } else {
                        btnBookmark.isSelected = false
                    }
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }
        }
    }

    //==============================================================================================

    private fun setCenterTime(centerTime: CenterTime) {
        checkCenterTime(centerTime)

        if (centerTime.restYn == "Y") {
            lunchTime.text = "진료가능"
        } else {
            lunchTime.text = "진료불가"
        }

        if (centerTime.sunYn == "Y") {
            holidayTime.text = "진료가능"
        } else {
            holidayTime.text = "휴진"
        }

        if (centerTime.satYn == "Y") {
            if (centerTime.satAllDayYn == "Y") {
                weekEndTime.text = "24시"
            } else {
                weekEndTime.text = "${centerTime.satStart} ~ ${centerTime.satEnd}"
            }
        } else {
            weekEndTime.text = "휴진"
        }

        if (centerTime.monAllDayYn == "Y" && centerTime.tueAllDayYn == "Y" && centerTime.wedAllDayYn == "Y" && centerTime.thuAllDayYn == "Y" && centerTime.friAllDayYn == "Y") {
            clinicTime.text = "24시"
        } else {
            if (clinicTimeMap.size == 5) {
                clinicTime.text = clinicTimeMap["mon"]
            } else {
                val week = mutableListOf<String>()
                val time = mutableListOf<String>()
                if (clinicTimeMap.containsKey("mon")) {
                    week.add("월")
                    time.add(clinicTimeMap["mon"].toString())
                }

                if (clinicTimeMap.containsKey("tue")) {
                    week.add("화")
                    time.add(clinicTimeMap["tue"].toString())
                }

                if (clinicTimeMap.containsKey("wed")) {
                    week.add("수")
                    time.add(clinicTimeMap["wed"].toString())
                }

                if (clinicTimeMap.containsKey("thu")) {
                    week.add("목")
                    time.add(clinicTimeMap["thu"].toString())
                }

                if (clinicTimeMap.containsKey("fri")) {
                    week.add("금")
                    time.add(clinicTimeMap["fri"].toString())
                }

                clinicTime.text = "${week}, ${time[0]}"
            }
        }
    }

    private fun checkCenterTime(centerTime: CenterTime) {
        if (centerTime.monYn == "Y") {
            clinicTimeMap.put("mon", "${centerTime.monStart} ~ ${centerTime.monEnd}")
        }

        if (centerTime.tueYn == "Y") {
            clinicTimeMap.put("tue", "${centerTime.tueStart} ~ ${centerTime.tueEnd}")
        }

        if (centerTime.wedYn == "Y") {
            clinicTimeMap.put("wed", "${centerTime.wedStart} ~ ${centerTime.wedEnd}")
        }

        if (centerTime.thuYn == "Y") {
            clinicTimeMap.put("thu", "${centerTime.thuStart} ~ ${centerTime.thuEnd}")
        }

        if (centerTime.friYn == "Y") {
            clinicTimeMap.put("fri", "${centerTime.friStart} ~ ${centerTime.friEnd}")
        }
    }

    private fun setHospitalData(data: HospitaDetailData) {
        layoutDescription.apply {
            when {
                data.description.isNullOrEmpty() -> visibility = View.GONE

                else -> {
                    visibility = View.VISIBLE
                    description.text = data.description!!.replace(DOUBLE_QOUTE, "\"")
                }
            }
        }

        val types = mutableListOf<String>()
        if ("Y" == data.clinicYn) {
            types.add("진료")
            layoutClinic.visibility = View.VISIBLE
        }

        if ("Y" == data.beautyYn) {
            types.add("미용")
            layoutBeauty.visibility = View.VISIBLE
        }

        if ("Y" == data.hotelYn) {
            types.add("호텔")
            layoutHotel.visibility = View.VISIBLE
        }

        if ("Y" == data.parkingYn) {
            types.add("주차")
            layoutParking.visibility = View.VISIBLE
        }

        if ("Y" == data.allDayYn) {
            types.add("24")
            layoutAllday.visibility = View.VISIBLE
        }

        if(types.size > 0) {
            layoutCategory.visibility = View.VISIBLE
        } else {
            layoutCategory.visibility = View.GONE
        }

        setBookingType(data.bookingType)

        layoutTreatment.apply {
            when {
                data.clinicList.size > 0 -> {
                    visibility = View.VISIBLE
                    setClinic(data.clinicList)
                }

                else -> visibility = View.GONE
            }
        }

        layoutAnimal.apply {
            when {
                data.animalList.size > 0 -> {
                    visibility = View.VISIBLE
                    setClinicAnimal(data.animalList)
                }

                else -> visibility = View.GONE
            }
        }

        hospitalName = data.name
        centerId = data.centerId
        address = data.location
    }

    private fun setBookingType(type: String) {
        when (type) {
            BookingType.A.name -> {
                laoutPetdocBookingRequest.visibility = View.GONE
                layoutButton.visibility = View.VISIBLE
                layoutHospitalRegister.visibility = View.GONE
                divider.visibility = View.GONE
            }

            BookingType.B.name -> {
                laoutPetdocBookingRequest.visibility = View.GONE
                layoutButton.visibility = View.VISIBLE
                layoutHospitalRegister.visibility = View.GONE
                divider.visibility = View.GONE
            }

            else -> {
                laoutPetdocBookingRequest.visibility = View.VISIBLE
                layoutButton.visibility = View.GONE
                layoutHospitalRegister.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE
            }
        }
    }

    private fun setClinic(clinicList: List<Clinic>) {
        val sb = StringBuilder()
        for (i in 0 until clinicList.size) {
            sb.append(clinicList[i].name)

            if (i != clinicList.size - 1) {
                sb.append(", ")
            } else {
                break
            }
        }

        clinicTreatment.text = sb.toString()
    }

    private fun setClinicAnimal(animalList: List<Animal>) {
        val sb = StringBuilder()
        for (i in 0 until animalList.size) {
            sb.append(animalList[i].name)

            if (i != animalList.size - 1) {
                sb.append(", ")
            } else {
                break
            }
        }

        clinicAnimal.text = sb.toString()
    }

    private fun setDoctorList(doctors: List<Vet>) {
        if (doctors.size > 0) {
            layoutDoctorIntroduce.visibility = View.VISIBLE

            doctorItems.addAll(doctors)
            doctorAdapter = DoctorAdapter()
            doctorList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                isNestedScrollingEnabled = false
                adapter = doctorAdapter
            }

            dataModel.doctorItems.value = doctorItems
        } else {
            layoutDoctorIntroduce.visibility = View.GONE
        }
    }

    private fun setKeyworkList(keywords: List<String>) {
        if (keywords.size > 0) {
            hospitalKeyword.visibility = View.VISIBLE
            hospitalKeyword.removeAllViews()

            for (i in 0 until keywords.size) {
                hospitalKeyword.addView(
                    HospitalKeyword(
                        requireContext(),
                        keywords[i]
                    )
                )
            }
        } else {
            hospitalKeyword.visibility = View.GONE
        }
    }

    private fun setClinicPrice(items:List<PriceItem>) {
        if (items.size > 0) {
            layoutPrice.visibility = View.VISIBLE

            priceItems.addAll(items)
            dataModel.priceItems.value = priceItems

            if (priceItems.size > 7) {
                layoutPriceAll.apply {
                    visibility = View.VISIBLE
                    setOnClickListener(clickListener)
                }
            } else {
                layoutPriceAll.visibility = View.GONE
            }

            priceAdapter = PriceAdapter()
            priceList.apply { adapter = priceAdapter }
        } else {
            layoutPrice.visibility = View.GONE
        }
    }

    private fun setCustomMarkerView() {
        markerRoot = layoutInflater.inflate(R.layout.hospital_detail_marker, null)
        markerImage = markerRoot!!.findViewById(R.id.marker)
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

    private fun onImageClicked(item:Img) {
        if (item.videoYn == "N") {
            startActivity(Intent(requireActivity(), HospitalImageDetailActivity::class.java).apply {
                putExtra("id", centerId)
            })
        } else {
            if (item.videoStatusYn == "Y") {
                if (!fromHome) {
                    findNavController().navigate(R.id.action_hospitalDetailFragment_to_hospitalVideoFragment)
                } else {
                    startActivity(Intent(requireActivity(), HospitalVideoActivity::class.java).apply {
                        putExtra("videoUrl", item.videoUrl)
                    })
                }
            } else {
                AppToast(requireContext()).showToastMessage(
                    "동영상 인코딩 중입니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }
    }

    //==============================================================================================
    inner class ImageAdapter : RecyclerView.Adapter<ImageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ImageHolder(layoutInflater.inflate(R.layout.hospital_detail_image_item, parent, false))

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            holder.setImage(hospitalImagItems[position].imgUrl)

            if (hospitalImagItems[position].videoYn == "Y") {
                holder.itemView.videoPlay.visibility = View.VISIBLE
            } else {
                holder.itemView.videoPlay.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { onImageClicked(hospitalImagItems[position]) }
        }

        override fun getItemCount() = hospitalImagItems.size
    }

    inner class ImageHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            Glide.with(requireContext()).load(url).into(item.hospitalImg)
        }
    }

    //=============================================================================================
    inner class DoctorAdapter : RecyclerView.Adapter<DoctorHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DoctorHolder(layoutInflater.inflate(R.layout.adapter_hospital_detail_doctor_item, parent, false))

        override fun onBindViewHolder(holder: DoctorHolder, position: Int) {
            holder.setImage(doctorItems[position].imgUrl)

            val name = doctorItems[position].name
            val vet = doctorItems[position].position
            holder.setName("${name}${vet}")

            holder.setDoctorClinicType(doctorItems[position].keyword)
        }

        override fun getItemCount() = doctorItems.size
    }

    inner class DoctorHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            item.doctorImg.apply {
                when {
                    url.isNotEmpty() -> {
                        Glide.with(requireContext())
                            .load(if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}${url}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.doctorImg)
                    }

                    else -> {
                        Glide.with(requireContext())
                            .load(R.drawable.img_profile)
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.doctorImg)
                    }
                }
            }
        }

        fun setName(name: String) {
            item.doctorName.text = name
        }

        fun setDoctorClinicType(type: List<String>) {
            item.clinicType.removeAllViews()

            for (i in 0 until type.size) {
                item.clinicType.addView(DoctorClinicType(type[i]))
            }
        }
    }

    inner class PriceAdapter : RecyclerView.Adapter<PriceHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PriceHolder(layoutInflater.inflate(R.layout.adapter_hospital_price_item, parent, false))

        override fun onBindViewHolder(holder: PriceHolder, position: Int) {
            if (position != 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    topMargin = margin16
                }
            }

            holder.setItem(priceItems[position].name!!)
            holder.setPrice(Helper.ToPriceFormat(priceItems[position].price))
        }

        override fun getItemCount() = if(priceItems.size > 7) {
            7
        } else {
            priceItems.size
        }
    }

    inner class PriceHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setItem(_item: String) {
            item.clinicItem.text = _item
        }

        fun setPrice(_price: String) {
            item.clinicPrice.text = "${_price}원"
        }
    }

    //==============================================================================================
    inner class DoctorClinicType(type:String) : FrameLayout(requireContext()) {

        init {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_doctor_clinic_type, null, false)

            view.type.text = type

            addView(view)
        }
    }

    class HospitalKeyword(context: Context, type:String) : FrameLayout(context) {

        init {
            val view = LayoutInflater.from(context).inflate(R.layout.view_hospital_type, null, false)

            view.type.text = type

            addView(view)
        }
    }

    //==============================================================================================
    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.btnOnlyReservation -> {
                if (StorageUtils.loadBooleanValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_LOGIN_STATUS
                    )
                ) {
                    if (!fromHome) {
                        bundleOf(
                            "centerId" to centerId
                        ).let {
                            findNavController().navigate(
                                R.id.action_hospitalSearchMapFragment_to_hospitalReservationFragmet,
                                it
                            )
                        }
                    } else {
                        startActivity(
                            Intent(
                                requireActivity(),
                                HospitalReservationActivity::class.java
                            ).apply {
                                putExtra("centerId", centerId)
                                putExtra("bookingTab", bookingTab)
                            })
                    }

                    FirebaseAPI(requireActivity()).logEventFirebase(
                        "병원예약_예약버튼",
                        "Click Event",
                        "병원 상세페이지에서 예약버튼 클릭"
                    )
                } else {
                    startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                }
            }

            R.id.layoutPriceAll -> {
                if (!fromHome) {
                    findNavController().navigate(R.id.action_hospitalDetailFragment_to_hospitalPriceFragment)
                } else {
                    startActivity(Intent(requireActivity(), HospitalPriceActivity::class.java).apply {
                        putParcelableArrayListExtra("priceItems", ArrayList(priceItems))
                    })
                }
            }
        }
    }
}