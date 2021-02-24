package kr.co.petdoc.petdoc.base

sealed class UiState {
    object Loading : UiState()
    object Retry : UiState()
    object Success : UiState()
    data class Failure(val e: Throwable? = null) : UiState()
}