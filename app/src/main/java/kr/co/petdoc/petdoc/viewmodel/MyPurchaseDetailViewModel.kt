package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.domain.OrderDetail
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.nicepay.OrderStatus
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

/**
 * Petdoc
 * Class: MyPurchaseDetailViewModel
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
class MyPurchaseDetailViewModel(
    private val apiService: PetdocApiService,
    private val persistentPrefs: PersistentPrefs,
    private val orderId:String
) : BaseViewModel() {

    val orderDetail = MutableLiveData<OrderDetail>()
    val moveToPetCheck = SingleLiveEvent<Unit>()
    val moveToCancel = SingleLiveEvent<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    fun start() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val userId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "")
                val response = withContext(Dispatchers.IO) { apiService.getOrderDetail(userId, orderId) }
                if (response.code == "SUCCESS") {
                    orderDetail.postValue(response.resultData.order)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setOrderDate(item:OrderDetail?): String {
        item?.let {
            val regDate = item.orderDate.split(" ")
            val orderDate = regDate[0].replace("-", ".")
            when (item.orderStatus) {
                OrderStatus.NET_CANCELED.name, OrderStatus.CANCELED.name -> {
                    val tempDate = item.cancelDate?.split(" ")
                    val date = tempDate?.get(0)?.replace("-", ".")
                    return "${orderDate}결제 / ${date}취소"
                }

                else -> return orderDate
            }
        }

        return ""
    }

    fun setPaymentInfo(item: OrderDetail?): String {
        item?.let {
            val cardQuota = if (item.cardQuota == "00") {
                "일시불"
            } else {
                String.format("%d", item.cardQuota.toInt())
            }
            return "${item.cardName}(${item.cardNo}) $cardQuota"
        }

        return ""
    }

    fun onCancel() {
        moveToCancel.value = orderId
    }

    fun onGoPurchase() {
        moveToPetCheck.call()
    }

    fun onGoToTest() {
        moveToPetCheck.call()
    }
}