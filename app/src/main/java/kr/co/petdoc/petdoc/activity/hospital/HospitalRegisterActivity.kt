package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalRegisterFragment

/**
 * Petdoc
 * Class: HospitalRegisterActivity
 * Created by kimjoonsung on 2020/06/15.
 *
 * Description :
 */
class HospitalRegisterActivity : AppCompatActivity() {

    private var centerId = -1
    private var name = ""
    private var location = ""
    private var fromHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_register)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        name = intent?.getStringExtra("name")!!
        location = intent?.getStringExtra("location")!!
        fromHome = intent?.getBooleanExtra("fromHome", fromHome)!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalRegisterFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putString("name", name)
                    putString("location", location)
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