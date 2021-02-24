package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName
import kr.co.petdoc.petdoc.network.response.submodel.ChatCategoryItem


/**
 * Petdoc
 * Class: ChatCategoryResponse
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
data class ChatCategoryResponse(
    var code: String,
    var messageKey: Any,
    var resultData: List<ChatCategoryItem>
)