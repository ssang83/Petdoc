package kr.co.petdoc.petdoc.activity.chat

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.viewmodel.ChatDataModel

/**
 * Petdoc
 * Class: ChatInfoActivity
 * Created by kimjoonsung on 2020/05/20.
 *
 * Description :
 */
class ChatInfoActivity : BaseActivity() {

    private lateinit var dataModel: ChatDataModel
    private var petInfoItem: PetInfoItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)
        dataModel = ViewModelProvider(this).get(ChatDataModel::class.java)

        petInfoItem = intent?.getParcelableExtra("petItem")

        dataModel.petInfo.value = petInfoItem
        dataModel.category.value = PetdocApplication.mChatCategoryItem
    }

    override fun onBackPressed() {
        findNavController(R.id.chatInfoHost).let{
            if(it.currentDestination?.id == R.id.chatExisitingInfoFragment){
                finish()
                return
            }
        }
        super.onBackPressed()
    }
}