package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.*
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: HospitalRequest
 * Created by kimjoonsung on 2020/06/08.
 *
 * Description :
 */
class HospitalListRequest(
    apiService: ApiService,
    tag: String,
    _latitude: String,
    _longitude: String,
    _keyword:String,
    _working:String,
    _register:String,
    _booking:String,
    _beauty:String,
    _hotel:String,
    _allDay:String,
    _parking:String,
    _healthCheck:String,
    _petIdList:MutableList<Int>,
    _size: Int,
    _page: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HospitalListResponse>
    private lateinit var call: Call<HospitalListResponse>

    private val latitude = _latitude
    private val longitude = _longitude
    private val keyword = _keyword
    private val working = _working
    private val register = _register
    private val booking = _booking
    private val beauty = _beauty
    private val hotel = _hotel
    private val allDay = _allDay
    private val parking = _parking
    private val healthCheckYn = _healthCheck
    private val anmimalIdList = _petIdList
    private val size = _size
    private val page = _page

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("ownerLatitude", latitude)
            put("ownerLongitude", longitude)
            put("latitude", latitude)
            put("longitude", longitude)
            put("autoComplete", true)
            put("keyword", keyword)
            put("workingYn", working)
            put("registerYn", register)
            put("bookingYn", booking)
            put("beautyYn", beauty)
            put("hotelYn", hotel)
            put("allDayYn", allDay)
            put("parkingYn", parking)
            put("healthCheckYn", healthCheckYn)
            put("animalIdList", if(anmimalIdList.size > 0) JSONArray(anmimalIdList) else null)
            put("size", size)
            put("page", page)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postHospitalList(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalDetailRequest(
    apiService: ApiService,
    tag: String,
    _id: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HospitalDetailResponse>
    private lateinit var call: Call<HospitalDetailResponse>

    private val id = _id

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalDetail(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalAddBookmarkRequest(
    apiService: ApiService,
    tag: String,
    _id: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postHospitalBookmark(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalCancelBookmarkRequest(
    apiService: ApiService,
    tag: String,
    _id: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteHospitalBookmark(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalBookmarkStatusRequest(
    apiService: ApiService,
    tag: String,
    _id: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalBookmark(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalSearchKeywordRequest(
    apiService: ApiService,
    tag: String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HospitalKeywordResponse>
    private lateinit var call: Call<HospitalKeywordResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalSearchKeyword()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalRegisterInfoRequest(
    apiService: ApiService,
    tag: String,
    id:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalRegisterInfo(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalClinicRegisterRequest(
    apiService: ApiService,
    tag: String,
    id:Int,
    _roomId:Int,
    _petId:Int,
    _memo:String,
    _clinicIdList:MutableList<Int>
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id
    private val roomId = _roomId
    private val petId = _petId
    private val memo = _memo
    private val clinicIdList = _clinicIdList

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("petdocPetId", petId)
            put("memo", memo)
            put("centerId", centerId)
            put("clinicRoomId", roomId)
            put("clinicIdList", JSONArray(clinicIdList))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postClinicRegister(centerId, roomId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalReservationInfoRequest(
    apiService: ApiService,
    tag: String,
    id:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HospitalReservationInfoResponse>
    private lateinit var call: Call<HospitalReservationInfoResponse>

    private val centerId = id

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalReservationInfo(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalReservationClinicInfoRequest(
    apiService: ApiService,
    tag: String,
    id:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback:RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalBookingClinicInfo(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalReservationBeautyInfoRequest(
    apiService: ApiService,
    tag: String,
    id:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback:RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHospitalBookingBeautyInfo(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalReservationTimeTableListRequest(
    apiService: ApiService,
    tag: String,
    id:Int,
    _date:String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback:RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id
    private val date = _date

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getTimeTableList(centerId, date)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalClinicReservationRequest(
    apiService: ApiService,
    tag: String,
    id:Int,
    _roomId:Int,
    _petId:Int,
    _memo:String,
    _bookingTime:String,
    _clinicIdList:MutableList<Int>
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = id
    private val roomId = _roomId
    private val petId = _petId
    private val memo = _memo
    private val clinicIdList = _clinicIdList
    private val bookingTime = _bookingTime

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("petdocPetId", petId)
            put("memo", memo)
            put("centerId", centerId)
            put("clinicRoomId", roomId)
            put("bookingTime", bookingTime)
            put("clinicIdList", JSONArray(clinicIdList))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postClinicReservation(centerId, roomId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalRequireBookingRequest(
    apiService: ApiService,
    tag: String,
    _centerId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = _centerId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postHospitalRequestBooking(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalInfoEditRequest(
    apiService: ApiService,
    tag: String,
    _centerId:Int,
    _type:HashSet<String>
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val centerId = _centerId
    private val type = _type

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").toInt()

        val content = JSONObject().apply{
            put("userId", userId)
            put("centerId", centerId)
            put("proposalList", JSONArray(type.toList()))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postHospitalInfoEdit(centerId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class HospitalBookingCancelRequest(
    apiService: ApiService,
    tag: String,
    var centerId:Int,
    var type:String,
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

        call = apiService.deleteBookingCancel(centerId, type, bookingId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class DoctorListRequest(
    apiService: ApiService,
    tag: String,
    var centerId:Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<DoctorListResponse>
    private lateinit var call: Call<DoctorListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getDoctorList(centerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}