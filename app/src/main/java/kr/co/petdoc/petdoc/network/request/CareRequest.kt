package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.db.care.body.Temperature
import kr.co.petdoc.petdoc.db.care.humidity.Humidity
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.BodyTemperatureRecordListResponse
import kr.co.petdoc.petdoc.network.response.CareHistoryListRespone
import kr.co.petdoc.petdoc.network.response.CareRecordListResponse
import kr.co.petdoc.petdoc.network.response.HumidityRecordListResponse
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: CareRequest
 * Created by kimjoonsung on 2020/06/22.
 *
 * Description :
 */
class CareRecordListRequest(apiService: ApiService, tag: String, _petId: Int, _date: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<CareRecordListResponse>
    private lateinit var call: Call<CareRecordListResponse>

    private val petId = _petId
    private val date = _date

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getCareRecordList(petId, date)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class CareRecordUploadRequest(apiService: ApiService, tag: String, _petId: Int, _value:String, _date: String, _type:String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val date = _date
    private val value = _value
    private val type = _type

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("pet_id", petId)
            put("value", value)
            put("check_date", date)
            put("type", type)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postCareRecordUpload(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class CareRecordDeleteRequest(apiService: ApiService, tag: String, _petId: Int) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.deleteCareRecord(petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class CareHistoryListRequest(apiService: ApiService, tag: String, _petId: Int, _date: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<CareHistoryListRespone>
    private lateinit var call: Call<CareHistoryListRespone>

    private val petId = _petId
    private val date = _date

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getCareHistoryList(petId, date)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class TemperatureUploadRequest(apiService: ApiService, tag: String, _temperature:List<Temperature>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _temperature

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val jsonArray = mutableListOf<JSONObject>()
        for (item in items) {
            val obj = JSONObject().apply{
                put("petId", item.petId)
                put("value", item.value)
                put("regDate", item.regDate)
            }

            jsonArray.add(obj)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), jsonArray.toString())

        call = apiService.postBodyTemperature(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HumidityUploadRequest(apiService: ApiService, tag: String, _humidity:List<Humidity>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _humidity

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val jsonArray = mutableListOf<JSONObject>()
        for (item in items) {
            val obj = JSONObject().apply{
                put("petId", item.petId)
                put("tvalue", item.tvalue)
                put("hvalue", item.hvalue)
                put("regDate", item.regDate)
            }

            jsonArray.add(obj)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), jsonArray.toString())

        call = apiService.postHumidity(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class EarImageUploadRequest(apiService: ApiService, tag: String, _petId: Int, _earImage:List<EarImage>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _earImage
    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        for (item in items) {
            if (item.leftEarImage != null && item.rightEarImage != null) {
                val leftFile = File(item.leftEarPath)
                val rightFile = File(item.rightEarPath)

                builder.addFormDataPart(
                    "file",
                    "${item.year}${item.month}${item.day}_ear_L.jpg",
                    RequestBody.create(MediaType.parse("image/jpeg"), leftFile)
                )

                builder.addFormDataPart(
                    "file",
                    "${item.year}${item.month}${item.day}_ear_R.jpg",
                    RequestBody.create(MediaType.parse("image/jpeg"), rightFile)
                )
            } else if (item.leftEarImage != null) {
                val leftFile = File(item.leftEarPath)
                builder.addFormDataPart(
                    "file",
                    URLEncoder.encode("${item.year}${item.month}${item.day}_ear_L", "UTF-8"),
                    RequestBody.create(MediaType.parse("image/jpeg"), leftFile)
                )
            } else if (item.rightEarImage != null) {
                val rightFile = File(item.rightEarPath)
                builder.addFormDataPart(
                    "file",
                    URLEncoder.encode("${item.year}${item.month}${item.day}_ear_R", "UTF-8"),
                    RequestBody.create(MediaType.parse("image/jpeg"), rightFile)
                )
            }
        }

        call = apiService.postEarImage(userId, petId, builder.build())
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class TempInjectUploadRequest(apiService: ApiService, tag: String, _petId:Int, _temperature: List<Temperature>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _temperature
    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val jsonArray = mutableListOf<JSONObject>()
        for (item in items) {
            val obj = JSONObject().apply{
                put("inject", item.dosage)
                put("regDate", item.regDate)
            }

            jsonArray.add(obj)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), jsonArray.toString())

        call = apiService.postTemperatureInject(petId, body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class TemperatureDeleteRequest(apiService: ApiService, tag: String, _petId:Int, _temperature: List<Temperature>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _temperature
    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var idList = ""
        for (i in 0 until items.size) {
            if (i != items.size - 1) {
                idList += "${items[i].regDate}, "
            } else {
                idList += "${items[i].regDate}"
            }
        }

        call = apiService.deleteTemperatureRecord(petId, idList)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HumidityDeleteRequest(apiService: ApiService, tag: String, _petId: Int, _humidity:List<Humidity>) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val items = _humidity
    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var idList = ""
        for (i in 0 until items.size) {
            if (i != items.size - 1) {
                idList += "${items[i].regDate}, "
            } else {
                idList += "${items[i].regDate}"
            }
        }

        call = apiService.deleteHumidityRecord(petId, idList)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class BodyTemperatureRecordListRequest(apiService: ApiService, tag: String, _petId: Int, _offset:Int, _limit:Int) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<BodyTemperatureRecordListResponse>
    private lateinit var call: Call<BodyTemperatureRecordListResponse>

    private val petId = _petId
    private val offset = _offset
    private val limit = _limit

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getBodyTemperatureRecordList(petId, offset, limit)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class HumidityRecordListRequest(apiService: ApiService, tag: String, _petId: Int, _offset:Int, _limit:Int) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<HumidityRecordListResponse>
    private lateinit var call: Call<HumidityRecordListResponse>

    private val petId = _petId
    private val offset = _offset
    private val limit = _limit

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getHumidityRecordList(petId, offset, limit)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}