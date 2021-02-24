package kr.co.petdoc.petdoc.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.pettalk.PetTalkDetailFragment

/**
 * Petdoc
 * Class: PetTalkDetailActivity
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
class PetTalkDetailActivity : AppCompatActivity() {

    private var petTalkId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_talk_detail)

        petTalkId = intent?.getIntExtra("petTalkId", petTalkId)!!

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, PetTalkDetailFragment().apply {
                val bundle = Bundle().apply {
                    putInt("petTalkId", petTalkId)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }
}