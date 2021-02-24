package kr.co.petdoc.petdoc.nicepay

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NicePayConfig(
    @SerializedName("itemId") val itemId: Int,        // 물품아이디
    @SerializedName("merchantId") val merchantId: Int,    // 상점 아이디
    @SerializedName("payMethodType") val payMethodType: String,  // 결제 수단 {CARD, NAVER_PAY, SAMSUNG_PAY, KAKAO_PAY, PAYCO}
    @SerializedName("itemAmount") val itemAmount: Int,    // 물품 가격
    @SerializedName("itemCount") val itemCount: Int,     // 결제 갯수
    @SerializedName("petdocUserId") val petdocUserId: Int,  // 유저 아이디
    @SerializedName("cardCode") val cardCode: String,   // 카드 결제시 카드사 코드
    @SerializedName("cardQuota") val cardQuota: String   // 카드 결제시 할부기간 ( 무이자 : 00, 2개월 : 02 .. )
) : Parcelable