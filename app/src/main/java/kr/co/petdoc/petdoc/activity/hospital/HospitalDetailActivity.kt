package kr.co.petdoc.petdoc.activity.hospital

import android.app.Activity
import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.hospital.HospitalDetailFragment

/**
 * Petdoc
 * Class: HospitalDetailActivity
 * Created by kimjoonsung on 2020/06/09.
 *
 * Description :
 */
class HospitalDetailActivity : BaseActivity() {

    private var centerId = -1
    private var bookingTab = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_detail)

        mActivityList.add(this)

        centerId = intent?.getIntExtra("centerId", centerId)!!
        bookingTab = intent?.getStringExtra("bookingTab") ?: bookingTab

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, HospitalDetailFragment().apply {
                val bundle = Bundle().apply {
                    putInt("centerId", centerId)
                    putBoolean("fromHome", true)
                    putString("bookingTab", bookingTab)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivityList.remove(this)      // 정상종료인 경우 리스트에서 제거
    }
}