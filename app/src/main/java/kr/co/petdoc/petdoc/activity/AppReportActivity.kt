package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.CustomerReportFragment

/**
 * Petdoc
 * Class: AppReportActivity
 * Created by kimjoonsung on 2020/08/20.
 *
 * Description :
 */
class AppReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_report)

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, CustomerReportFragment())
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}