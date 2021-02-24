package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MagazineBookmarkData
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class MagazineBookmarkData(
    @SerializedName("bookmark_count")
    var bookmarkCount: Int,
    @SerializedName("category_name")
    var categoryName: String,
    var content: String,
    @SerializedName("created_at")
    var createdAt: String,
    var images: List<Any>,
    @SerializedName("is_bookmark")
    var isBookmark: Boolean,
    @SerializedName("is_like")
    var isLike: Boolean,
    @SerializedName("like_count")
    var likeCount: Int,
    @SerializedName("list_score")
    var listScore: Int,
    var pk: Int,
    var tags: List<Any>,
    var title: String,
    @SerializedName("title_image_url")
    var titleImageUrl: String,
    var type: String,
    @SerializedName("view_count")
    var viewCount: Int
)