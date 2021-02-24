package kr.co.petdoc.petdoc.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import org.greenrobot.eventbus.EventBus

/**
 * Petdoc
 * Class: KeyboardHeightProvider
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class KeyboardHeightProvider(activity: Activity) : PopupWindow(activity) {
    /**
     * The cached landscape height of the keyboard
     */
    private var keyboardLandscapeHeight = 0

    /**
     * The cached portrait height of the keyboard
     */
    private var keyboardPortraitHeight = 0

    /**
     * The view that is used to calculate the keyboard height
     */
    private val popupView: View?

    /**
     * The parent view
     */
    private val parentView: View

    /**
     * The root activity that uses this KeyboardHeightProvider
     */
    private val activity: Activity

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {
        if (!isShowing && parentView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    fun close() {
        dismiss()
    }

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private fun getScreenOrientation(): Int {
        return activity.resources.configuration.orientation
    }

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity window height.
     */
    private fun handleOnGlobalLayout() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        popupView!!.getWindowVisibleDisplayFrame(rect)
        val orientation = getScreenOrientation()
        var topCutoutHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            topCutoutHeight = getTopCutoutHeight()
        }
        val keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            keyboardPortraitHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation)
        } else {
            keyboardLandscapeHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation)
        }
    }

    /**
     * 버스로 보낸다.
     */
    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        EventBus.getDefault().post(
            SoftKeyboardBus(
                height,
                orientation
            )
        )

//        if (observer != null) {
//            observer.onKeyboardHeightChanged(height, orientation);
//        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun getTopCutoutHeight(): Int {
        val decorView = activity.window.decorView ?: return 0
        var cutOffHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val windowInsets = decorView.rootWindowInsets
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val displayCutout = windowInsets.displayCutout
                if (displayCutout != null) {
                    val list =
                        displayCutout.boundingRects
                    for (rect in list) {
                        if (rect.top == 0) {
                            cutOffHeight += rect.bottom - rect.top
                        }
                    }
                }
            }
        }
        return cutOffHeight
    }

    /**
     * Construct a new KeyboardHeightProvider
     *
     * @param activity The parent activity
     */
    init {
        this.activity = activity
        val li = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = li.inflate(R.layout.popup_window, null, false)
        contentView = popupView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
        }

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED
        parentView = activity.findViewById(android.R.id.content)
        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
        popupView!!.viewTreeObserver.addOnGlobalLayoutListener {
            if (popupView != null) {
                handleOnGlobalLayout()
            }
        }
    }
}
