package kr.co.petdoc.petdoc.network.response

import okhttp3.Headers

/**
 * Petdoc
 * Class: NetworkBusResponse
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description : Network Response EventBus
 */
class NetworkBusResponse @JvmOverloads constructor(
    status: String = "",
    tag: String = "",
    response: String = "",
    header: Headers? = null,
    code: String = ""
) {
    var status: String
    var tag: String
    var response: String
    var code: String
    var header:Headers?

    init {
        this.status = status
        this.tag = tag
        this.response = response
        this.code = code
        this.header = header
    }
}