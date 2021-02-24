package kr.co.petdoc.petdoc.repository.network.form

import com.google.gson.annotations.SerializedName

/**
 * Petdoc
 * Class: InjectionForm
 * Created by kimjoonsung on 1/20/21.
 *
 * Description :
 */
data class InjectionForm(
    @SerializedName("regDate") val regDate: String,     // "20200511 10:32:54"
    @SerializedName("inject")  val inject: Boolean      // true or false
)