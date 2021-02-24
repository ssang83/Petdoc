package kr.co.petdoc.petdoc.model.healthcareresult

import kr.co.petdoc.petdoc.domain.BloodResult

data class BloodResultDetailItem(
    val specimenId: String,
    val largeCategory: String,
    val smallCategory: String,
    val itemNameEng: String,
    val itemNameKor: String,
    val unit: String,
    val resultRate: String,
    val minRate: String,
    val maxRate: String,
    val referMinRate: String,
    val referMaxRate: String,
    val level: String,
    val percent: Int,
    var position: Int = 0
) : PetCheckResultSection {
    companion object {
        fun fromDomain(result: BloodResult): BloodResultDetailItem {
            return BloodResultDetailItem(
                specimenId = result.specimenId,
                largeCategory = result.largeCategory,
                smallCategory = result.smallCategory,
                itemNameEng = result.itemNameEng,
                itemNameKor = result.itemNameKor,
                unit = result.unit,
                resultRate = result.resultRate,
                minRate = result.minRate,
                maxRate = result.maxRate,
                referMinRate = result.referMinRate,
                referMaxRate = result.referMaxRate,
                level = result.level,
                percent = result.percent
            )
        }
    }
}