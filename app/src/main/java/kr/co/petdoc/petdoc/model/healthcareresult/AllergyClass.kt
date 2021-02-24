package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.DnsInspectionResult

data class AllergyClass(
    val danger2: List<String>,
    val danger3: List<String>,
    val danger4: List<String>,
    val danger5: List<String>,
    val danger6: List<String>
) : PetCheckResultSection {
    companion object {
        fun fromDomain(domain: DnsInspectionResult): AllergyClass {
            return AllergyClass(
                danger2 = domain.danger2,
                danger3 = domain.danger3,
                danger4 = domain.danger4,
                danger5 = domain.danger5,
                danger6 = domain.danger6
            )
        }
    }
}