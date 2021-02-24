package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ProfileItem
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description : 프로필 정보
 */
data class ProfileItem(
    @SerializedName("activity_grade")
    val activityGrade: Int,
    val birthday: String,
    val email: String,
    val gender: String,
    @SerializedName("godo_id")
    val godoId: String,
    @SerializedName("id_godo")
    val idGodo: String,
    @SerializedName("id_point")
    val idPoint: String,
    @SerializedName("marketing_email")
    val marketingEmail: Int,
    @SerializedName("marketing_sms")
    val marketingSms: Int,
    @SerializedName("mobile_confirm")
    val mobileConfirm: Int,
    val name: String,
    val nickname: String,
    @SerializedName("old_userid")
    val oldUserid: Int,
    val password: String,
    val phone: String,
    @SerializedName("privacy_date")
    val privacyDate: String,
    @SerializedName("privacy_save_period")
    val privacySavePeriod: String,
    @SerializedName("profile_path")
    val profilePath: String,
    @SerializedName("push_agree")
    val pushAgree: Int,
    @SerializedName("reg_route")
    val regRoute: Int,
    @SerializedName("social_key")
    val socialKey: String,
    @SerializedName("social_type")
    val socialType: Int,
    val status: Int,
    @SerializedName("user_id")
    val userId: Int
)