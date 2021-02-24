package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.domain.BloodComment
import kr.co.petdoc.petdoc.repository.network.PetdocApiService

/**
 * Petdoc
 * Class: BloodCommentViewModel
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */
class BloodCommentViewModel(
    private val apiService: PetdocApiService,
    private val categoryName: String
) : BaseViewModel() {
    val title = MutableLiveData<String>().apply {
        value = "$categoryName 항목"
    }

    val comment = MutableLiveData<List<BloodComment>>()

    fun start() {
        viewModelScope.launch {
            try {
                uiState.postValue(UiState.Loading)
                val response = apiService.getBloodComment(categoryName)
                if (response.code == "SUCCESS") {
                    comment.postValue(response.resultData.bloodComment)
                }
                uiState.postValue(UiState.Success)
            } catch (e: Exception) {
                uiState.postValue(UiState.Failure(e))
            }
        }
    }
}