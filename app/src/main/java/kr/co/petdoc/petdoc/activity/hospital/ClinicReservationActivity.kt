package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.hospital.ClinicReservationFragment
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: ClinicReservationActivity
 * Created by kimjoonsung on 2020/06/16.
 *
 * Description :
 */
class ClinicReservationActivity : BaseActivity() {

    private var centerId = -1
    private var fromHome = false
    private var petId = -1
    private var clinicId = ""
    private var message = ""
    private var hospitalName = ""
    private var bookingTab = ""
    private var petKind = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_reservation)

        mActivityList.add(this)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!
        petId = intent?.getIntExtra("petId", petId)!!
        clinicId = intent?.getStringExtra("clinicId")!!
        message = intent?.getStringExtra("msg")!!
        hospitalName = intent?.getStringExtra("name")!!
        bookingTab = intent?.getStringExtra("bookingTab") ?: bookingTab
        petKind = intent?.getStringExtra("petKind") ?: petKind

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ClinicReservationFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putInt("petId", petId)
                    putBoolean("fromHome", fromHome)
                    putString("clinicId", clinicId)
                    putString("msg", message)
                    putString("name", hospitalName)
                    putString("bookingTab", bookingTab)
                    putString("petKind", petKind)
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