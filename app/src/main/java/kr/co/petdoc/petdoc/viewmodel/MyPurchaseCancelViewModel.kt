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
import kr.co.petdoc.petdoc.network.makePurchaseCancelRequestBody
import kr.co.petdoc.petdoc.nicepay.OrderCancel
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

/**
 * Petdoc
 * Class: MyPurchaseCancelViewModel
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
class MyPurchaseCancelViewModel(
    private val apiService: PetdocApiService,
    private val persistentPrefs: PersistentPrefs,
    private val orderId: String
) : BaseViewModel() {

    val orderDetail = MutableLiveData<OrderDetail>()
    val showCancelToast = SingleLiveEvent<String>()
    val cancelReason = MutableLiveData<OrderCancel>().apply { value = OrderCancel.NONE }
    val etcReason = MutableLiveData<String>().apply { value = "" }

    private var curReason:OrderCancel = OrderCancel.NONE

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

    fun onCancelReason(curCancelReason:OrderCancel) {
        cancelReason.value = curCancelReason
        curReason = curCancelReason
    }

    fun onCancel(etcReason:String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val userId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "")
                val reason = if(curReason.name == OrderCancel.ETC.name) {
                    etcReason
                } else {
                    getReason(curReason)
                }

                val response = withContext(Dispatchers.IO) { apiService.cancelOrder(orderId, userId, makePurchaseCancelRequestBody(orderId, userId, reason)) }
                if (response.code == "SUCCESS") {
                    showCancelToast.postValue("결제 취소되었습니다.")
                } else {
                    showCancelToast.postValue("결제 취소가 실패되었습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onTextChanged(text: CharSequence?) {
        Logger.d("etc reason : $text")
        etcReason.value = text?.toString()?.trim()
    }

    private fun getReason(reason: OrderCancel) =
        when (reason.name) {
            OrderCancel.CHANGE_MIND.name -> OrderCancel.CHANGE_MIND.reason
            OrderCancel.SERVICE_NOT_STATISFIED.name -> OrderCancel.SERVICE_NOT_STATISFIED.reason
            OrderCancel.PRICE_HIGH.name -> OrderCancel.PRICE_HIGH.reason
            else -> OrderCancel.ETC.reason
        }
}