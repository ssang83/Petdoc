package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MyPetTalkData
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class MyPetTalkData(
    var commentCount: Int,
    var comments: List<Any>,
    @SerializedName("createdAt")
    var createdAt: String,
    var id: Int,
    var images: List<MyPetTalkImage>,
    var mainScore: Int,
    var nickName: String,
    var pk: Int,
    var title: String,
    var titleImage: String?,
    var type: String,
    var user: MyPetTalkUser
)

data class MyPetTalkImage(
    @SerializedName("image_url")
    var imageUrl: String
)

data class MyPetTalkUser(
    @SerializedName("nick_name")
    var nickName: String
)