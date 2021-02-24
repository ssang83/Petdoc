package kr.co.petdoc.petdoc.repository.network.form

import com.google.gson.annotations.SerializedName

data class HospitalListForm(
    @SerializedName("ownerLatitude") val ownerLatitude: String,
    @SerializedName("ownerLongitude") val ownerLongitude: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("autoComplete") val autoComplete: Boolean = true,
    @SerializedName("keyword") val keyword: String = "",
    @SerializedName("workingYn") val working: String = "",
    @SerializedName("registerYn") val register: String = "",
    @SerializedName("bookingYn") val booking: String = "",
    @SerializedName("beautyYn") val beauty: String = "",
    @SerializedName("hotelYn") val hotel: String = "",
    @SerializedName("allDayYn") val allDay: String = "",
    @SerializedName("parkingYn") val parking: String = "",
    @SerializedName("healthCheckYn") val healthCheck: String = "",
    @SerializedName("animalIdList") val petIdList: List<Int>? = null,
    @SerializedName("size") val size: Int,
    @SerializedName("page") val page: Int
)