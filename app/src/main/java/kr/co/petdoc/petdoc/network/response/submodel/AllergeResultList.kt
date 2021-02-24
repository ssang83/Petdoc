package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName

/**
 * Petdoc
 * Class: AllergeResult
 * Created by kimjoonsung on 2020/09/14.
 *
 * Description :
 */
class AllergeResultList : ArrayList<AllergeResultSubList>()

class AllergeResultSubList : ArrayList<AllergeResultSubListItem>()

data class AllergeResultSubListItem(
    var itemNameEng: String,
    var itemNameKor: String,
    var largeCategory: String,
    var percent: Int,
    var resultRate: String,
    var smallCategory: String,
    var specimenId: Int,
    var minRate: String,
    var maxRate: String,
    var level:String
)