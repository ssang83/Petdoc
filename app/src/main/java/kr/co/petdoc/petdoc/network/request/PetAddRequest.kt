package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.repository.resetMyPetsDirty
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import java.io.File
import java.net.URLEncoder

/**
 * Petdoc
 * Class: PetAddRequest
 * Created by kimjoonsung on 2020/07/03.
 *
 * Description :
 */
class PetAddNameRequest(apiService: ApiService, tag: String, _name:String, var petId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val name = _name

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

        val content = JSONObject().apply{
            put("step", "1")
            put("name", name)
            put("userId", oldUserId)
            put("id", if(petId != -1) petId else null)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddName(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddNameWithProfileRequest(apiService: ApiService, tag: String, _name: String, _image:String, _petId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val name = _name
    private val image = _image
    private val petId = _petId

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

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", name)
            .addFormDataPart("userId", oldUserId.toString())

        if (petId != -1) {
            builder.addFormDataPart("id", petId.toString())
            builder.addFormDataPart("step", "1")
        }

        val file = File(image)
        val encodeFilename = URLEncoder.encode(file.name, "UTF-8")
        builder.addFormDataPart("imageFile", encodeFilename, RequestBody.create(MediaType.parse("image/jpeg"), file))

        call = apiService.postPetAddNameWithPicture(builder.build())
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetKindListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getPetKindList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetSpeciesListRequest(apiService: ApiService, tag: String, _kind:String, _name: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val kind = _kind
    private val name = _name

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getPetSepciesList(kind, name)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddKindRequest(apiService: ApiService, tag: String, _petId:Int, _kind:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val kind = _kind
    private val petId = _petId

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

        val content = JSONObject().apply{
            put("step", "2")
            put("kind", kind)
            put("id", petId)
            put("userId", oldUserId)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddKind(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddSpeciesRequest(apiService: ApiService, tag: String, _petId:Int, _species:String, _speciesId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val speceies = _species
    private val speceiesId = _speciesId

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

        val content = JSONObject().apply{
            put("step", "3")
            put("species", speceies)
            put("speciesId", speceiesId)
            put("id", petId)
            put("userId", oldUserId)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddSpecies(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddBirthRequest(apiService: ApiService, tag: String, _petId:Int, _birthday:String, _ageType:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val birthday = _birthday
    private val ageType = _ageType

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

        val content = JSONObject().apply{
            put("step", "4")
            put("id", petId)
            put("userId", oldUserId)
            put("birthday", birthday)
            put("ageType", ageType)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddBirth(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddGenderRequest(apiService: ApiService, tag: String, _petId:Int, _gender:String, _neutrtion:Boolean) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val gender = _gender
    private val neutrition = _neutrtion

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

        val content = JSONObject().apply{
            put("step", "5")
            put("id", petId)
            put("userId", oldUserId)
            put("gender", gender)
            put("isNeutralized", neutrition)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddGender(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetAddVaccineStatusRequest(apiService: ApiService, tag: String, _petId:Int, _status:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId
    private val status = _status

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

        val content = JSONObject().apply{
            put("step", "6")
            put("id", petId)
            put("userId", oldUserId)
            put("inoculationStatus", status)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetAddVaccine(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PetDeleteRequest(apiService: ApiService, tag: String, _petId:Int) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val petId = _petId

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

        val content = JSONObject().apply{
            put("id", petId)
            put("userId", oldUserId)
            put("isActive", false)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPetDelete(body)
        call.enqueue(callback)
        resetMyPetsDirty()
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}