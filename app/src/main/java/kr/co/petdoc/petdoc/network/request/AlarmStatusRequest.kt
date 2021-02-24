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
 * Class: AlarmStatusRequest
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
class PushStatusRequest(apiService: ApiService, tag: String, _chatNoti:String, _commentNoti:String, _gradeNoti:String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val chatNoti = _chatNoti
    private val commentNoti = _commentNoti
    private val gradeNoti = _gradeNoti

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("chat_noti", chatNoti)
            put("comment_noti", commentNoti)
            put("grade_noti", gradeNoti)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.putNotiStatus(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MarketingStatusRequest(apiService: ApiService, tag: String, _status:String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val status = _status

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("marketing", status)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.putMarketingStatus(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}