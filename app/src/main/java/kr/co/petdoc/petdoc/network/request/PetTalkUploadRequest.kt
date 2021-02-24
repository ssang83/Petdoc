package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.HomePetTalkListResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.net.URLEncoder

/**
 * Petdoc
 * Class: PetTalkUploadRequest
 * Created by kimjoonsung on 2020/06/04.
 *
 * Description :
 */
class PetTalkUploadRequest(
    apiService: ApiService,
    tag: String,
    _title: String,
    _content: String,
    _type: String,
    _images: List<String>
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val title = _title
    private val content = _content
    private val type = _type
    private val images = _images

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .addFormDataPart("content", content)
            .addFormDataPart("type", type)
            .addFormDataPart("image_count", images.size.toString())

        if (images.size > 0) {
            for (i in 0 until images.size) {
                val file = File(images[i])
                val encodedFileName = URLEncoder.encode(file.name, "UTF-8")
                builder.addFormDataPart("image${images.size - i}", encodedFileName, RequestBody.create(MediaType.parse("multipart/form-data"), file))
            }
        }

        call = apiService.postPetTalkUpload(builder.build())
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class PetTalkDeleteRequest(
    apiService: ApiService,
    tag: String,
    _id: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deletePetTalk(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class PetTalkModifyRequest(
        apiService: ApiService,
        tag: String,
        _id: Int,
        _title: String,
        _content: String,
        _type: String,
        _images: List<String>
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id
    private val title = _title
    private val content = _content
    private val type = _type
    private val images = _images

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("type", type)
                .addFormDataPart("image_count", images.size.toString())

        if (images.size > 0) {
            for (i in 0 until images.size) {
                val file = File(images[i])
                val encodedFileName = URLEncoder.encode(file.name, "UTF-8")
                builder.addFormDataPart("image${images.size - i}", encodedFileName, RequestBody.create(MediaType.parse("multipart/form-data"), file))
            }
        }

        call = apiService.putPetTalkUpload(id, builder.build())
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HomePetTalkListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HomePetTalkListResponse>
    private lateinit var call: Call<HomePetTalkListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHomePetTalkList()
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}