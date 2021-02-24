package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ChatKeywordItem
 * Created by kimjoonsung on 2020/05/29.
 *
 * Description :
 */
data class ChatKeywordItem(
    val count: Int,
    val keyword: String,
    val pk: Int
)