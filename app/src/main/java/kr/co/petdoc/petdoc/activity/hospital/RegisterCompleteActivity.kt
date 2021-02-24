package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.RegisterCompleteFragment

/**
 * Petdoc
 * Class: RegisterCompleteActivity
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class RegisterCompleteActivity : AppCompatActivity() {

    private var registerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_complete)

        registerId = intent?.getIntExtra("bookingId", registerId)!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, RegisterCompleteFragment().apply {
                val bundle = Bundle().apply {
                    putInt("registerId", registerId)
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