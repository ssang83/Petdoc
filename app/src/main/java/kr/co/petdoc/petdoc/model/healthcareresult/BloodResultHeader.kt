package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.DnsInspectionResult

data class BloodResultHeader(
    val result: DnsInspectionResult
) : PetCheckResultSection {
    val warningCnt: Int
        get() {
            return with(result) {
                var sumOfWarning = 0
                bloodDangerList.forEach {
                    sumOfWarning += it.size
                }
                sumOfWarning
            }
        }
}