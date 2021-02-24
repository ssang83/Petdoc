package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PetTalkReplyItem
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
data class PetTalkReplyItem(
    var content: String,
    var createdAt: String,
    var id: Int,
    var petTalkId: Int,
    var updatedAt: String,
    var userId: Int,
    var userImg: String?,
    var userNickName: String
)