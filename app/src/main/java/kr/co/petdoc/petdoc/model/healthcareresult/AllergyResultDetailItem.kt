package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.AllergyResult

data class AllergyResultDetailItem(
    val specimenId: String,
    val largeCategory: String,
    val smallCategory: String,
    val smallCategoryNameEng: String,
    val itemNameEng: String,
    val itemNameKor: String,
    val resultRate: String,
    val minRate: String,
    val maxRate: String,
    val level: String,
    val percent: Int,
    var position: Int = 0
) : PetCheckResultSection {
    companion object {
        fun fromDomain(result: AllergyResult): AllergyResultDetailItem {
            return AllergyResultDetailItem(
                specimenId = result.specimenId,
                largeCategory = result.largeCategory,
                smallCategory = result.smallCategory,
                smallCategoryNameEng = result.smallCategoryNameEng,
                itemNameEng = result.itemNameEng,
                itemNameKor = result.itemNameKor,
                resultRate = result.resultRate,
                minRate = result.minRate,
                maxRate = result.maxRate,
                level = result.level,
                percent = result.percent
            )
        }
    }
}