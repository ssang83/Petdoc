package kr.co.petdoc.petdoc.nicepay

/**
 * Petdoc
 * Class: OrderCancel
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
enum class OrderCancel(val reason: String) {
    NONE(""),
    CHANGE_MIND("단순 변심"),
    SERVICE_NOT_STATISFIED("서비스 불만족"),
    PRICE_HIGH("가격 부담"),
    ETC("기타"),
}