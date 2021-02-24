package kr.co.petdoc.petdoc.activity.hospital

import android.content.Intent
import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.fragment.hospital.ClinicReservationCompleteFragment
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: ClinicReservationCompleteActivity
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class ClinicReservationCompleteActivity : BaseActivity() {

    private var bookingId = -1
    private var fromHome = false
    private var bookingTab = ""
    private var isClickedMap = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_reservation_complete)
        instance = this
        mActivityList.add(this)

        bookingId = intent?.getIntExtra("bookingId", bookingId)!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!
        bookingTab = intent?.getStringExtra("bookingTab") ?: bookingTab

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ClinicReservationCompleteFragment().apply {
                val bundle = Bundle().apply {
                    putInt("bookingId", bookingId)
                    putBoolean("fromHome", fromHome)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        activityFinish()    // MainActivity로 이동하면 스택에 쌓여 있는 모든 Activity를 종료한다.
        if (!isClickedMap) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", bookingTab)
            })
        } else {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", "hospital")
            })
        }
    }

    fun clickedOnMap() {
        this.isClickedMap = true
        onBackPressed()
    }

    fun finishAllActivity() {
        activityFinish()
    }

    companion object {
        lateinit var instance:ClinicReservationCompleteActivity
    }
}