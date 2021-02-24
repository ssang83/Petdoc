package kr.co.petdoc.petdoc.nicepay

data class Installment(
    val month: Int,
    val isInterestFree: Boolean
) {
    val name: String
        get() {
            return when(month) {
                1 -> "일시불"
                else -> "${month}개월${if (isInterestFree) " (무이자)" else ""}"
            }
        }
}
