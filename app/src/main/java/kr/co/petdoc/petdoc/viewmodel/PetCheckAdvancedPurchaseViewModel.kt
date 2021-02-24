package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.AppConstants.PREF_KEY_LAST_PAYMENT_METHOD
import kr.co.petdoc.petdoc.common.AppConstants.PREF_KEY_USE_SAME_PAYMENT_METHOD
import kr.co.petdoc.petdoc.domain.ORDER_STATE_NET_COMPLETED
import kr.co.petdoc.petdoc.domain.PetcheckAdvancedItem
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.nicepay.*
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

class PetCheckAdvancedPurchaseViewModel(
    private val apiService: PetdocApiService,
    private val nicePayApiService: NicePayApiService,
    private val persistentPrefs: PersistentPrefs
) : BaseViewModel() {
    // 이전 결제 수단 동일하게 사용여부
    val isUseSamePaymentMethodChecked = MutableLiveData<Boolean>().apply {
        value = persistentPrefs.getValue(PREF_KEY_USE_SAME_PAYMENT_METHOD, false)
    }
    // 현재 선택된 결제 수단 (신용카드, 간편결제)
    val paymentMethod = MutableLiveData<PaymentMethod>().apply {
        value = if (isUseSamePaymentMethodChecked.value == true) {
            val savedPaymentMethod = persistentPrefs.getValue(PREF_KEY_LAST_PAYMENT_METHOD, PaymentMethod.NONE.name)
            PaymentMethod.valueOf(savedPaymentMethod)
        } else {
            PaymentMethod.NONE
        }
    }
    // 현재 선택된 신용카드
    val creditCard = MutableLiveData<CreditCard>()
    // 현재 선택된 할부 정보
    val installment = MutableLiveData<Installment>()

    // 나이스페이에서 받아온 신용 카드 정보
    private val creditCardMap = mutableMapOf<String, CreditCard>()
    // 펫닥 상품 정보
    val itemInfo = MutableLiveData<PetcheckAdvancedItem>()
    // 구매 번호
    val orderNumber = MutableLiveData<String>()
    val viewOrderNumber = MutableLiveData<String>()

    // 약관 선택 상태
    val isAgreeAllTermsChecked = MutableLiveData<Boolean>()
    val isPrivacyPolicyChecked = MutableLiveData<Boolean>()
    val isTermsOfServiceChecked = MutableLiveData<Boolean>()

    // 화면 Event
    val startNicePayScreen = SingleLiveEvent<NicePayConfig>()
    val showCreditCardChooser = SingleLiveEvent<List<CreditCard>>()
    val showInstallmentsChooser = SingleLiveEvent<List<Installment>>()
    val startPurchaseCompleteScreen = SingleLiveEvent<Unit>()
    val closeNicePayScreen = SingleLiveEvent<Unit>()
    val showToast = SingleLiveEvent<String>()
    val privacyTermsScreen = SingleLiveEvent<Unit>()
    val serviceTermsScreen = SingleLiveEvent<Unit>()

    // NicePay 결제 정보창 Listener
    val webViewListener = object: NicePayWebView.NicePayWebViewListener {
        override fun initComplete() {}

        override fun onPurchaseComplete(orderNumber: String) {
            this@PetCheckAdvancedPurchaseViewModel.orderNumber.postValue(orderNumber)
            checkOrderNumberAndHandleScreen(orderNumber)
        }

        override fun closeByForce() {}
    }

    fun start() {
        if (itemInfo.value == null || creditCardMap.isEmpty()) {
            load()
        }
    }

    private fun load() {
        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }
        viewModelScope.launch(Dispatchers.IO + handler) {
            try {
                uiState.postValue(UiState.Loading)
                // 상품 정보
                val petcheckItemResponse = withContext(Dispatchers.IO) { apiService.getItemInfoForPetCheckAdvanced() }
                if (petcheckItemResponse.code == "SUCCESS") {
                    itemInfo.postValue(petcheckItemResponse.resultData.items)
                }
                // 신용카드 정보
//                val nicepayResponse = nicePayApiService.fetchCreditCardInfo(makeNicePayCreditCardInfoRequestBody())
                val nicepayResponse = apiService.getCreaditCardInfo(petcheckItemResponse.resultData.items.merchantId)
                nicepayResponse.resultData.cardInfo.body.data.map { data ->
                    creditCardMap[data.fnCd]?.interestFreeMonths?.add(
                        InterestFreeMonth(data.instmntMon, data.minAmt)
                    ) ?: run {
                        val credit = CreditCard(data.fnNm, data.fnCd)
                        creditCardMap[data.fnCd] = credit.apply {
                            interestFreeMonths.add(
                                InterestFreeMonth(data.instmntMon, data.minAmt)
                            )
                            setPaymentAmount(itemInfo.value?.itemAmount?:0)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            uiState.postValue(UiState.Success)
        }
    }

    // 구매번호 조회 후 구매가 정상적으로 이루어진 경우 구매완료 페이지 이동
    private fun checkOrderNumberAndHandleScreen(orderNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "")
                val response = apiService.getOrderDetail(userId, orderNumber)
                if (response.code == "SUCCESS") {
                    if (response.resultData.order.orderStatus == ORDER_STATE_NET_COMPLETED) {
                        this@PetCheckAdvancedPurchaseViewModel.viewOrderNumber.postValue(response.resultData.order.viewOrderId)
                        startPurchaseCompleteScreen.postCall()
                    } else {
                        closeNicePayScreen.postCall()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 신용카드 선택 버튼 클릭
    fun onCreditCardChooserBtnClicked() {
        showCreditCardChooser.value = creditCardMap.map { it.value }
    }

    // 할부 개월 버튼 클릭
    fun onInstallmentsBtnClicked() {
        creditCard.value?.let {
            showInstallmentsChooser.value = it.installments
        }
    }

    // 결제하기 버튼 클릭
    fun onPaymentBtnClicked() {
        if (checkValidation()) {
            val itemInfo = itemInfo.value!!
            startNicePayScreen.value = NicePayConfig(
                itemId = itemInfo.itemId,
                merchantId = itemInfo.merchantId,
                payMethodType = paymentMethod.value?.paymentName?:"",
                itemAmount = itemInfo.itemAmount,
                itemCount = 1,
                petdocUserId = persistentPrefs.getValue(AppConstants.PREF_KEY_USER_ID, "").toInt(),
                cardCode = if (paymentMethod.value == PaymentMethod.CREDIT_CARD) {
                    creditCard.value?.code?:""
                } else "",
                cardQuota = if (paymentMethod.value == PaymentMethod.CREDIT_CARD) {
                    installment.value?.month?.toString()?.padStart(2, '0')?:""
                } else ""
            )
            saveLastPaymentMethodIfNecessary()
        }
    }

    private fun checkValidation(): Boolean {
        return checkPaymentMethod() && checkTermsAgreed()
    }

    private fun checkPaymentMethod(): Boolean {
        return when(paymentMethod.value) {
            PaymentMethod.NONE -> {
                showToast.value = "결제 수단을 선택해주세요"
                false
            }
            PaymentMethod.CREDIT_CARD -> {
                if (creditCard.value == null) {
                    showToast.value = "카드를 선택해주세요"
                    false
                } else {
                    true
                }
            }
            else -> true
        }
    }

    private fun checkTermsAgreed(): Boolean {
        val isTermsAgreed = isAgreeAllTermsChecked.value ?: false
        if (!isTermsAgreed) {
            showToast.value = "약관에 동의해주세요"
        }
        return isTermsAgreed
    }

    // 결제수단 버튼 클릭
    fun onPaymentMethodClicked(curPaymentMethod: PaymentMethod) {
        paymentMethod.value = curPaymentMethod
    }

    // 선택한 결제 수단을 다음에도 사용
    fun onUseSamePaymentCheckBoxClicked() {
        val checkState = (isUseSamePaymentMethodChecked.value?:false).not()
        isUseSamePaymentMethodChecked.value = checkState
    }

    // 결제 진행 필수 동의 클릭
    fun onAgreeAllClicked() {
        val checkState = (isAgreeAllTermsChecked.value?:false).not()
        isAgreeAllTermsChecked.value = checkState
        isPrivacyPolicyChecked.value = checkState
        isTermsOfServiceChecked.value = checkState
    }

    // 개인정보 약관 클릭
    fun onTermsOfPrivacyPolicyClicked() {
        val checkState = (isPrivacyPolicyChecked.value?:false).not()
        isPrivacyPolicyChecked.value = checkState
        updateAgreeAllCheckState()
    }

    // 서비스 이용 약관 클릭
    fun onTermsOfServiceClicked() {
        val checkState = (isTermsOfServiceChecked.value?:false).not()
        isTermsOfServiceChecked.value = checkState
        updateAgreeAllCheckState()
    }

    private fun updateAgreeAllCheckState() {
        isAgreeAllTermsChecked.value = (isTermsOfServiceChecked.value == true)
            && (isPrivacyPolicyChecked.value == true)
    }

    // 신용카드 선택
    fun onCreditCardSelected(creditCard: CreditCard) {
        val curCreditCard = this.creditCard.value
        if (curCreditCard != creditCard) {
            this.creditCard.value = creditCard
            // 신용카드 변경 시 할부기간 일시불로 초기화
            this.installment.value =
                Installment(1, false)
        }
    }

    // 할부 기간 선택
    fun onInstallmentSelected(installment: Installment) {
        this.installment.value = installment
    }

    // 개인정보 수집,이용 및 위탁동의 클릭
    fun onPrivacyTermsClicked() {
        privacyTermsScreen.call()
    }

    // 대행 서비스 약관동의 클릭
    fun onServiceTermsClicked() {
        serviceTermsScreen.call()
    }

    // 동일한 결제 수단 이용 체크 처리
    private fun saveLastPaymentMethodIfNecessary() {
        val isUseSamePaymentMethodChecked = isUseSamePaymentMethodChecked.value?:false
        val isPaymentMethodSelected = paymentMethod.value != PaymentMethod.NONE
        if (isUseSamePaymentMethodChecked && isPaymentMethodSelected) {
            persistentPrefs.setValue(PREF_KEY_LAST_PAYMENT_METHOD, paymentMethod.value?.name ?: PaymentMethod.NONE.name)
            persistentPrefs.setValue(PREF_KEY_USE_SAME_PAYMENT_METHOD, isUseSamePaymentMethodChecked)
        }

        if (isUseSamePaymentMethodChecked.not()) {
            persistentPrefs.setValue(PREF_KEY_USE_SAME_PAYMENT_METHOD, false)
            persistentPrefs.setValue(PREF_KEY_LAST_PAYMENT_METHOD, PaymentMethod.NONE.name)
        }
    }

}