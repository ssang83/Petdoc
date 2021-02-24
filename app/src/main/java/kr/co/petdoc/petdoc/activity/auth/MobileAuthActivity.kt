package kr.co.petdoc.petdoc.activity.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.activity_mobile_authorization.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: MobileAuthActivity
 * Created by kimjoonsung on 2020/07/29.
 *
 * Description :
 */
class MobileAuthActivity : AppCompatActivity() {

    private val REQUEST_AUTH = 0x1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_mobile_authorization)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        btnAuth.setOnClickListener {
            Airbridge.trackEvent("identify", "click", "identify_start", null, null, null)
            FirebaseAPI(this).logEventFirebase("가입_본인인증", "Click Event", "본인인증 버튼 클릭")
            startActivityForResult(Intent(this, AuthorizationActivity::class.java), REQUEST_AUTH)
        }

        btnClose.setOnClickListener { onBackPressed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_AUTH && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}