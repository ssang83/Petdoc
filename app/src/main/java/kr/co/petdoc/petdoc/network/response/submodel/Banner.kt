package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: Banner
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 배너 이미지 아이템
 */
data class BannerItem(
    val bannerImageUrl: String,
    val comments: List<Comment>,
    val content: String,
    val createdAt: String,
    val id: Int,
    val possibleComment: Boolean,
    val postType: String,
    val priority: Int,
    val title: String,
    val type: String,
    val url: String,
    val version: String
)

data class Comment(
    val content: String,
    val createdAt: String,
    val id: Int,
    val pk: Int,
    val user: User,
    val userId: Int,
    val userImg: String,
    val userNickName: String
)

data class User(
    val nickName: String,
    val id: Int,
    val profileImage: String
)