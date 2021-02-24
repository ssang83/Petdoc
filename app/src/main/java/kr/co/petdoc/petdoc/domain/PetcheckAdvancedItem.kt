package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class PetcheckAdvancedItemResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String,
    @SerializedName("resultData") val resultData: PetcheckAdvancedItemData
)

data class PetcheckAdvancedItemData(
    @SerializedName("items") val items: PetcheckAdvancedItem
)

data class PetcheckAdvancedItem(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("merchantMid") val merchantMid: String,
    @SerializedName("merchantKey") val merchantKey: String,
    @SerializedName("itemId") val itemId: Int,
    @SerializedName("itemName") val itemName: String,
    @SerializedName("itemUrl") val itemUrl: String,
    @SerializedName("itemContents") val itemContents: String,
    @SerializedName("itemAmount") val itemAmount: Int,
    @SerializedName("categoryId") val categoryId: Int
)