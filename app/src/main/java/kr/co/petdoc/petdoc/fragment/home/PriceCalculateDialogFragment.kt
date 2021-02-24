package kr.co.petdoc.petdoc.fragment.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.dialog_clinic_price.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.HospitalActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionCallback
import kr.co.petdoc.petdoc.utils.PermissionStatus
import kr.co.petdoc.petdoc.viewmodel.ClinicPriceDataModel

/**
 * Petdoc
 * Class: PriceCalculateDialogFragment
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class PriceCalculateDialogFragment(callback: CallbackListener) : DialogFragment() {

    private val TEN_THOUNDS = 10000
    private val THOUNDS = 1000

    private lateinit var clinicDataModel: ClinicPriceDataModel
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var callbackListener = callback

    private var minValue = 0
    private var maxValue = 0

    private var latitude = ""
    private var longitude = ""

    interface CallbackListener {
        fun onDissmiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullDialog)
        clinicDataModel = ViewModelProvider(requireActivity()).get(ClinicPriceDataModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.BOTTOM)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_clinic_price, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        minValue = clinicDataModel.priceMin.value!!
        maxValue = clinicDataModel.priceMax.value!!

        Logger.d("minValue : $minValue, maxValue : $maxValue")

        btnHospital.setOnClickListener {
//            startActivity(Intent(requireActivity(), HospitalActivity::class.java).apply {
//                putExtra("latitude", latitude)
//                putExtra("longitude", longitude)
//            })

            callbackListener.onDissmiss()
        }

        //============================================================================================

        animationArea.apply{
            setAnimation(R.raw.money)
        }

        animationArea.playAnimation()

        caluatePrice()
        getCurrentGPSLocation()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        animationArea.cancelAnimation()
    }

    private fun caluatePrice() {
        if (minValue == maxValue) {
            val unit = minValue.div(TEN_THOUNDS)
            val temp = minValue.rem(TEN_THOUNDS)
            val unit1 = temp.div(THOUNDS)

            if (unit1 == 0) {
                priceRange.text = "${unit}만원"
            } else {
                priceRange.text = "${unit}만 ${unit1}천원"
            }
        } else {
            val minUnit = minValue.div(TEN_THOUNDS)
            val temp = minValue.rem(TEN_THOUNDS)
            val minUnit1 = temp.div(THOUNDS)

            val maxUnit = maxValue.div(TEN_THOUNDS)
            val temp1 = maxValue.rem(TEN_THOUNDS)
            val maxUnit1 = temp1.div(THOUNDS)

            if (minUnit1 == 0 && maxUnit1 == 0) {
                priceRange.text = "${minUnit}만원 ~ ${maxUnit}만원"
            } else if (minUnit1 == 0) {
                priceRange.text = "${minUnit}만원 ~ ${maxUnit}만 ${maxUnit1}천원"
            } else if (maxUnit1 == 0) {
                priceRange.text = "${minUnit}만 ${minUnit1}천원 ~ ${maxUnit}만원"
            } else {
                priceRange.text = "${minUnit}만 ${minUnit1}천원 ~ ${maxUnit}만 ${maxUnit1}천원"
            }
        }
    }

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

                                        latitude = location.latitude.toString()
                                        longitude = location.longitude.toString()
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

            latitude = mLastLocation.latitude.toString()
            longitude = mLastLocation.longitude.toString()
        }
    }
}