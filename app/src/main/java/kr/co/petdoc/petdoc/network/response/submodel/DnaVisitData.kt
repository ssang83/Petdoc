package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: DnaVisitData
 * Created by kimjoonsung on 2020/09/22.
 *
 * Description :
 */
data class DnaVisitData(
    var authCode: String,
    var bookingId: Int,
    var centerName: String,
    var clinicId: Int,
    var collectReqDate: String,
    var petName: String,
    var petdocPetId: String,
    var petdocUserId: String,
    var requestDate: String,
    var status: Int,
    var statusType: String,
    var userName: String
)