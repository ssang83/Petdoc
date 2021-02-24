package kr.co.petdoc.petdoc.network.request

import android.content.Context
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: AbstractApiRequest
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
abstract class AbstractApiRequest {
    /**
     * The endpoint for executing the calls.
     */
    protected val apiService: ApiService

    /**
     * Identifies the request.
     */
    protected val tag: String
    protected var context: Context

    protected constructor(apiService: ApiService, tag:String) {
        this.apiService = apiService
        this.tag = tag

        context = PetdocApplication.application!!
    }

    /**
     * Cancels the running request. The response will still be delivered but will be ignored. The
     * implementation should call invalidate on the callback which is used for the request.
     */
    abstract fun cancel()

    /**
     * Check for active internet connection
     *
     * @return boolean
     */
    internal fun isInternetActive(): Boolean {
        return Helper.isInternetActive(context)
    }

    abstract fun execute()
}