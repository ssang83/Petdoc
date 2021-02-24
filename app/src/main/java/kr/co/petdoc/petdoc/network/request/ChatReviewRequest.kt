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
 * Class: ChatRoomAddReviewRequest
 * Created by kimjoonsung on 2020/06/01.
 *
 * Description :
 */
class ChatReviewRequest(apiService: ApiService, tag: String, id: Int, rate:String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val roomId = id
    private val ratingValue = rate

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val obj = JSONObject().apply{
            put("reviews", ratingValue)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), obj.toString())

        call = apiService.putChatQuitReview(roomId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}