package kr.co.petdoc.petdoc.network

import android.text.TextUtils
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.event.RequestFinishedEvent
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * Petdoc
 * Class: ApiRequestCallback
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description : Api ReauestCallback
 */
class RequestCallback(tag:String) : Callback<ResponseBody?> {
    /**
     * Indicates if the callback was invalidated.
     */
    private var isInvalidated: Boolean = false

    /**
     * The tag of the request which uses this callback.
     */
    private val requestTag = tag

    override fun onResponse(
        call: Call<ResponseBody?>?,
        response: Response<ResponseBody?>?
    ) {
        if (call == null || response == null) {
            Logger.e("call or response null error!")
            return
        }
        var result: String?
        var urlDecode = ""
        try {
            urlDecode = URLDecoder.decode(call.request().url().toString(), "UTF-8")
            Logger.d("urlDecode:$urlDecode")
        } catch (e: Exception) {
            Logger.p(e)
        }

        if (response.isSuccessful) {
            try {
//                    result = response.body().string();
                result = getBody(response.body()!!)
            } catch (e: Exception) {
                Logger.p(e)
                result = null
            }
        } else {
            try {
//                    result = response.errorBody().string();
                result = getBody(response.errorBody()!!)
            } catch (e: Exception) {
                Logger.p(e)
                result = null
            }
        }

        if (response.isSuccessful) {
            EventBus.getDefault().post(NetworkBusResponse("ok", requestTag, result!!, response.headers()))
        } else {
            val code = response.code().toString()
            EventBus.getDefault().post(NetworkBusResponse("fail", requestTag, result!!, null, code))
        }
    }

    override fun onFailure(
        call: Call<ResponseBody?>,
        t: Throwable
    ) {
        if (!call.isCanceled && !isInvalidated) {
            EventBus.getDefault().post(ApiErrorEvent(requestTag, t))
        }

        finishRequest()
    }

    private fun getBody(responseBody: ResponseBody): String? {
        var body = ""

        //ResponseBody responseBody = response.body();
        val source = responseBody.source()
        try {
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()
            var charset = Charset.forName("UTF-8")
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(Charset.forName("UTF-8"))
            }
            if (responseBody.contentLength() != 0L) {
                body = buffer.clone().readString(charset)
            }
        } catch (e: java.lang.Exception) {
            Logger.p(e)
        }
        return body
    }

    /**
     * Invalidates this callback. This means the caller doesn't want to be called back anymore.
     */
    fun invalidate() {
        isInvalidated = true
    }

    /**
     * Posts a [RequestFinishedEvent] on the EventBus to tell the [ApiClient]
     * to remove the request from the list of running requests.
     */
    private fun finishRequest() {
        EventBus.getDefault().post(RequestFinishedEvent(requestTag))
    }


    /**
     * Call this method if No internet connection or other use.
     *
     * @param resultMsgUser User defined messages.
     */
    fun postUnexpectedError(resultMsgUser: String) {
        EventBus.getDefault().post(ApiErrorWithMessageEvent(requestTag, resultMsgUser))
        finishRequest()
    }
}