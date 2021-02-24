package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HealthCareItem
 * Created by kimjoonsung on 2020/09/14.
 *
 * Description :
 */
data class HealthCareResultItem(
    var authCode: String,
    var bookingId: Int,
    var clinicId: Int,
    var petName: String,
    var petdocPetId: String,
    var petdocUserId: String,
    var requestDate: String,
    var resultSendDate: String,
    var status: Int,
    var statusType: String,
    var userName: String,
    var centerName: String,
    var readYn: String
)