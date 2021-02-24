package kr.co.petdoc.petdoc.fragment.hospital

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_hospital_location.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: HospitalLocationFragment
 * Created by kimjoonsung on 2020/09/03.
 *
 * Description :
 */
class HospitalLocationFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null

    private var latitude = ""
    private var longitude = ""
    private var bookingType = ""

    private var markerImage: AppCompatImageView? = null
    private var markerRoot: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_hospital_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        latitude = arguments?.getString("latitude")!!
        longitude = arguments?.getString("longitude")!!
        bookingType = arguments?.getString("bookingType")!!
        Logger.d("latitude : $latitude, longitude : $longitude, bookingType : $bookingType")

        //===========================================================================================

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        childFragmentManager.findFragmentById(R.id.map)?.let { it ->
            if (it is SupportMapFragment) {
                it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Logger.d("onMapReady, latitude : $latitude, longitude : $longitude")

        map = googleMap

        //지도 접근은 이 이벤트 콜백이 호출 된 이후부터 접근가능
        setCustomMarkerView()

        val HOSPITAL = LatLng(latitude.toDouble(), longitude.toDouble())

        when (bookingType) {
            BookingType.A.name,
            BookingType.R.name,
            BookingType.B.name -> {
                markerImage?.setBackgroundResource(R.drawable.img_hospital_reservation)
            }

            else -> markerImage?.setBackgroundResource(R.drawable.img_hospital_normal)
        }

        map?.apply {
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

}