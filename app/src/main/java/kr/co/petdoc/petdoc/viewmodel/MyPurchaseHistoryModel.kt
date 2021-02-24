package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.domain.Order
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.nicepay.OrderStatus
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

/**
 * Petdoc
 * Class: MyPurchaseHistoryModel
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
class MyPurchaseHistoryModel(
    private val apiService: PetdocApiService,
    private val persistentPrefs: PersistentPrefs
) : BaseViewModel() {

    val historyItems = MutableLiveData<List<Order>>()
    val moveToPurchaseDetail = SingleLiveEvent<Order>()
    val moveToPurchaseCancel = SingleLiveEvent<Order>()
    val showDeleteChooser = SingleLiveEvent<Order>()
    val moveToPetCheck = SingleLiveEvent<Unit>()
    val showToast = SingleLiveEvent<String>()

    private var tempItems = mutableListOf<Order>()

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    fun start(isReload:Boolean) {
        if (historyItems.value == null || isReload) {
            load()
        }
    }

    fun historyDelete(orderId:Int) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val userId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "").toInt()
                val response = withContext(Dispatchers.IO) { apiService.deleteOrderInfo(userId, orderId) }
                if (response.code == "SUCCESS") {
                    val item = tempItems.filter { it.orderId == orderId }
                    tempItems.remove(item[0])
                    historyItems.postValue(tempItems)
                    showToast.postValue("결제 내역이 삭제 되었습니다.")
                } else {
                    showToast.postValue("결제 내역이 삭제가 실패 되었습니다.")
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    fun onItemClick(item: Order) {
        Logger.d("order id : ${item.orderId}")
        moveToPurchaseDetail.value = item
    }

    fun onDelete(item: Order) {
        Logger.d("order id : ${item.orderId}")
        showDeleteChooser.value = item
    }

    fun onCancel(item: Order) {
        Logger.d("order id : ${item.orderId}")
        when (item.orderStatus) {
            OrderStatus.COMPLETED.name -> {
                moveToPurchaseCancel.value = item
            }

            OrderStatus.CANCELED.name -> {
                moveToPetCheck.call()
            }
        }
    }

    fun setStatusText(status: String) =
        when (status) {
            OrderStatus.AUTH.name -> OrderStatus.AUTH.status
            OrderStatus.CANCELED.name -> OrderStatus.CANCELED.status
            OrderStatus.FAILED.name, OrderStatus.NET_CANCELED.name -> OrderStatus.FAILED.status
            OrderStatus.COMPLETED.name -> OrderStatus.COMPLETED.status
            else -> OrderStatus.NONE.status
        }

    fun setOrderDate(date: String, cancelDate:String?, status: String): String {
        val regDate = date.split(" ")
        val orderDate = regDate[0].replace("-", ".")
        when (status) {
            OrderStatus.CANCELED.name, OrderStatus.NET_CANCELED.name -> {
                val tempDate = cancelDate?.split(" ")
                val date = tempDate?.get(0)?.replace("-", ".")
                return "${orderDate}결제 / ${date}취소"
            }

            else -> return orderDate
        }
    }

    fun setButtonText(status: String) =
        when (status) {
            OrderStatus.COMPLETED.name -> OrderStatus.CANCELED.status
            else -> "재구매"
        }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val userId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "").toInt()
                val response = withContext(Dispatchers.IO) { apiService.getOrderList(userId) }
                if (response.code == "SUCCESS") {
                    val items = response.resultData.orderList.filter { it.showYn == "Y" }
                    tempItems.clear()
                    tempItems.addAll(items)
                    historyItems.postValue(items)
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }
}