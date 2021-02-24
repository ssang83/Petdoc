package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.SplashImageResponse
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: SplashImageRequest
 * Created by sungminKim on 2020/04/02.
 *
 * Description: Splash Image Request
 */
class SplashImageRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag){

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.loadSplash()
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HomeShorCutListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag){

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHomeShortCut()
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}