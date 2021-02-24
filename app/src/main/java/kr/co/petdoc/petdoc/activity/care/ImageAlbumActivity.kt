package kr.co.petdoc.petdoc.activity.care

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.ImageAlbumFragment
import org.greenrobot.eventbus.EventBus

/**
 * Petdoc
 * Class: ImageAlbumActivity
 * Created by kimjoonsung on 2020/06/26.
 *
 * Description :
 */
class ImageAlbumActivity : AppCompatActivity() {

    private var fromMy = false
    private var petId = -1
    private var petImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_album)

        fromMy = intent?.getBooleanExtra("fromMy", false)!!
        petId = intent?.getIntExtra("petId", petId)!!
        petImage = intent?.getStringExtra("petImage")!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ImageAlbumFragment().apply {
                val bundle = Bundle().apply {
                    putInt("petId", petId)
                    putBoolean("fromMy", fromMy)
                    putString("petImage", petImage)
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
        EventBus.getDefault().post("Ear")
    }
}