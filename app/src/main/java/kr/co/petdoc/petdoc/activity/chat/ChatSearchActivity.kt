package kr.co.petdoc.petdoc.activity.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chat_search.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: ChatSearchActivity
 * Created by kimjoonsung on 2020/05/27.
 *
 * Description :
 */
class ChatSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_search)
    }

    override fun onBackPressed() {
        if(chatSearchFragment.childFragmentManager?.backStackEntryCount == 0){
            finish()
        }else super.onBackPressed()
    }
}