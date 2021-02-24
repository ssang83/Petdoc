package kr.co.petdoc.petdoc.fragment.chat.v2.desk

import com.sendbird.android.AdminMessage
import com.sendbird.android.BaseMessage
import com.sendbird.android.shadow.com.google.gson.JsonParser
import org.json.JSONObject

/**
 * Petdoc
 * Class: DeskAdminMessage
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
object DeskAdminMessage {

    private const val ADMIN_MESSAGE_CUSTOM_TYPE = "SENDBIRD_DESK_ADMIN_MESSAGE_CUSTOM_TYPE"
    private const val EVENT_TYPE_ASSIGN = "TICKET_ASSIGN"
    private const val EVENT_TYPE_TRANSFER = "TICKET_TRANSFER"
    private const val EVENT_TYPE_CLOSE = "TICKET_CLOSE"

    fun isMessage(message: BaseMessage?): Boolean {
        return message != null && message is AdminMessage && message.customType == ADMIN_MESSAGE_CUSTOM_TYPE
    }

    fun isAssignType(message: BaseMessage): Boolean {
        var result = false
        if (isMessage(message)) {
            val data = (message as AdminMessage).data
            if (data != null && data.length > 0) {
                val dataObj = JsonParser().parse(data) as JSONObject
                val type = dataObj["type"].toString()
                if (type == EVENT_TYPE_ASSIGN) {
                    result = true
                }
            }
        }

        return result
    }

    fun isTransferType(message: BaseMessage): Boolean {
        var result = false
        if (isMessage(message)) {
            val data = (message as AdminMessage).data
            if (data != null && data.length > 0) {
                val dataObj = JsonParser().parse(data) as JSONObject
                val type = dataObj["type"].toString()
                if (type == EVENT_TYPE_TRANSFER) {
                    result = true
                }
            }
        }
        return result
    }

    fun isCloseType(message: BaseMessage): Boolean {
        var result = false
        if (isMessage(message)) {
            val data = (message as AdminMessage).data
            if (data != null && data.length > 0) {
                val dataObj = JsonParser().parse(data) as JSONObject
                val type = dataObj["type"].toString()
                if (type == EVENT_TYPE_CLOSE) {
                    result = true
                }
            }
        }

        return result
    }
}