package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: BloodResult
 * Created by kimjoonsung on 2020/09/14.
 *
 * Description :
 */
class BloodResultList : ArrayList<BloodResultSubList>()

class BloodResultSubList : ArrayList<BloodResultSubListItem>()

data class BloodResultSubListItem(
    var itemNameEng: String,
    var itemNameKor: String,
    var largeCategory: String,
    var minRate: String,
    var percent: Int,
    var maxRate: String,
    var resultRate: String,
    var smallCategory: String,
    var specimenId: Int,
    var referMinRate: String,
    var referMaxRate: String,
    var unit: String,
    var level:String
)