package kr.co.petdoc.petdoc.network.response.submodel

import com.google.gson.annotations.SerializedName

/**
 * Petdoc
 * Class: CategoryItem
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description : 홈 상담 카테고리 데이터
 */
data class CategoryItem (
    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("titleColor")
    val titleColor: String
)