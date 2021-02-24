package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_blood_test_about.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: BloodTestAboutActivity
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */
class BloodTestAboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_test_about)

        btnClose.setOnClickListener { finish() }
    }
}