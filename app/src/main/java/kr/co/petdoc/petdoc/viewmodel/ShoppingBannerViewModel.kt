package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.network.response.submodel.BannerItem

/**
 * Petdoc
 * Class: ShoppingBannerViewModel
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
class ShoppingBannerViewModel : ViewModel() {
    private val _bannerItemList: MutableLiveData<MutableList<BannerItem>> = MutableLiveData()
    private val _currentPosition: MutableLiveData<Int> = MutableLiveData()

    val bannerItemList: LiveData<MutableList<BannerItem>>
        get() = _bannerItemList

    val currentPosition : LiveData<Int>
        get() = _currentPosition

    init {
        _currentPosition.value = 0
    }

    fun setBannerItem(items: MutableList<BannerItem>) {
        _bannerItemList.value = items
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    fun getCurrentPosition() = currentPosition.value
}
