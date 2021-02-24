package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatRoomsRequest
 * Created by kimjoonsung on 2020/05/27.
 *
 * Description :
 */
class ChatRoomsRequest(
    apiService: ApiService,
    tag: String,
    _petId: Int,
    _message: String,
    _isSearch: String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val message = _message
    private val isSearch = _isSearch

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val obj = JSONObject().apply{
            put("pet_id", petId)
            put("text", message)
            put("can_search", isSearch)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), obj.toString())

        call = apiService.postChattingRooms(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class ChatRoomsRecommendRequest(
    apiService: ApiService,
    tag: String,
    _roomId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val roomId = _roomId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postChatRoomRecommend(roomId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class ChatRoomsRecommendCancelRequest(
    apiService: ApiService,
    tag: String,
    _roomId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val roomId = _roomId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteChatRoomRecommend(roomId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}