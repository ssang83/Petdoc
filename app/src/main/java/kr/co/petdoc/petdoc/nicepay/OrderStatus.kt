package kr.co.petdoc.petdoc.nicepay

/**
 * Petdoc
 * Class: OrderStatus
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
enum class OrderStatus(val status: String) {
    NONE(""),
    ORDERED("결제 시작"),
    AUTH("인증"),
    FAILED("결제 실패"),
    CANCELED("결제 취소"),
    NET_CANCELED("결제 망상 취소"),
    COMPLETED("결제 완료"),
}