package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: CareHistoryData
 * Created by kimjoonsung on 2020/06/23.
 *
 * Description :
 */
data class ResultData(
    var careHistoryList: List<CareHistoryData>
)

data class CareHistoryData(
    var bath: String?,
    var clinic: String?,
    var clinicContent: String?,
    var communion: String?,
    var date: String,
    var feces: String?,
    var hvalue: Double,
    var memo: String?,
    var petId: Int,
    var teeth: String?,
    var temperature: Double,
    var tvalue: Double,
    var urine: String?,
    var walk: String?,
    var weight: String?
)

data class ScannerResultData(
    var result: Boolean
)