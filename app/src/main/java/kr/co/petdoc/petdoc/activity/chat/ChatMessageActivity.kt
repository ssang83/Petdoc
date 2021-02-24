package kr.co.petdoc.petdoc.activity.chat

import android.os.Bundle
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.fragment.chat.ChatMessageFragment
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: ChatMessageActivity
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class ChatMessageActivity : BaseActivity() {

    private var chatPk = -1
    private var status = ""
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_message)

        chatPk = intent?.getIntExtra("chatPk", chatPk)!!
        status = intent?.getStringExtra("status")!!
        name = intent?.getStringExtra("name")!!
        Logger.d("chatPk : $chatPk, status : $status, name : $name")

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, ChatMessageFragment().apply {
                val bundle = Bundle().apply {
                    putInt("chatPk", chatPk)
                    putString("status", status)
                    putString("name", name)
                }
                arguments = bundle
            })
        }

        transaction.commit()
    }
}