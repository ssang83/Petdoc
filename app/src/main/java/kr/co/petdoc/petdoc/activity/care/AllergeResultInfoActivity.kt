package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_allerge_test_result_info.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: AllergeResultInfoFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class AllergeResultInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_allerge_test_result_info)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        btnClose.setOnClickListener { finish() }
    }
}