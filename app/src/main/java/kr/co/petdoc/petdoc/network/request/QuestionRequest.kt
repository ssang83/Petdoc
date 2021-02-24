package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.QuestionsListResponse
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: QuestionRequest
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
class QuestionListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<QuestionsListResponse>
    private lateinit var call: Call<QuestionsListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getQuestionList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class QuestionUploadRequest(apiService: ApiService, tag: String, _title:String, _content:String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val title = _title
    private val content = _content

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("title", title)
            put("content", content)
            put("app_verion", "")
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postQuestions(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}