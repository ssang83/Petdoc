package kr.co.petdoc.petdoc.activity.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_related_hospital.*
import kotlinx.android.synthetic.main.view_hospital_service.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.response.submodel.RelatedHospitalItem
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: RelatedHospitalActivity
 * Created by kimjoonsung on 2020/05/27.
 *
 * Description :
 */
class RelatedHospitalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var relatedHospitalItem:RelatedHospitalItem

    private var hospitalMap : GoogleMap? = null

    private var latitude = 0.0
    private var longitude = 0.0
    private var telePhoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_related_hospital)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        btnCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telePhoneNumber")))
        }

        btnTop.setOnClickListener { scrollView.scrollTo(0,0) }

        btnClose.setOnClickListener { onBackPressed() }

        //=========================================================================================
        relatedHospitalItem = intent?.getParcelableExtra("item")!!

        if (relatedHospitalItem.imageUrl!!.isNotEmpty()) {
            layoutBlankImg.visibility = View.GONE
            hospitalImg.visibility = View.VISIBLE

            Glide.with(this)
                .load( if(relatedHospitalItem.imageUrl!!.startsWith("http")) relatedHospitalItem.imageUrl!! else "${AppConstants.IMAGE_URL}${relatedHospitalItem.imageUrl!!}")
                .into(hospitalImg)
        } else {
            layoutBlankImg.visibility = View.VISIBLE
            hospitalImg.visibility = View.GONE
        }

        hospitalName.text = relatedHospitalItem.name

        if (relatedHospitalItem.addition!!.size > 0) {
            setService(relatedHospitalItem.addition!!)
        }

        serviceHour.apply {
            when {
                relatedHospitalItem.serviceHour!!.isNotEmpty() -> text = relatedHospitalItem.serviceHour!!
                else -> text = "모름"
            }
        }

        clinicSubject.apply {
            when {
                relatedHospitalItem.counsel!!.isNotEmpty() -> text = relatedHospitalItem.counsel!!
                else -> text = "모름"
            }
        }

        clinicPet.apply {
            when {
                relatedHospitalItem.kinds!!.isNotEmpty() -> text = relatedHospitalItem.kinds!!
                else -> text = "강아지"
            }
        }

        benefit.text = "병원에서 이벤트를 진행하고 있습니다.많은 관심 부탁드립니다."

        location.text = relatedHospitalItem.address!!

        latitude = relatedHospitalItem.latitude
        longitude = relatedHospitalItem.longitude

        telePhoneNumber = relatedHospitalItem.telephone!!

        //==========================================================================================
        supportFragmentManager.findFragmentById(R.id.hospitalMap)?.let{it->
            if(it is SupportMapFragment){
                it.getMapAsync(this)                // 꼭 지도 연결을 위해서 프래그먼트에 OnMapReadyCallback이 연동된 포인트를 연결하세요
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onMapReady(map: GoogleMap?) { //지도가 붙고나서,  위에서 세팅한 연동 지점으로 이 콜백이 호출됩니다.
        hospitalMap = map

        //지도 접근은 이 이벤트 콜백이 호출 된 이후부터 접근가능

        val HOSPITAL = LatLng(latitude, longitude)

        hospitalMap?.apply {
            val markerOptions = MarkerOptions().apply {
                title(relatedHospitalItem.name!!)
                position(HOSPITAL)
            }

            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLng(HOSPITAL))
            animateCamera(CameraUpdateFactory.zoomTo(16f))
        }
    }

    private fun setService(service: List<String>) {
        hospitalService.removeAllViews()

        for (i in 0 until service.size) {
            hospitalService.addView(
                HospitalService(
                    this,
                    service[i]
                )
            )
        }
    }

    //==============================================================================================
    inner class HospitalService(context: Context, service:String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_hospital_service, null, false)

            view.service.text = service

            addView(view)
        }
    }
}