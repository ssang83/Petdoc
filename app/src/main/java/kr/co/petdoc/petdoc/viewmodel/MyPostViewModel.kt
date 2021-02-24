package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.MyPetTalkData
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyPostViewModel
 * Created by kimjoonsung on 1/11/21.
 *
 * Description :
 */
class MyPostViewModel(
    private val apiService: PetdocApiService
) : BaseViewModel() {
    val myPetTalkItems = MutableLiveData<List<MyPetTalkData>>()
    val petTalkDetailScreen = SingleLiveEvent<Unit>()

    fun start() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getMyPetTalkList() }
                if (response.code == "200") {
                    myPetTalkItems.postValue( response.petTalkList)
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("MM월dd일", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }
}