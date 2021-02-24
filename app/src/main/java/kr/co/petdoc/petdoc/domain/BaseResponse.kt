package kr.co.petdoc.petdoc.domain

interface BaseResponse {
    val status: String
    fun isSuccess() = (status == "ok")

}