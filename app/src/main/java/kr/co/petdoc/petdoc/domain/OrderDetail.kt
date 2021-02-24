package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class OrderDetailResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String,
    @SerializedName("resultData") val resultData: OrderDetailData
)

data class OrderDetailData(
    @SerializedName("order") val order: OrderDetail
)

data class OrderDetail(
    @SerializedName("itemName") val itemName: String,
    @SerializedName("authResultCode") val authResultCode: String,
    @SerializedName("authResultMsg") val authResultMsg: String,
    @SerializedName("payMethodType") val payMethodType: String,
    @SerializedName("payAmount") val payAmount: Int,
    @SerializedName("payCount") val payCount: Int,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("petdocUserId") val petdocUserId: Int,
    @SerializedName("cardQuota") val cardQuota: String,
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("cardNo") val cardNo: String,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("itemUrl") val itemUrl: String,
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("cancelDate") val cancelDate: String?,
    @SerializedName("viewOrderId") val viewOrderId: String,
    @SerializedName("authKeyUseYn") val authKeyUseYn: String
)

const val ORDER_STATE_ORDERED = "ORDERED"            // 주문 시작
const val ORDER_STATE_AUTH = "AUTH"                  // 인증
const val ORDER_STATE_FAILED = "FAILED"              // 주문 실패
const val ORDER_STATE_CANCELED = "CANCELED"          // 주문 취소
const val ORDER_STATE_NET_CANCELED = "NET_CANCELED"  // 망상 취소
const val ORDER_STATE_NET_COMPLETED = "COMPLETED"    // 주문 완료