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
 * Class: MagazineListRequest
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description :
 */
class MagazineListRequest(apiService: ApiService, tag:String, order:String, limit:Int, page:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<MagazineListRespone>

    private lateinit var call: Call<MagazineListRespone>

    private var mOrder = order
    private var mLimit = limit
    private var mPage = page

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineList(mOrder, mLimit, mPage)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineLikeCountRequest(apiService: ApiService, tag:String, var id:HashSet<Int>) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineLikeCount(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}