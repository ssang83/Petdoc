package kr.co.petdoc.petdoc.fragment.chat.v2.desk

import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBirdException
import com.sendbird.android.UserMessage
import com.sendbird.android.shadow.com.google.gson.JsonParser
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: DeskUserRichMessage
 * Created by kimjoonsung on 12/10/20.
 *
 * Description :
 */
class DeskUserRichMessage {

    companion object {
        private const val USER_RICH_MESSAGE_CUSTOM_TYPE = "SENDBIRD_DESK_RICH_MESSAGE"
        private const val EVENT_TYPE_INQUIRE_CLOSURE = "SENDBIRD_DESK_INQUIRE_TICKET_CLOSURE"
        private const val INQUIRE_CLOSURE_STATE_WAITING = "WAITING"
        private const val INQUIRE_CLOSURE_STATE_CONFIRMED = "CONFIRMED"
        private const val INQUIRE_CLOSURE_STATE_DECLINED = "DECLINED"
        const val EVENT_TYPE_URL_PREVIEW = "SENDBIRD_DESK_URL_PREVIEW"

        fun isInquireCloserType(message: BaseMessage): Boolean {
            var result = false
            if (isMessage(message)) {
                val el = JsonParser().parse((message as UserMessage).data)
                if (!el.isJsonNull && el.asJsonObject.has("type")
                        && el.asJsonObject["type"].asString == EVENT_TYPE_INQUIRE_CLOSURE) {
                    result = true
                }
            }

            return result
        }

        fun isInquireCloserTypeWaitingState(message: BaseMessage): Boolean {
            var result = false
            val state = getInquireCloserTypeState(message)
            if (state != null && state == INQUIRE_CLOSURE_STATE_WAITING) {
                result = true
            }

            return result
        }

        fun isInquireCloserTypeConfirmedState(message: BaseMessage): Boolean {
            var result = false
            val state = getInquireCloserTypeState(message)
            if (state != null && state == INQUIRE_CLOSURE_STATE_CONFIRMED) {
                result = true
            }

            return result
        }

        fun isInquireCloserTypeDeclinedState(message: BaseMessage): Boolean {
            var result = false
            val state = getInquireCloserTypeState(message)
            if (state != null && state == INQUIRE_CLOSURE_STATE_DECLINED) {
                result = true
            }

            return result
        }

        fun isUrlPreviewType(message: BaseMessage): Boolean {
            var result = false
            if (isMessage(message)) {
                val el = JsonParser().parse((message as UserMessage).data)
                if (!el.isJsonNull && el.asJsonObject.has("type")
                        && el.asJsonObject["type"].asString == EVENT_TYPE_URL_PREVIEW) {
                    result = true
                }
            }

            return result
        }

        fun getUrlPreviewInfo(message: BaseMessage): UrlPreviewInfo? {
            var info: UrlPreviewInfo? = null
            try {
                if (isUrlPreviewType(message)) {
                    info = UrlPreviewInfo((message as UserMessage).data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return info
        }

        fun updateUserMessageWithUrl(channel: GroupChannel, message: BaseMessage, text: String, url: String, handler: UpdateUserMessageWithUrlHandler?) {
            object : UrlPreviewAsyncTask() {
                override fun onPostExecute(info: UrlPreviewInfo?) {
                    try {
                        if (info != null) {
                            val jsonString = info.toJsonString()
                            channel.updateUserMessage(message.messageId, text, jsonString, USER_RICH_MESSAGE_CUSTOM_TYPE, { userMessage, exception ->
                                if (exception != null) {
                                    if (handler != null) {
                                        handler.onResult(null, exception)
                                    }

                                    return@updateUserMessage
                                }

                                if (handler != null) {
                                    handler.onResult(userMessage, null)
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                        if (handler != null) {
                            handler.onResult(null, SendBirdException("Url parsing error..."))
                        }
                    }
                }
            }.execute(url)
        }

        private fun isMessage(message: BaseMessage):Boolean {
            return message != null && message is UserMessage && message.customType == USER_RICH_MESSAGE_CUSTOM_TYPE
        }

        private fun getInquireCloserTypeState(message: BaseMessage): String? {
            var state: String? = null
            if (isInquireCloserType(message)) {
                val data = JsonParser().parse((message as UserMessage).data).asJsonObject
                val body = data["body"].asJsonObject
                state = body["state"].asString
            }
            return state
        }
    }

    interface UpdateUserMessageWithUrlHandler {
        fun onResult(userMessage: UserMessage?, e: SendBirdException?)
    }
}