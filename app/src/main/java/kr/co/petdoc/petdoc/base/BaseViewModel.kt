package kr.co.petdoc.petdoc.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    open val uiState = MutableLiveData<UiState>(UiState.Success)

    fun launchWithRx(job: () -> Disposable) {
        compositeDisposable.add(job())
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}