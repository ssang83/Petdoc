package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: NoticeData
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class NoticeData(
    var content: String,
    @SerializedName("created_at")
    var createdAt: String,
    var pk: Int,
    var title: String,
    @SerializedName("updated_at")
    var updatedAt: String
)