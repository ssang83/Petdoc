package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.BeautyReservationFragment

/**
 * Petdoc
 * Class: BeautyReservationActivity
 * Created by kimjoonsung on 2020/06/18.
 *
 * Description :
 */
class BeautyReservationActivity : AppCompatActivity() {

    private var centerId = -1
    private var fromHome = false
    private var petId = -1
    private var message = ""
    private var hospitalName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beauty_reservation)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!
        petId = intent?.getIntExtra("petId", petId)!!
        message = intent?.getStringExtra("msg")!!
        hospitalName = intent?.getStringExtra("name")!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, BeautyReservationFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putInt("petId", petId)
                    putBoolean("fromHome", fromHome)
                    putString("msg", message)
                    putString("name", hospitalName)
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