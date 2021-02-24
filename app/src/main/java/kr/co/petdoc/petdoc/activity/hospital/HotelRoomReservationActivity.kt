package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HotelRoomSelectFragment

/**
 * Petdoc
 * Class: HotelRoomReservationActivity
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class HotelRoomReservationActivity : AppCompatActivity() {

    private var centerId = -1
    private var fromHome = false
    private var petId = -1
    private var hospitalName = ""
    private var enterDate = ""
    private var outDate = ""
    private var enterTime = ""
    private var outTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beauty_reservation)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!
        petId = intent?.getIntExtra("petId", petId)!!
        hospitalName = intent?.getStringExtra("name")!!
        enterDate = intent?.getStringExtra("enterDate")!!
        outDate = intent?.getStringExtra("outDate")!!
        enterTime = intent?.getStringExtra("enterTime")!!
        outTime = intent?.getStringExtra("outTime")!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HotelRoomSelectFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putInt("petId", petId)
                    putBoolean("fromHome", fromHome)
                    putString("name", hospitalName)
                    putString("enterDate", enterDate)
                    putString("outDate", outDate)
                    putString("enterTime", enterTime)
                    putString("outTime", outTime)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}