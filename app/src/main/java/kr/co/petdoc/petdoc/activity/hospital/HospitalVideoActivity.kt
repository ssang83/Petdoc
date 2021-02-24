package kr.co.petdoc.petdoc.activity.hospital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.hospital.HospitalVideoFragment

/**
 * Petdoc
 * Class: HospitalVideoActivity
 * Created by kimjoonsung on 2020/09/03.
 *
 * Description :
 */
class HospitalVideoActivity : AppCompatActivity() {

    private var videoUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_video)

        videoUrl = intent?.getStringExtra("videoUrl") ?: videoUrl

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalVideoFragment().apply {
                bundleOf(
                    "videoUrl" to videoUrl
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