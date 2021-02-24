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
 * Class: ReplyAddRequest
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
class ReplyAddRequest(apiService: ApiService, tag: String, _type: String, _petTalkId: Int, _comment:String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val type = _type
    private val petTalkId = _petTalkId
    private val comment = _comment

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val obj = JSONObject().apply{
            put("content", comment)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), obj.toString())

        call = apiService.postReplyAdd(type, petTalkId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}