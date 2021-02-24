package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MagazineItem
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description : 매거진 데이터
 */
data class MagazineItem(
    val categoryId: Int,
    val categoryNm: String,
    val createdAt: String,
    val id: Int,
    val listScore: Int,
    val title: String,
    val titleImage: String,
    val type: String,
    val viewCount: Int,
    val likeCount:Int = 0
)


data class PetTalkItem(
    val pk: Int,
    val type: String,
    val content: String,
    val id: Int,
    val createdAt: String,
    val title: String,
    val viewCount: Int,
    val userId: String,
    val mainScore: Int,
    val commentCount: Int,
    val titleImage: String,
    val nickName: String,
    val profileImage: String
)

data class MagazineDetailItem(
    var categoryId: Int,
    var categoryNm: String,
    var createdAt: String,
    var encyContents: List<EncyContent>,
    var id: Int,
    var listScore: Int,
    var similarTitle: String,
    var title: String,
    var titleImage: String,
    var type: String,
    var viewCount: Int,
    var likeCount: Int,
    var webUrl: String
)

data class EncyContent(
    var content: String,
    var contentType: String,
    var createdAt: String,
    var encyId: Int,
    var id: Int,
    var image: String,
    var title: String,
    var updatedAt: String
)

data class MagazineSearchData(
    var totalCount:Int = 0,
    var data:List<MagazineItem>
)