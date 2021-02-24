package kr.co.petdoc.petdoc.network.event

/**
 * Petdoc
 * Class: ApiErrorEvent
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class ApiErrorEvent {

    var requestTag: String
    var throwable: Throwable? = null

    /**
     * Create an error event without any further error information.
     *
     * @param requestTag Identifies the request that failed.
     */
    constructor(requestTag:String) {
        this.requestTag = requestTag
    }

    /**
     * Create an error event with detailed error information in the form of a RetrofitError.
     *
     * @param requestTag Identifies the request that failed.
     * @param t          An error object with detailed information.
     */
    constructor(requestTag: String, t:Throwable) {
        this.requestTag = requestTag
        this.throwable = t
    }
}