package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.food.MyPetFoodRecommendResult
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem

/**
 * Petdoc
 * Class: MatchFoodResultActivity
 * Created by kimjoonsung on 2020/07/10.
 *
 * Description :
 */
class MatchFoodResultActivity : AppCompatActivity() {

    private var item:PetInfoItem? = null
    private var type = ""
    private var isPurchase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_food_result)

        item = intent?.getParcelableExtra("item")
        type = intent?.getStringExtra("type") ?: type
        isPurchase = intent?.getBooleanExtra("isPurchase", false) ?: false

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, MyPetFoodRecommendResult().apply {
                bundleOf("item" to item, "type" to type, "isPurchase" to isPurchase).let {
                    arguments = it
                }
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }
}