package kr.co.petdoc.petdoc.network.response.submodel


/**
 * Petdoc
 * Class: LegacyChatItem
 * Created by kimjoonsung on 2020/05/22.
 *
 * Description :
 */
data class LegacyChatItem(
    val categoryId: Int,
    val categoryParentName: String,
    val counselRequestText: String,
    val createdAt: String,
    val id: Int,
    val kind: String,
    val recommendCount: Int
)