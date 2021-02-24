package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ImageListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ImageListRequest
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description: Test Code
 */
class ImageListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    /**
     * The callback used for this request. Declared globally for cancellation. See [ ][.cancel].
     */
    private lateinit var callback: ApiCallback<ImageListResponse>

    /**
     * To cancel REST API call from Retrofit. See [.cancel].
     */
    private lateinit var call: Call<ImageListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()) {
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getImageList(context.getResources().getString(R.string.api_image_list))
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}