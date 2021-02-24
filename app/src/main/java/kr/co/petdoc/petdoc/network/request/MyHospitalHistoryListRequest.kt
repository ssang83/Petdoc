package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.BookingHistoryListResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.RegisterHistoryListResponse
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: MyHospitalHistoryListRequest
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
class MyHospitalHistoryRegisterListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<RegisterHistoryListResponse>
    private lateinit var call: Call<RegisterHistoryListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMyHospitalHistoryRegisterList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MyHospitalHistoryBookingListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<BookingHistoryListResponse>
    private lateinit var call: Call<BookingHistoryListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMyHospitalHistoryBookingList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MyHospitalHistoryDetailRequest(apiService: ApiService, tag: String, _type: String, _bookingId:Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HospitalHistoryResponse>
    private lateinit var call: Call<HospitalHistoryResponse>

    private val type = _type
    private val bookingId = _bookingId

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMyHospitalDetail(type, bookingId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}