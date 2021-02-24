package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: UserGradeItem
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description :
 */
data class UserGradeItem(
    @SerializedName("code")
    val code: Int,
    @SerializedName("first_connected")
    val firstConnected: Boolean,
    @SerializedName("grade_change")
    val gradeChange: String
)