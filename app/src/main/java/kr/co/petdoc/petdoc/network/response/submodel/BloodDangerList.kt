package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: BloodDangerList
 * Created by kimjoonsung on 2020/09/22.
 *
 * Description :
 */
class BloodDangerList : ArrayList<BloodDangerListSubList>()

class BloodDangerListSubList : ArrayList<BloodDangerListSubListItem>()

data class BloodDangerListSubListItem(
    var itemNameEng: String,
    var itemNameKor: String,
    var largeCategory: String,
    var level: String,
    var maxRate: String,
    var minRate: String,
    var percent: Int,
    var referMaxRate: String,
    var referMinRate: String,
    var resultRate: String,
    var smallCategory: String,
    var specimenId: Int,
    var unit: String
)