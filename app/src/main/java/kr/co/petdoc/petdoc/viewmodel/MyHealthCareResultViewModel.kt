package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.domain.AllergyResult
import kr.co.petdoc.petdoc.domain.BloodResult
import kr.co.petdoc.petdoc.domain.DnsInspectionResult
import kr.co.petdoc.petdoc.fragment.mypage.health_care.MyHeathCareResultConfig
import kr.co.petdoc.petdoc.model.healthcareresult.*
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

class MyHealthCareResultViewModel(
    private val config: MyHeathCareResultConfig,
    private val apiService: PetdocApiService
) : BaseViewModel() {
    val allergyResultSections = MutableLiveData<List<PetCheckResultSection>>()
    val bloodResultSections = MutableLiveData<List<PetCheckResultSection>>()

    val quickSearchBloodCategory = MutableLiveData<String>()
    val quickSearchBloodListPosition = map(quickSearchBloodCategory) { category ->
        var moveToPosition = 0
        if (category.isNotEmpty()) {
            moveToPosition = bloodResultSections.value?.indexOfFirst {
                (it is BloodResultDetailHeader) && (it.smallCategory == category)
            } ?: 0
        }
        moveToPosition
    }

    fun start() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getHealthCareResultList(config.bookingId)
                }
                if (response.code == "SUCCESS") {
                    bloodResultSections.postValue(
                        mapToBloodSections(response.resultData.dnsInspectionResult)
                    )
                    allergyResultSections.postValue(
                        mapToAllergySections(response.resultData.dnsInspectionResult)
                    )
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun mapToBloodSections(result: DnsInspectionResult): List<PetCheckResultSection> {
        return arrayListOf<PetCheckResultSection>().apply {
            add(BloodResultHeader(result))
            add(BloodWarning(result.bloodDangerList))
            add(BloodResultAbout())
            addAll(mapToBloodDetails(result.bloodResultList))
            add(BloodResultFooter())
        }
    }

    private fun mapToBloodDetails(bloodResultList: List<List<BloodResult>>): List<PetCheckResultSection> {
        val details = arrayListOf<PetCheckResultSection>()
        bloodResultList.forEach { resultList ->
            resultList.forEachIndexed { index, bloodResult ->
                if (index == 0) {
                    details.add(BloodResultDetailHeader(bloodResult.smallCategory))
                }
                details.add(
                    BloodResultDetailItem.fromDomain(bloodResult).apply { position = index }
                )
            }
        }
        return details
    }

    private fun mapToAllergySections(result: DnsInspectionResult): List<PetCheckResultSection> {
        return arrayListOf<PetCheckResultSection>().apply {
            add(AllergyResultHeader(result))
            add(AllergyClass.fromDomain(result))
            add(AllergyResultAbout())
            addAll(mapToAllergyDetails(result.allergyResultList))
        }
    }

    private fun mapToAllergyDetails(allergyResultList: List<List<AllergyResult>>): List<PetCheckResultSection> {
        val details = arrayListOf<PetCheckResultSection>()
        allergyResultList.forEach { resultList ->
            resultList.forEachIndexed { index, allergyResult ->
                if (index == 0) {
                    details.add(AllergyResultDetailHeader(allergyResult.smallCategory))
                }
                details.add(
                    AllergyResultDetailItem.fromDomain(allergyResult).apply { position = index }
                )
            }
        }
        return details
    }
}