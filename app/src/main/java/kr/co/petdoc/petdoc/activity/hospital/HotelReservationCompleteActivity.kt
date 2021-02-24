package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HotelReservationCompleteFragment

/**
 * Petdoc
 * Class: HotelReservationCompleteActivity
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class HotelReservationCompleteActivity : AppCompatActivity() {

    private var bookingId = -1
    private var fromHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_reservation_complete)

        bookingId = intent?.getIntExtra("bookingId", bookingId)!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HotelReservationCompleteFragment().apply {
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
        finish()
    }
}