package kr.co.petdoc.petdoc.network.event

/**
 * Petdoc
 * Class: ApiErrorWithMessageEvent
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class ApiErrorWithMessageEvent(requestTag: String, resultMsgUser: String) {

    val requestTag: String
    val resultMsgUser: String

    init {
        this.requestTag = requestTag
        this.resultMsgUser = resultMsgUser
    }
}