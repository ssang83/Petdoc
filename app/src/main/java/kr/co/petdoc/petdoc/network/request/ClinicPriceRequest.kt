package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.BannerDetailResponse
import kr.co.petdoc.petdoc.network.response.PriceCalcuateResponse
import kr.co.petdoc.petdoc.network.response.PriceCategoryListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ClinicPriceRequest
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class PriceCategoryListRequest(apiService: ApiService, tag: String, type: String) :
    AbstractApiRequest(apiService, tag) {

    private val categoryType = type

    private lateinit var callback: ApiCallback<PriceCategoryListResponse>

    private lateinit var call: Call<PriceCategoryListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getClinicPriceCategory(categoryType)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PriceCalculateRequest(
    apiService: ApiService,
    tag: String,
    _type: String,
    _gender: String,
    _age: Int,
    _weight: String,
    _categoryId: Int
) :
    AbstractApiRequest(apiService, tag) {

    private val type = _type
    private val gender = _gender
    private val age = _age
    private val weight = _weight
    private val categoryId = _categoryId

    private lateinit var callback: ApiCallback<PriceCalcuateResponse>

    private lateinit var call: Call<PriceCalcuateResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getClinicPriceCalculate(type, gender, age, weight, categoryId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}