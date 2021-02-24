package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.hospital.HospitalReservationFragment

/**
 * Petdoc
 * Class: HospitalReservationActivity
 * Created by kimjoonsung on 2020/06/16.
 *
 * Description :
 */
class HospitalReservationActivity : BaseActivity() {

    private var centerId = -1
    private var bookingTab = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_reservation)

        mActivityList.add(this)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        bookingTab = intent?.getStringExtra("bookingTab") ?: bookingTab

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalReservationFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putBoolean("fromHome", true)
                    putString("bookingTab", bookingTab)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)
    }
}