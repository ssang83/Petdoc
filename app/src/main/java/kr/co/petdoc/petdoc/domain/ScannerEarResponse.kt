package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ScannerEarResponse
 * Created by kimjoonsung on 1/20/21.
 *
 * Description :
 */
data class CareEarImageResponse(
    var code: String,
    var messageKey: Any,
    var resultData: CareEarImage
)

data class CareEarImage(
    var imageList: List<Image>
)

data class Image(
    var created: String,
    var id: Int,
    var path: String,
    var petId: Int,
    var userId: Int,
    var type:String,
    var location:String,
    var regDt:String
)