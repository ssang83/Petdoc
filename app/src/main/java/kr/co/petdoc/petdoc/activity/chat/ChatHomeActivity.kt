package kr.co.petdoc.petdoc.activity.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat_quit.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.PetChatFragment
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: ChatHomeActivity
 * Created by kimjoonsung on 2020/09/02.
 *
 * Description :
 */
class ChatHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_home)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, PetChatFragment().apply {
                val bundle = Bundle().apply {
                    putBoolean("isActivity", true)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}