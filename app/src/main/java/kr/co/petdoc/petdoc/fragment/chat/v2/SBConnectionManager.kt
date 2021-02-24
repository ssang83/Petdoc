package kr.co.petdoc.petdoc.fragment.chat.v2

import android.content.Context
import com.sendbird.android.SendBird
import com.sendbird.android.SendBird.ConnectHandler
import com.sendbird.android.SendBird.DisconnectHandler
import com.sendbird.desk.android.SendBirdDesk
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: ConnectionManager
 * Created by kimjoonsung on 11/30/20.
 *
 * Description :
 */
class SBConnectionManager {

    companion object {
        fun login(userId: String?, handler: ConnectHandler?) {
            SendBird.connect(userId, ConnectHandler { user, e ->
                if (e != null) {
                    handler?.onConnected(user, e)
                    return@ConnectHandler
                }
                SendBirdDesk.authenticate(userId, null) { e -> handler?.onConnected(user, e) }
            })
        }

        fun logout(handler: DisconnectHandler?) {
            SendBird.disconnect { handler?.onDisconnected() }
        }

        fun addConnectionManagementHandler(context: Context, handlerId: String?, handler: ConnectionManagementHandler?) {
            SendBird.addConnectionHandler(handlerId, object : SendBird.ConnectionHandler {
                override fun onReconnectStarted() {}
                override fun onReconnectSucceeded() {
                    handler?.onConnected(true)
                }

                override fun onReconnectFailed() {}
            })
            if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
                handler?.onConnected(false)
            } else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) { // push notification or system kill
                val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")
                SendBird.connect(userId, ConnectHandler { user, e ->
                    if (e != null) {
                        return@ConnectHandler
                    }
                    SendBirdDesk.authenticate(userId, null, SendBirdDesk.AuthenticateHandler { e ->
                        if (e != null) {
                            return@AuthenticateHandler
                        }
                        handler?.onConnected(false)
                    })
                })
            }
        }

        fun removeConnectionManagementHandler(handlerId: String?) {
            SendBird.removeConnectionHandler(handlerId)
        }
    }

    interface ConnectionManagementHandler {
        /**
         * A callback for when connected or reconnected to refresh.
         *
         * @param reconnect Set false if connected, true if reconnected.
         */
        fun onConnected(reconnect: Boolean)
    }
}