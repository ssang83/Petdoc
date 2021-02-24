package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: LegacyMagazineListRequest
 * Created by kimjoonsung on 2020/05/25.
 *
 * Description :
 */
class LegacyMagazineListRequest(
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
    private var offset = _offset

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getLegacyMagazineList(categoryId, order, limit, offset)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}