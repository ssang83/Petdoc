package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PetTalkDetailItem
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
data class PetTalkDetailItem(
    var board: Board,
    var code: Int,
    var commentCount: Int,
    var content: String,
    @SerializedName("created_at")
    var createdAt: String,
    var id: Int,
    var images: List<ImageX>,
    @SerializedName("main_score")
    var mainScore: Int,
    var nickName: String,
    var petTalk: PetTalk,
    var petTalkImageList: List<PetTalkImage>,
    var pk: Int,
    var profileImage: String,
    var title: String,
    var type: String,
    var userId: Int,
    @SerializedName("view_count")
    var viewCount: Int,
    var webUrl: String
)

data class Board(
    var comments: List<Any>,
    var content: String,
    @SerializedName("created_at")
    var createdAt: String,
    var images: List<Image>,
    @SerializedName("main_score")
    var mainScore: Int,
    var pk: Int,
    var title: String,
    var type: String,
    var user: PetTalkUser,
    @SerializedName("view_count")
    var viewCount: Int,
    @SerializedName("web_url")
    var webUrl: String
)

data class ImageX(
    var id: Int,
    @SerializedName("image_rel_path")
    var imageRelPath: String,
    @SerializedName("image_url")
    var imageUrl: String,
    @SerializedName("pet_talk_id")
    var petTalkId: Int
)

data class PetTalk(
    var commentCount: Int,
    var content: String,
    var id: Int,
    var mainScore: Int,
    var nickName: String,
    var profileImage: String,
    var title: String,
    var type: String,
    var userId: Int,
    var viewCount: Int,
    var webUrl: String
)

data class PetTalkImage(
    var id: Int,
    var image: String,
    var petTalkId: Int
)

data class Image(
    var id: Int,
    @SerializedName("image_rel_path")
    var imageRelPath: String,
    @SerializedName("image_url")
    var imageUrl: String,
    @SerializedName("pet_talk_id")
    var petTalkId: Int
)

data class PetTalkUser(
    @SerializedName("image_rel_path")
    var imageRelPath: String,
    @SerializedName("nick_name")
    var nickName: String,
    var pk: Int,
    @SerializedName("profile_image")
    var profileImage: String
)