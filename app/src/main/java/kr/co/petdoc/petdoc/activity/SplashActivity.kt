package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import co.ab180.airbridge.Airbridge
import co.ab180.airbridge.AirbridgeCallback
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.repository.resetMyPetsDirty
import kr.co.petdoc.petdoc.viewmodel.DeepLinkViewModel


/**
 * petdoc-android
 * Class: SplashActivity
 * Fixed by sungminkim on 2020/04/02.
 *
 * Description :  Running on Navigator ( Splash Image, Permission Guide, User Guide )
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var dataModel:DeepLinkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(this).get(DeepLinkViewModel::class.java)
        setContentView(R.layout.activity_splash_intro)             // start On Navigator
    }

    override fun onResume() {
        super.onResume()

        Airbridge.getDeeplink(intent, object : AirbridgeCallback<Uri> {
            override fun onComplete() {
                // After process deeplink data
            }

            override fun onFailure(throwable: Throwable) {
                // Error
            }

            override fun onSuccess(result: Uri) {
                // petdocair://petdoc.deeplink.page?type=banner&id=200
                if (result.scheme == "petdocair" && result.host == "petdoc.deeplink.page") {
                    val type = result.getQueryParameter("type")
                    val id = result.getQueryParameter("id")
                    val bookingId = result.getQueryParameter("bookingId")
                    Logger.d("type : $type, id : $id, bookingId : $bookingId")
                    dataModel.deepLinkType.value = type
                    dataModel.deepLinkId.value = id
                    dataModel.deepLinkBookingId.value = bookingId
                }
            }
        })

        resetMyPetsDirty()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}