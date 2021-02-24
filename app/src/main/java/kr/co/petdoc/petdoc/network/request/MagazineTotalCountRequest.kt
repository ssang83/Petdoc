package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: MagazineTotalCountRequest
 * Created by kimjoonsung on 2020/05/25.
 *
 * Description :
 */
class MagazineTotalCountRequest(apiService: ApiService, tag: String, _categoryId: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private var categoryId = _categoryId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineTotalCount(categoryId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}