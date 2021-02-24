package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: AuthCodeStatusItem
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
data class AuthCodeStatusItem(
    var authKey: String,
    var buyUserId: String,
    var createAt: String,
    var goodsAmt: Int,
    var goodsNo: Int,
    var goodsOptions: String,
    var goodsPosition: Int,
    var id: Int,
    var orderNo: String,
    var petId: Int,
    var status: Int,
    var updateAt: String,
    var useUserId: Any
)