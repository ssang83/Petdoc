package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalInfoEditFragment

/**
 * Petdoc
 * Class: HospitalInfoEditActivity
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
class HospitalInfoEditActivity : AppCompatActivity() {

    private var hospitalName = ""
    private var hospitalAddr = ""
    private var centerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_info_edit)

        hospitalName = intent?.getStringExtra("name") ?: hospitalName
        hospitalAddr = intent?.getStringExtra("address") ?: hospitalAddr
        centerId = intent?.getIntExtra("centerId", centerId) ?: centerId

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalInfoEditFragment().apply {
                bundleOf(
                    "name" to hospitalName,
                    "address" to hospitalAddr,
                    "centerId" to centerId
                ).let {
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