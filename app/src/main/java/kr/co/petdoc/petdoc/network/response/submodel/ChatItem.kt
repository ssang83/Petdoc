package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ChatItem
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
data class ChatItem(
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("last_message")
    val lastMessage: String,
    val partner: Partner?,
    val pk: Int,
    val status: String,
    @SerializedName("updated_date")
    val updatedDate: String,
    @SerializedName("user_unread_count")
    val userUnreadCount: Int
)

data class Partner(
    @SerializedName("display_name")
    val displayName: String,
    val name: String,
    val pk: Int,
    @SerializedName("profile_image")
    val profileImage: String
)