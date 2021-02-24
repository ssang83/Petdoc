package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.PetInfoListResponse
import kr.co.petdoc.petdoc.repository.resetMyPetsDirty
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: PetInfoListRequest
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description : 펫 정보 Request
 */
class PetInfoListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
        } else {
            0
        }

        call = apiService.getPetInfoList(oldUserId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetListChangeRequest(apiService: ApiService, tag: String, var petId:MutableList<Int>) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("numbers", petId.toString())

        call = apiService.postPetListChange(builder.build())
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}
