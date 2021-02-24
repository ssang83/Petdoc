package kr.co.petdoc.petdoc.activity.care

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarItem
import kr.co.petdoc.petdoc.fragment.scanner.ImageDetailFragment

/**
 * Petdoc
 * Class: ImageDetailActivity
 * Created by kimjoonsung on 2020/06/29.
 *
 * Description :
 */
class ImageDetailActivity : AppCompatActivity() {

    private var imageUrl = ""
    private var item:EarItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        imageUrl = intent?.getStringExtra("petImage")!!
        item = intent?.getParcelableExtra("item")

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ImageDetailFragment().apply {
                val bundle = Bundle().apply {
                    putString("petImage", imageUrl)
                    putParcelable("item", item)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}