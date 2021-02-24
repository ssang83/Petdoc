package kr.co.petdoc.petdoc.network.event

/**
 * Petdoc
 * Class: RequestFinishedEvent
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class RequestFinishedEvent(requestTag: String) {

    /**
     * Identifies the request.
     */
    val requestTag: String

    init {
        this.requestTag = requestTag
    }
}