package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HomePetTalkData
 * Created by kimjoonsung on 2020/07/21.
 *
 * Description :
 */
data class HomePetTalkData(
    var commentCount: Int,
    var content: Any,
    var id: Int,
    var imageId: Int,
    var imageURL: String,
    var nickName: String,
    var score: Int,
    var title: String
)