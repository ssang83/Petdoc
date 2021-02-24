package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.DnsInspectionResult

data class AllergyResultHeader(
    val result: DnsInspectionResult
) : PetCheckResultSection {
    val warningCnt: Int
        get() {
            return with(result) {
                danger3.size + danger4.size + danger5.size + danger6.size
            }
        }
}