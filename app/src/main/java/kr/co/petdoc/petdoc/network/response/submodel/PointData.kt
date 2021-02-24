package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PointData
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
data class PointData(
    @SerializedName("ASSI_PO_AMT")
    var aSSIPOAMT: String,
    @SerializedName("CUST_ID")
    var cUSTID: String,
    @SerializedName("ENAB_PO_AMT")
    var eNABPOAMT: String
)