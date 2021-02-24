package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.domain.AllergyComment
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import java.lang.Exception

class AllergeCommentViewModel(
    private val apiService: PetdocApiService,
    private val categoryName: String
) : BaseViewModel() {
    val title = MutableLiveData<String>().apply {
        value = "$categoryName 항목"
    }
    val comment = MutableLiveData<AllergyComment>()

    fun start() {
        viewModelScope.launch {
            try {
                uiState.postValue(UiState.Loading)
                val response = apiService.getAllergyComment(categoryName.replace("/", "-"))
                if (response.code == "SUCCESS") {
                    comment.postValue(response.resultData.allergyComment)
                }
                uiState.postValue(UiState.Success)
            } catch (e: Exception) {
                uiState.postValue(UiState.Failure(e))
            }
        }
    }
}