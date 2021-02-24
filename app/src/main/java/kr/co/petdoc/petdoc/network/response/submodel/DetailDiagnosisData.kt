package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: DetailDiagnosisData
 * Created by kimjoonsung on 2020/07/23.
 *
 * Description :
 */

data class DetailDiagnosisData(
    var totPage: Int,
    val questions:List<DiagnosisData>
)

data class DiagnosisData(
    var checkedYn: String,
    var displaySeq: Int,
    var displayYn: String,
    var foodScoreJson: String,
    var id: Int,
    var nutrId: Int,
    var page: Int,
    var question: String,
    var totPage: Int
)