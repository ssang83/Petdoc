package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.DetailDiagnosisListResponse
import kr.co.petdoc.petdoc.repository.resetMyPetsDirty
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: MatchFoodRequest
 * Created by kimjoonsung on 2020/07/14.
 *
 * Description :
 */
class FoodRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMatchFoodList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class DiagnosisResultRequest(apiService: ApiService, tag: String, _petId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMatchResult(petId)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class SpeciesInfoRequest(apiService: ApiService, tag: String, _speciesId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val speciesId = _speciesId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getDiagnosisSpecies(speciesId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class CustomealInfoRequest(apiService: ApiService, tag: String, _petId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getCustomealInfo(petId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class FeedInfoRequest(apiService: ApiService, tag: String, _feedId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val feedId = _feedId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getFeedInfo(feedId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class NutritionInfoRequest(apiService: ApiService, tag: String, _pnId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val pnId = _pnId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getNutritionInfo(pnId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class WeightUploadRequest(apiService: ApiService, tag: String, _petId: Int, _cmInfoId:Int, _weight:Float) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val weight = _weight

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("weight", weight)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class BodyTypeUploadRequest(apiService: ApiService, tag: String, _petId: Int, _cmInfoId:Int, _type:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val type = _type

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserID = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserID)
            put("petId", petId)
            put("id", cmInfoId)
            put("bodyType", type)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class WalkTimeUploadRequest(apiService: ApiService, tag: String, _petId: Int, _cmInfoId:Int, _time:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val time = _time

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("walkTime", time)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class LikeFoodUploadRequest(apiService: ApiService, tag: String, _petId: Int, _cmInfoId:Int, _food:HashSet<Int>) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val food = _food

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("favoriteFoodList", JSONArray(food.toList()))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class AllergeFoodUploadRequest(apiService: ApiService, tag: String, _petId: Int, _cmInfoId:Int, _food:HashSet<Int>) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val food = _food

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("allergyFoodIdList", JSONArray(food.toList()))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class CurrentFeedUploadRequest(
    apiService: ApiService,
    tag: String,
    _petId: Int,
    _cmInfoId: Int,
    _dryFood: String,
    _wetFood: String,
    _snack: String,
    _dryFoodName: String,
    _wetFoddName: String,
    _snackName: String
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val dryFood = _dryFood
    private val dryFoodName = _dryFoodName
    private val wetFood = _wetFood
    private val wetFoodName = _wetFoddName
    private val snack = _snack
    private val snackName = _snackName

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("dryFoodYn", dryFood)
            put("wetFoodYn", wetFood)
            put("snackYn", snack)
            put("dryFoodName", dryFoodName)
            put("wetFoodName", wetFoodName)
            put("snackName", snackName)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PregnantStatusUploadRequest(
    apiService: ApiService,
    tag: String,
    _petId: Int,
    _cmInfoId: Int,
    _value:String,
    _state:Int
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val pregnancy = _value
    private val pregnancyState = _state

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val oldUserId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()

        val content = JSONObject().apply {
            put("userId", oldUserId)
            put("petId", petId)
            put("id", cmInfoId)
            put("pregnancy", pregnancy)
            put("pregnancyState", pregnancyState)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFood(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class DetailDiagnosisListRequest(
    apiService: ApiService,
    tag: String,
    _petId: Int,
    _cmInfoId: Int
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<DetailDiagnosisListResponse>
    private lateinit var call: Call<DetailDiagnosisListResponse>

    private val petId = _petId
    private val cmInfoId = _cmInfoId

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getDiagnosisDetailList(petId, cmInfoId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class DetailDiagnosisUploadRequest(
    apiService: ApiService,
    tag: String,
    _petId: Int,
    _cmInfoId: Int,
    _questionId:HashSet<Int>
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback:RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val cmInfoId = _cmInfoId
    private val questionId = _questionId

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply {
            put("questionIds", JSONArray(questionId))
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postDiagnosisDetail(petId, cmInfoId, body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MatchFoodResultRequest(
    apiService: ApiService,
    tag: String,
    var petId: Int
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback:RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply {
            put("petId", petId)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postMatchFoodResult(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}
