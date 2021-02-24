package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_guilde.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: GuideActivity
 * Created by kimjoonsung on 2020/04/21.
 *
 * Description : 등급 및 혜택 안내
 */
class GuideActivity : AppCompatActivity() {

    lateinit var imageUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_guilde)

        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        val type = intent.getStringExtra("type")
        if (type == "level") {
            imageUrl = AppConstants.REWARD_IMG_URL
        } else {
            imageUrl = AppConstants.TIP_IMG_URL
        }

        Glide.with(this)
            .load(imageUrl)
            .into(guideImg)

        btnClose.setOnClickListener { onBackPressed() }
        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }
    }

    override fun onBackPressed() {
        finish()
    }
}