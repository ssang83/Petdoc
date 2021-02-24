package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.BloodResult

class BloodWarning(
    val warningList: List<List<BloodResult>>
) : PetCheckResultSection