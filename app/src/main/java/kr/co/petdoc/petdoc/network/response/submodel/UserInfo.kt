package kr.co.petdoc.petdoc.network.response.submodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserInfo (

    @SerializedName("resultData")
    @Expose
    val resultData : UserResultData
)

data class UserResultData (

    @SerializedName("userInfo")
    @Expose
    val userInfo : UserInfoData,

    @SerializedName("oldToken")
    @Expose
    val oldToken : String,

    @SerializedName("accessToken")
    @Expose
    val accessToken : String,

    @SerializedName("refreshToken")
    @Expose
    val refreshToken : String
)

data class UserInfoData (
    val user_id: Int,
    val email: String,
    val password: String,
    val social_key: String,
    val id_godo: String,
    val id_point: String,
    val phone: String?,
    val nickname: String?,
    val birthday: String?,
    val gender: String?,
    val name: String?,
    val profile_path: String?,
    val social_type: String,
    val reg_route: Int,
    val marketing_email: Int,
    val marketing_sms: Int,
    val push_agree: Int,
    val chat_push_agree: Int?,
    val comment_push_agree: Int?,
    val grade_push_agree: Int?,
    val location_use_agree: Boolean?,
    val privacy_save_period: Int?,
    val privacy_date: String?,
    val old_userid: Int,
    val reg_date: String,
    val update_date: String?,
    val activity_grade: Int,
    val mobile_confirm: Int,
    val status: Int,
    val center_id:Int?,
    val email_confirm:Int?,
    val user_agent:Int?,
    val user_grade:Int?,
    val user_grade_end_date:Int?,
    val auth_id:Int?


/*"center_id": null,
"email_confirm": null,
"user_agent": null,
"user_grade": null,
"user_grade_end_date": null*/
)