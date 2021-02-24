package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.MyAlarmFragment

/**
 * Petdoc
 * Class: PushNotiActivity
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
class PushNotiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_noti)

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, MyAlarmFragment())
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}