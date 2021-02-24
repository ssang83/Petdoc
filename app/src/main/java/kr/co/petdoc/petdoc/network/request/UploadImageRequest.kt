package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

/**
 * Petdoc
 * Class: UploadImageRequest
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description: Test Code
 */
class UploadImageRequest(apiService: ApiService, tag: String, file: String) :
    AbstractApiRequest(apiService, tag) {

    private var imageFile: String = file

    /**
     * + The callback used for this request. Declared globally for cancellation. See [ ][.cancel].
     */
    internal lateinit var callback: ApiCallback<AbstractApiResponse>

    /**
     * To cancel REST API call from Retrofit. See [.cancel].
     */
    private lateinit var call: Call<AbstractApiResponse>

    override fun execute() {
        callback = ApiCallback(tag)
        if(!isInternetActive()) {
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val mediaType = MediaType.parse("multipart/form-data")
        val file = File(imageFile)

        val requestFile = RequestBody.create(mediaType, file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        call = apiService.uploadFile(context.getResources().getString(R.string.api_upload), "", "", body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}