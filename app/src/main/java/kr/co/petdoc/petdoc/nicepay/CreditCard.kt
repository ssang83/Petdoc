package kr.co.petdoc.petdoc.nicepay

data class CreditCard(
    val name: String,


    val code: String
) {
    val interestFreeMonths: ArrayList<InterestFreeMonth> = arrayListOf()
    val installments: List<Installment> by lazy {
        makeInstallments()
    }
    private var amountToPay: Int = 0

    fun setPaymentAmount(amountToPay: Int) {
        this.amountToPay = amountToPay
    }

    private fun makeInstallments(): List<Installment> {
        val installments = arrayListOf<Installment>()
        (1 .. 12).forEach { month ->
            installments.add(
                Installment(
                    month = month,
                    isInterestFree = interestFreeMonths.any {
                        // 할부가능 개월, 최소 할부 가능 금액
                        (it.instmntMon.toInt() == month) &&
                                (it.minAmt <= amountToPay)
                    }
                )
            )
        }
        return installments
    }


}