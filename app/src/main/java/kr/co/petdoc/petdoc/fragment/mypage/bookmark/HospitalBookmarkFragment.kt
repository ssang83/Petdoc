package kr.co.petdoc.petdoc.fragment.mypage.bookmark

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.fragment_bookmark_hospital.*
import kotlinx.android.synthetic.main.fragment_bookmark_hospital.hospitalList
import kotlinx.android.synthetic.main.fragment_pet_home.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.HospitalDetailActivity
import kr.co.petdoc.petdoc.adapter.mypage.bookmark.HospitalAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalBookmarkListResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalBookmarkData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionCallback
import kr.co.petdoc.petdoc.utils.PermissionStatus
import kr.co.petdoc.petdoc.utils.TextAddressFromGPS
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: HospitalBookmarkFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 병원 북마크 화면
 */
class HospitalBookmarkFragment : BaseFragment(), HospitalAdapter.AdapterListener {

    private val LOGTAG = "HospitalBookmarkFragment"
    private val BOOKMARK_LIST_REQUEST = "$LOGTAG.bookmarkListRequest"

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    lateinit var mAdapter: HospitalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = HospitalAdapter().apply {
            setListener(this@HospitalBookmarkFragment)
        }

        hospitalList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        getCurrentGPSLocation()
    }

    override fun onItemClicked(item:HospitalBookmarkData) {
        startActivity(Intent(requireActivity(), HospitalDetailActivity::class.java).apply {
            putExtra("centerId", item.centerId)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(BOOKMARK_LIST_REQUEST)) {
            mApiClient.cancelRequest(BOOKMARK_LIST_REQUEST)
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            BOOKMARK_LIST_REQUEST -> {
                if (response is HospitalBookmarkListResponse) {
                    if (response.code == "SUCCESS") {
                        if (response.resultData.size > 0) {
                            hospitalList.visibility = View.VISIBLE
                            textViewBookmarkEmpty.visibility = View.GONE

                            mAdapter.updateData(response.resultData)
                        } else {
                            hospitalList.visibility = View.GONE
                            textViewBookmarkEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Logger.d("error : ${response.messageKey}")
                    }
                }
            }
        }
    }

    //==============================================================================================
    private fun getCurrentGPSLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        Helper.permissionCheck(requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            object : PermissionCallback {
                override fun callback(status: PermissionStatus) {
                    when(status){
                        PermissionStatus.ALL_GRANTED -> {
                            if (isLocationEnabled()) {
                                mFusedLocationClient!!.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                                    val location: Location? = task.result
                                    if (location == null) {
                                        requestNewLocationData()
                                    } else {
                                        Logger.d("latitude : ${location.latitude}, longitude : ${location.longitude}")
                                        mApiClient.getHospitalBookmarkList(
                                            BOOKMARK_LIST_REQUEST,
                                            location.latitude,
                                            location.longitude
                                        )
                                    }
                                }
                            } else {
                                Toast.makeText(requireActivity(), "Turn on location", Toast.LENGTH_LONG).show()
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                startActivity(intent)
                            }
                        }
                        PermissionStatus.DENIED_SOME -> {
                            //finish()
                        }
                    }
                }
            },true )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = requireContext().applicationContext.getSystemService(
            Context.LOCATION_SERVICE) as LocationManager
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

            mApiClient.getHospitalBookmarkList(
                BOOKMARK_LIST_REQUEST,
                mLastLocation.latitude,
                mLastLocation.longitude
            )
        }
    }
}