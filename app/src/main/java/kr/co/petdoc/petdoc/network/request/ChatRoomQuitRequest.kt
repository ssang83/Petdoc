package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatRoomQuitRequest
 * Created by kimjoonsung on 2020/06/01.
 *
 * Description :
 */
class ChatRoomQuitRequest(apiService: ApiService, tag: String, id: Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val roomId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.putChatQuit(roomId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}