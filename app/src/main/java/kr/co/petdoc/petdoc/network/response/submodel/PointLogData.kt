package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PointLogData
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
data class PointLogData(
    @SerializedName("PO_LIST")
    var poList: List<LogData>
)

data class LogData(
    @SerializedName("BALANCE_AMT")
    var bALANCEAMT: Int,
    @SerializedName("CUST_ID")
    var cUSTID: String,
    @SerializedName("NOTE")
    var nOTE: String,
    @SerializedName("PO_AMT")
    var pOAMT: Int,
    @SerializedName("PO_DTM")
    var pODTM: String,
    @SerializedName("PO_USE_YN")
    var pOUSEYN: String,
    @SerializedName("SA_MEDI_CD")
    var sAMEDICD: String,
    @SerializedName("SA_MEDI_DP_NM")
    var sAMEDIDPNM: String,
    @SerializedName("STORD_DP_NM")
    var sTORDDPNM: String,
    @SerializedName("STORD_ID")
    var sTORDID: String
)