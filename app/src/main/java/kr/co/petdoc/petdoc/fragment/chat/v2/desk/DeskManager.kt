package kr.co.petdoc.petdoc.fragment.chat.v2.desk

import android.content.Context
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ChannelHandler
import com.sendbird.desk.android.SendBirdDesk
import com.sendbird.desk.android.Ticket
import java.util.concurrent.ConcurrentHashMap

/**
 * Petdoc
 * Class: DeskManager
 * Created by kimjoonsung on 12/9/20.
 *
 * Description :
 */
class DeskManager {

    abstract class TicketHandler {
        abstract fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage)
        abstract fun onChannelChanged(baseChannel: BaseChannel)
        abstract fun onMessageUpdated(baseChannel: BaseChannel, message: BaseMessage)
    }

    companion object {
        const val CONNECTION_HANDLER_ID_OPEN_TICKETS = "CONNECTION_HANDLER_ID_OPEN_TICKETS"
        const val CONNECTION_HANDLER_ID_CLOSED_TICKETS = "CONNECTION_HANDLER_ID_CLOSED_TICKETS"
        const val CONNECTION_HANDLER_ID_CHAT = "CONNECTION_HANDLER_ID_CHAT"

        const val TICKET_HANDLER_ID_GLOBAL = "TICKET_HANDLER_ID_GLOBAL"
        const val TICKET_HANDLER_ID_OPEN_TICKETS = "TICKET_HANDLER_ID_OPEN_TICKETS"
        const val TICKET_HANDLER_ID_CHAT = "TICKET_HANDLER_ID_CHAT"

        private var sInstance: DeskManager? = null
        private var mTicketHandlers: ConcurrentHashMap<String, TicketHandler>? = null
        private lateinit var mContext:Context

        @Synchronized
        fun init(context: Context) {
            if (sInstance == null) {
                sInstance = DeskManager()
                sInstance!!.addGlobalTicketHandler(context)
            }

            mContext = context
        }

        @Synchronized
        fun getInstance(): DeskManager? {
            if (sInstance == null) {
                throw RuntimeException("DeskManager instance hasn't been initialized.")
            }
            return sInstance
        }

        fun isTicketClosed(ticket: Ticket?): Boolean {
            var result = false
            if (ticket != null && ticket.status == "CLOSED") {
                result = true
            }

            return result
        }

        fun getLastMessage(ticket: Ticket): BaseMessage? {
            var lastMessage: BaseMessage? = null
            val groupChannel = ticket.channel
            if (groupChannel != null) {
                lastMessage = groupChannel.lastMessage
            }

            return lastMessage
        }

        fun getUnreadMessageCount(ticket: Ticket): Int {
            var unreadMessageCount = 0
            val groupChannel = ticket.channel
            if (groupChannel != null) {
                unreadMessageCount = groupChannel.unreadMessageCount
            }

            return unreadMessageCount
        }

        fun addTicketHandler(identifier: String?, handler: TicketHandler?) {
            if (identifier == null || identifier.length == 0 || handler == null) {
                return
            }

            SendBird.addChannelHandler(identifier, object : ChannelHandler() {
                override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                    if (!SendBirdDesk.isDeskChannel(baseChannel)) return
                    handler.onMessageReceived(baseChannel, baseMessage)
                }

                override fun onChannelChanged(channel: BaseChannel) {
                    if (!SendBirdDesk.isDeskChannel(channel)) return
                    handler.onChannelChanged(channel)
                }

                override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {
                    if (!SendBirdDesk.isDeskChannel(channel)) return
                    handler.onMessageUpdated(channel, message)
                }
            })

            mTicketHandlers?.put(identifier, handler)
        }

        fun removeTicketHandler(identifier: String?): TicketHandler? {
            if (identifier == null || identifier.length == 0) {
                return null
            }

            SendBird.removeChannelHandler(identifier)
            return mTicketHandlers?.remove(identifier)
        }
    }

    init {
        mTicketHandlers = ConcurrentHashMap()
    }

    private fun addGlobalTicketHandler(context: Context) {
        addTicketHandler(TICKET_HANDLER_ID_GLOBAL, object : TicketHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {

            }

            override fun onChannelChanged(channel: BaseChannel) {

            }

            override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {

            }
        })
    }
}