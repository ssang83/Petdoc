package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.DoctorFragment

/**
 * Petdoc
 * Class: DoctorActivity
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
class DoctorActivity : AppCompatActivity() {

    private var fromHome = false
    private var centerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        fromHome = intent?.getBooleanExtra("fromHome", fromHome) ?: fromHome
        centerId = intent?.getIntExtra("centerId", centerId) ?: centerId

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, DoctorFragment().apply {
                bundleOf("fromHome" to fromHome, "centerId" to centerId).let {
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