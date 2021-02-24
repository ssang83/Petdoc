package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.domain.MyAlarm
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyAlarmViewModel
 * Created by kimjoonsung on 1/25/21.
 *
 * Description :
 */
class MyAlarmViewModel(
    private val apiService: PetdocApiService
) : BaseViewModel() {

    val alarmItems = MutableLiveData<List<MyAlarm>>()
    val moveToNotiDetailScreen = SingleLiveEvent<MyAlarm>()
    val moveToChatScreen = SingleLiveEvent<ChatConfig>()

    private var chatStatus = ""
    private var chatPartner = ""

    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    fun start() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getMyAlarmList(0, 10000) }
                if (response.code == "SUCCESS") {
                    alarmItems.postValue(response.resultData)
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    fun moveToChat(chatId:Int) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                val response = withContext(Dispatchers.IO) { apiService.getChatListWithCoroutine() }
                if (response.chatList.size > 0) {
                    for (item in response.chatList) {
                        if (item.pk == chatId) {
                            chatStatus = item.status
                            chatPartner = item.partner?.name.toString()
                            moveToChatScreen.postValue(
                                ChatConfig(
                                    chatId = chatId,
                                    chatStatus = chatStatus,
                                    chatPartner = chatPartner
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    fun setNotiText(type: String) =
        when (type) {
            "chat" -> "상담"
            "care" -> "펫 체크 어드밴스드"
            "hospital" -> "병원 예약"
            "petTalk" -> "펫톡"
            "payment" -> "결제 완료"
            "payment_cancel" -> "결제 취소"
            "banner" -> "배너"
            "point" -> "포인트"
            else -> "프로모션"
        }

    fun setRegDate(date: String):String {
        val regDate = date.split(" ")
        return calculateDate(regDate[0])
    }

    fun onItemClick(item: MyAlarm) {
        Logger.d("event id : ${item.eventId}")
        moveToNotiDetailScreen.value = item
    }

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("MM월dd일", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }
}

data class ChatConfig(
    val chatId: Int,              // 상담 채팅 아이디
    val chatStatus: String,       // 상담 채팅 status
    val chatPartner: String       // 상담 채팅 수의사 이름
)