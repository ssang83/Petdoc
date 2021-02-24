package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ChatDetailItem
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
data class ChatDetailItem(
    @SerializedName("can_search")
    val canSearch: Boolean,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("counsel_tag")
    val counselTag: List<Any>,
    @SerializedName("counsel_text")
    val counselText: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("end_date")
    val endDate: String,
    val messages: List<Message>,
    val partner: Partner?,
    val pet: Pet,
    val pk: Int,
    val reviews: Int,
    val status: String,
    @SerializedName("web_url")
    val webUrl: String,
    val is_recommend:Boolean
)

data class Message(
    val createdAt: String,
    @SerializedName("_id")
    val id: Int,
    val image: String,
    val text: String,
    val user: MessageUser?
)

data class Pet(
    @SerializedName("birth_day")
    val birthDay: String,
    val gender: String,
    val kind: String,
    val pk: Int,
    val species: String
)

data class MessageUser(
    val avatar: String,
    @SerializedName("_id")
    val id: Int,
    val name: String
)