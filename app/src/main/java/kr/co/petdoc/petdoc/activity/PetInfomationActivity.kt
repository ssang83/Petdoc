package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.mypage.PetInformationFragment

/**
 * Petdoc
 * Class: PetInfomationActivity
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class PetInfomationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_information)

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, PetInformationFragment())
        }

        transaction.commit()
    }
}