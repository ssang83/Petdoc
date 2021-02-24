package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.AuthCodeStatusResponse
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: HealthCareRequest
 * Created by kimjoonsung on 2020/09/11.
 *
 * Description :
 */
class HealthCareBookingRequest(
    apiService: ApiService,
    tag: String,
    var centerId:Int,
    var clinicRoomId:Int,
    var petId:Int,
    var authCode:String,
    var bookingTime:String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val clinicIdList = mutableListOf<Int>().apply { add(99) }
        val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").toInt()

        val content = JSONObject().apply{
            put("centerId", centerId)
            put("clinicRoomId", clinicRoomId)
            put("clinicIdList", JSONArray(clinicIdList))
            put("memo", "")
            put("petdocPetId", petId)
            put("petdocUserId", userId)
            put("authCode", authCode)
            put("bookingTime", bookingTime)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postHealtCare(centerId, clinicRoomId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareBookingCancelRequest(
    apiService: ApiService,
    tag: String,
    var centerId:Int,
    var bookingId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteHealthCare(centerId, bookingId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareResultListRequest(
    apiService: ApiService,
    tag: String,
    var bookingId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHealthCareResultList(bookingId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareResultForPetRequest(
    apiService: ApiService,
    tag: String,
    var petId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHealthCareResultForPet(petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareStatusRequest(
    apiService: ApiService,
    tag: String,
    var petId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHealthCareStatus(petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareCheckAuthCodeRequest(
    apiService: ApiService,
    tag: String,
    var authCode: String,
    var petId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.checkAuthKey(authCode, petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareAuthCodeStatusRequest(
    apiService: ApiService,
    tag: String,
    var petId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<AuthCodeStatusResponse>
    private lateinit var call: Call<AuthCodeStatusResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getAuthCodeStatus(petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareBookingIdRequest(
    apiService: ApiService,
    tag: String,
    var authCode: String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getBookingIdForAuthCode(authCode)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareVisitRequest(
    apiService: ApiService,
    tag: String,
    var bookingId: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getDnaVisitStatus(bookingId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HealthCareResultForPetIds(
    apiService: ApiService,
    tag: String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getPetCheckResultForIds()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}