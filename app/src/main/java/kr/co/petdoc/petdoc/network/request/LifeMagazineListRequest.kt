package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.LifeMagazineSearchListResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: LifeMagazineListRequest
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
class LifeMagazineListRequest(
    apiService: ApiService,
    tag: String,
    _categoryId: String,
    _order: String,
    _limit: Int,
    _offset: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<MagazineListRespone>

    private lateinit var call: Call<MagazineListRespone>

    private var categoryId = _categoryId
    private var order = _order
    private var limit = _limit
    private val offset = _offset

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getLifeMagazine(categoryId, order, limit, offset)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class LifeMagazineSearchListRequest(
    apiService: ApiService,
    tag: String,
    _keyword:String,
    _categoryId: String,
    _order: String,
    _limit: Int,
    _offset: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<LifeMagazineSearchListResponse>

    private lateinit var call: Call<LifeMagazineSearchListResponse>

    private var categoryId = _categoryId
    private var order = _order
    private var limit = _limit
    private val offset = _offset
    private val keyword = _keyword

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getLifeMagazineSearch(keyword, categoryId, order, limit, offset)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}