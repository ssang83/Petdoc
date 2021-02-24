package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.BannerListResponse
import kr.co.petdoc.petdoc.network.response.PetTalkResponse
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: BannerListRequest
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description :
 */
class BannerListRequest(apiService: ApiService, tag: String, type: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<BannerListResponse>

    private lateinit var call: Call<BannerListResponse>

    private var bannerType = type

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getBannerList(bannerType)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}



class FamousPetTalkRequest(apiService:ApiService, tag:String) : AbstractApiRequest(apiService, tag){

    private lateinit var callback: ApiCallback<PetTalkResponse>

    private lateinit var call: Call<PetTalkResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getFamousPetTalk()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetTalkListRequest(apiService:ApiService, tag:String, var type:String, var limit:Int, var page:Int) : AbstractApiRequest(apiService, tag){

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = if(type.isNullOrBlank()) apiService.getPetTalkListAll( "createdAt",  limit, page) else apiService.getPetTalkList(type, "createdAt",  limit, page)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}