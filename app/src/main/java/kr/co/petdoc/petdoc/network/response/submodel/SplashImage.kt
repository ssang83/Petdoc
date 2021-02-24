package kr.co.petdoc.petdoc.network.response.submodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SplashImages(
    @SerializedName("items")
    @Expose
    val splashImages : MutableList<SplashItem> = mutableListOf()
)

data class SplashItem(
    @SerializedName("image_url")
    val image_url : String,
    val hexcode : String
)
