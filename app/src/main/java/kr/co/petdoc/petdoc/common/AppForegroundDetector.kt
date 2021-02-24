package kr.co.petdoc.petdoc.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import kr.co.petdoc.petdoc.log.Logger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Petdoc
 * Class: AppForegroundDetector
 * Created by kimjoonsung on 2020/10/28.
 *
 * Description :
 */
class AppForegroundDetector : Application.ActivityLifecycleCallbacks {
    interface Listener {
        fun onCreated(activityName: String)
        fun onStarted(activityName: String?)
        fun onResumed(activityName: String?)
        fun onPaused(activityName: String?)
        fun onStopped(activityName: String?)
        fun onDestroyed(activityName: String?)
    }

    var listenerList: CopyOnWriteArrayList<Listener> = CopyOnWriteArrayList()

    fun addListener(listener: Listener) = listenerList.add(listener)

    fun removeListener(listener: Listener) = listenerList.remove(listener)

    override fun onActivityStarted(activity: Activity?) {
//        Logger.d("onActivityStarted: ${activity?.javaClass?.name}")

        try {
            listenerList.forEach { listener ->
                listener.onStarted(activity?.javaClass?.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityStopped(activity: Activity?) {
//        Logger.d("onActivityStopped: ${activity?.javaClass?.name}")

        try {
            listenerList.forEach { listener ->
                listener.onStopped(activity?.javaClass?.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityPaused(activity: Activity?) {
//        Logger.d("onActivityPaused: ${activity?.javaClass?.name}")
    }

    override fun onActivityResumed(activity: Activity?) {
//        Logger.d("onActivityResumed: ${activity?.javaClass?.name}")

        try {
            listenerList.forEach { listener ->
                listener.onResumed(activity?.javaClass?.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityDestroyed(activity: Activity?) {
//        Logger.i("onActivityDestroyed: ${activity?.javaClass?.name}")
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
//        Logger.i("onActivityCreated: ${activity?.javaClass?.name}")
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
}