package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.RelatedHospitalListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: HospitalListRequest
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
class RelatedHospitalListRequest(apiService: ApiService, tag: String, _page: Int, _count: Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<RelatedHospitalListResponse>

    private lateinit var call: Call<RelatedHospitalListResponse>

    private val page = _page
    private val count = _count

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalList(page, count)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}