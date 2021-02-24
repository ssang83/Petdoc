package kr.co.petdoc.petdoc.network.response.submodel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Petdoc
 * Class: AccountData
 * Created by kimjoonsung on 2020/07/29.
 *
 * Description :
 */
data class AccountData (
    var userId:Int,
    var userEmail:String,
    var socialType:String,
    var nickName:String,
    var phone:String
)