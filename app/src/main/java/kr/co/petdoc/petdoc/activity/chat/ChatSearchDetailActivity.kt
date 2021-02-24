package kr.co.petdoc.petdoc.activity.chat

import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.chat.search.ChatSearchDetailFragment
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: ChatDetailActivity
 * Created by kimjoonsung on 2020/06/01.
 *
 * Description :
 */
class ChatSearchDetailActivity : BaseActivity() {

    private var chatId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        chatId = intent?.getIntExtra("chatId", chatId)!!
        Logger.d("chatPk : $chatId")

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ChatSearchDetailFragment().apply {
                val bundle = Bundle().apply {
                    putInt("chatId", chatId)
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