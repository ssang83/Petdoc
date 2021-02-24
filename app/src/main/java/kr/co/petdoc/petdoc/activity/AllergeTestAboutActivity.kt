package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_allerge_test_about.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.databinding.ActivityAllergeTestAboutBinding

class AllergeTestAboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAllergeTestAboutBinding>(this, R.layout.activity_allerge_test_about)

        btnClose.setOnClickListener {
            finish()
        }
    }
}