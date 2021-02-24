package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ShortcutItem
 * Created by kimjoonsung on 2020/09/16.
 *
 * Description :
 */
data class ShortcutItem(
    var highlightYn: String,
    var id: String,
    var title: String,
    var bannerId:String
)