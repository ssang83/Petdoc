package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: PetTalkReplyListPagingRequest
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
class PetTalkReplyPagingListRequest(
    apiService: ApiService,
    tag: String,
    _petTalkId: Int,
    _baseId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petTalkId = _petTalkId
    private val baseId = _baseId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getPetTalkReplyPagingList(petTalkId, baseId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}