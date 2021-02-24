package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalDetailImageFragment

/**
 * Petdoc
 * Class: HospitalImageDetailActivity
 * Created by kimjoonsung on 2020/06/10.
 *
 * Description :
 */
class HospitalImageDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_image_detail)

        val id = intent?.getIntExtra("id", -1)!!
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalDetailImageFragment().apply {
                val bundle = Bundle().apply {
                    putInt("id", id)
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