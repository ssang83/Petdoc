package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

/**
 * Petdoc
 * Class: ChatRoomAddImageRequest
 * Created by kimjoonsung on 2020/06/01.
 *
 * Description :
 */
class ChatRoomAddImageRequest(apiService: ApiService, tag: String, _roomId:Int, file: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private var imageFile: String = file
    private var roomId = _roomId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val mediaType = MediaType.parse("multipart/form-data")
        val file = File(imageFile)

        val requestFile = RequestBody.create(mediaType, file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        call = apiService.postChatImage(roomId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}