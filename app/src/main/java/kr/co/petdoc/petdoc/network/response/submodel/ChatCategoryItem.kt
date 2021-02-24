package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ChatCategoryItem
 * Created by kimjoonsung on 2020/05/20.
 *
 * Description :
 */
data class ChatCategoryItem(
    val childList: List<Child>,
    val level: Int,
    val name: String,
    val pk: Int
)

data class Child(
    val childList: List<ChildX>,
    val level: Int,
    val name: String,
    val pk: Int
)

data class ChildX(
    val level: Int,
    val name: String,
    val pk: Int
)