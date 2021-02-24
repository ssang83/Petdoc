package kr.co.petdoc.petdoc.toast_push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.toast.android.push.ToastPushMessageReceiver
import com.toast.android.push.message.ToastRemoteMessage
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.atomic.AtomicInteger

/**
 * Petdoc
 * Class: MyPushMessageReceiver
 * Created by kimjoonsung on 2020/08/04.
 *
 * Description :
 */
class MyToastPushMessageReceiver : ToastPushMessageReceiver() {

    companion object {
        private val NOTIFICATION_CHANNEL_ID = "Petdoc_Channel_ID"

        // NOTIFICATIOM ID 값 추출
        private val c = AtomicInteger(0)
        fun getNotificationID() = c.incrementAndGet()
    }

    override fun onMessageReceived(context: Context, remoteMessage: ToastRemoteMessage) {
        Logger.d("remoteMessage : ${remoteMessage.message}")

        EventBus.getDefault().post("onMessageReceived")

        val title = remoteMessage.message.title
        val body = remoteMessage.message.body

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("noti_type", "test")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pi = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(body)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_notification)
            .setColor(Helper.readColorRes(R.color.dark_grey))
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 알림이 표시 되지 않는다...처음에는 되었는데...안되서 우선 안드로이드 기본 알림으로 대체 합니다..
//        notify(context, getNotificationID(), builder.build())

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appName = context.applicationInfo.loadLabel(context.packageManager).toString()
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_HIGH).apply {
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }

        nm.notify(getNotificationID(), builder.build())
    }

}