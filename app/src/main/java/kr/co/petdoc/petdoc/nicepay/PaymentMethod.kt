package kr.co.petdoc.petdoc.nicepay

enum class PaymentMethod(val paymentName: String) {
    NONE(""),
    CREDIT_CARD("CARD"),
    KAKAO_PAY("KAKAO_PAY"),
    NAVER_PAY("NAVER_PAY"),
    SAMSUNG_PAY("SAMSUNG_PAY"),
    PAYCO("PAYCO"),
}