package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.network.response.submodel.ChatCategoryItem
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem

/**
 * Petdoc
 * Class: ChatDataModel
 * Created by kimjoonsung on 2020/05/20.
 *
 * Description :
 */
class ChatDataModel : ViewModel() {

    var petInfo = MutableLiveData<PetInfoItem>().apply{ this.value = null }
    var category = MutableLiveData<ChatCategoryItem>().apply{ this.value = null}
}