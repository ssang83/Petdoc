package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: UserProfileData
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
data class MyUserProfileData(
    @SerializedName("activity_grade")
    var activityGrade: Int,
    var birthday: String,
    @SerializedName("center_id")
    var centerId: Int?,
    @SerializedName("chat_push_agree")
    var chatPushAgree: Int?,
    @SerializedName("comment_push_agree")
    var commentPushAgree: Int?,
    var email: String,
    @SerializedName("email_confirm")
    var emailConfirm: Int?,
    var gender: String,
    @SerializedName("grade_push_agree")
    var gradePushAgree: Int?,
    @SerializedName("id_godo")
    var idGodo: String,
    @SerializedName("id_point")
    var idPoint: String,
    @SerializedName("location_use_agree")
    var locationUseAgree: Int?,
    @SerializedName("marketing_email")
    var marketingEmail: Int,
    @SerializedName("marketing_sms")
    var marketingSms: Int,
    @SerializedName("mobile_confirm")
    var mobileConfirm: Int,
    var name: String,
    var nickname: String,
    @SerializedName("old_userid")
    var oldUserid: Int,
    var password: String,
    var phone: String,
    @SerializedName("privacy_date")
    var privacyDate: String?,
    @SerializedName("privacy_save_period")
    var privacySavePeriod: String,
    @SerializedName("profile_path")
    var profilePath: String?,
    @SerializedName("push_agree")
    var pushAgree: Int,
    @SerializedName("reg_date")
    var regDate: String,
    @SerializedName("reg_route")
    var regRoute: Int,
    @SerializedName("social_key")
    var socialKey: String?,
    @SerializedName("social_type")
    var socialType: Int,
    var status: Int,
    @SerializedName("update_date")
    var updateDate: String,
    @SerializedName("user_agent")
    var userAgent: String?,
    @SerializedName("user_grade")
    var userGrade: Int?,
    @SerializedName("user_grade_end_date")
    var userGradeEndDate: String?,
    @SerializedName("user_id")
    var userId: Int
)