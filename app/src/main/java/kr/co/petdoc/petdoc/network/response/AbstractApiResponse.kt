package kr.co.petdoc.petdoc.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Petdoc
 * Class: AbstractApiResponse
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
open class AbstractApiResponse : Serializable {

    /**
     * @return The status
     */
    /**
     * @param status The status
     */
    @SerializedName("status")
    @Expose
    var status: Int = 0

    /**
     * @return The message
     */
    /**
     * @param message The message
     */
    @SerializedName("message")
    @Expose
    var message: String = ""

    /**
     * @return The code
     */
    /**
     * @param message The code
     */
    @SerializedName("code")
    @Expose
    var code: String = ""

    /**
     *
     */
    @SerializedName("detail")
    @Expose
    var detail: String = ""

    @SerializedName("messageKey")
    @Expose
    var messageKey: String? = null

    /**
     * Identifies the request which was executed to receive this response. The tag is used to make
     * sure that a class which executes a requests only handles the response which is meant for it.
     * This implies that the tag is unique.
     */
    var requestTag: String = ""
}