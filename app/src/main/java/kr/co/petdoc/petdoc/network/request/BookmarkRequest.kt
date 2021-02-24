package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.HospitalBookmarkListResponse
import kr.co.petdoc.petdoc.network.response.MagazineBookmarkListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: BookmarkRequest
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
class HospitalBookmarkListRequest(apiService: ApiService, tag: String, _latitude:Double, _longitude:Double) :
    AbstractApiRequest(apiService, tag) {

    private val latitude = _latitude
    private val longitude = _longitude

    private lateinit var callback: ApiCallback<HospitalBookmarkListResponse>
    private lateinit var call: Call<HospitalBookmarkListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalBookmarkList(latitude, longitude)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MagazineBookmarkListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<MagazineBookmarkListResponse>
    private lateinit var call: Call<MagazineBookmarkListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineBookmarkList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}