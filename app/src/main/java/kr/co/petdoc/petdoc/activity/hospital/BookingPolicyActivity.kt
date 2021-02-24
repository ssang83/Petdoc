package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.BookingPolicyFragment

/**
 * Petdoc
 * Class: BookingPolicyActivity
 * Created by kimjoonsung on 2020/09/07.
 *
 * Description :
 */
class BookingPolicyActivity : AppCompatActivity() {

    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_policy)

        url = intent.getStringExtra("url") ?: url

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, BookingPolicyFragment().apply {
                val bundle = Bundle().apply {
                    putString("url", url)
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