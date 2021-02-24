package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: OrderListResponse
 * Created by kimjoonsung on 2/3/21.
 *
 * Description :
 */
data class OrderListResponse(
    var code: String,
    var messageKey: String,
    var resultData: OrderItem
)

data class OrderItem(
    var orderList: List<Order>
)

data class Order(
    var authResultCode: String,
    var authResultMsg: String,
    var cancelDate: String?,
    var cardName: String,
    var cardNo: String,
    var cardQuota: String,
    var itemName: String,
    var itemUrl: String,
    var merchantName: String,
    var orderDate: String,
    var orderId: Int,
    var orderStatus: String,
    var payAmount: Int,
    var payCount: Int,
    var payMethodType: String,
    var resultCode: String,
    var resultMsg: String,
    var showYn: String,
    var viewOrderId: String,
    var authKeyStatus: Int
)