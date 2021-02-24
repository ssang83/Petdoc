package kr.co.petdoc.petdoc.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.atomic.AtomicInteger

/**
 * Petdoc
 * Class: MyFirebaseMessagingService
 * Created by kimjoonsung on 2020/09/07.
 *
 * Description :
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"
    private val REGISTER_PUSH_TOKEN = "$TAG.registerPushToken"

    companion object {
        private val NOTIFICATION_CHANNEL_ID = "Petdoc_Channel_ID"

        // NOTIFICATIOM ID 값 추출
        private val c = AtomicInteger(0)
        fun getNotificationID() = c.incrementAndGet()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d("onNewToken : $token")
        if (token.isNotEmpty()) {
            StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_FCM_PUSH_TOKEN, token)
            registerPushToken()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Logger.e("onMessageReceived data: ${message.data}")

        EventBus.getDefault().post("onMessageReceived")
        sendNotification(message)
    }

    private fun sendNotification(message: RemoteMessage?) {
        if (message == null) {
            Logger.e("message null!!")
            return
        }

        val type = message.data["type"]
        Logger.e("sendNotification type : $type")

        //{pk=289, type=banner, body=(광고) 가을 산책 필수품, 슬개골 보호 의류 파텔라 한정 수량 첫 할인!}
        val id = message.data["pk"]
        val body = message.data["body"]
        Logger.e("id : $id, body : $body")

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("noti_type", type)
        intent.putExtra("noti_id", id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pi = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
//                .setContentTitle(title)
            .setContentText(body)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_notification)
            .setColor(Helper.readColorRes(R.color.dark_grey))
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = applicationInfo.loadLabel(packageManager).toString()
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
            }

            nm.createNotificationChannel(channel)
        }

        nm.notify(getNotificationID(), builder.build())
    }

    private fun registerPushToken() {
        val token = StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_FCM_PUSH_TOKEN, "")
        Logger.e("registerPushToken getPushToken:$token")

        if (token.isNotEmpty()) {
            PetdocApplication.application.apiClient.registerFCMToken(REGISTER_PUSH_TOKEN, token)
        }
    }
}