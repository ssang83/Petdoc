package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalLocationFragment

/**
 * Petdoc
 * Class: HospitalLocationActivity
 * Created by kimjoonsung on 2020/09/03.
 *
 * Description :
 */
class HospitalLocationActivity : AppCompatActivity() {

    private var latitude = ""
    private var longitude = ""
    private var bookingType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_location)

        latitude = intent?.getStringExtra("latitude") ?: latitude
        longitude = intent?.getStringExtra("longitude") ?: longitude
        bookingType = intent?.getStringExtra("bookingType") ?: bookingType

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalLocationFragment().apply {
                bundleOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "bookingType" to bookingType
                ).let {
                    arguments = it
                }
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}