package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.MagazineDetailResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: MagazineDetailRequet
 * Created by kimjoonsung on 2020/06/04.
 *
 * Description :
 */
class MagazineDetailRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<MagazineDetailResponse>

    private lateinit var call: Call<MagazineDetailResponse>

    private val magazineId = id

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineDetail(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailSubListRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<MagazineListRespone>

    private lateinit var call: Call<MagazineListRespone>

    private val magazineId = id

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineDetailSubList(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailViewCountRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postMagazineViewCount(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailLikeCountRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMagazineLikeCount(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailLikeStatusRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
            val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            call = apiService.getMagazineLikeStatus(magazineId, userId)
            call.enqueue(callback)
        }
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailBookmarkRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postMagazineBookmark(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailBookmarkCancelRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteMagazineBookmark(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailLikeRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postMagazineLike(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class MagazineDetailLikeCancelRequet(apiService: ApiService, tag:String, id:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    private val magazineId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteMagazineLike(magazineId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}