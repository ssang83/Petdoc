package kr.co.petdoc.petdoc.widget.toast

import android.content.Context
import android.content.ContextWrapper
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import kr.co.petdoc.petdoc.log.Logger
import org.jetbrains.annotations.NotNull

/**
 * Petdoc
 * Class: AppSafeToastContext
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description :
 * WindowBadTokenException Crash 대응을 위한 ToastContext by kotlin
 * (https://github.com/drakeet/ToastCompat/blob/master/library/src/main/java/me/drakeet/support/toast/AppSafeToastContext.java)
 */
class AppSafeToastContext(
    @NotNull base: Context,
    @NotNull toast: Toast,
    @NotNull tokenListener: AppToastBadTokenListener
) : ContextWrapper(base) {

    private var toast = toast
    private var badTokenListener = tokenListener

    override fun getApplicationContext(): Context {
        return ApplicationContextWrapper(baseContext, toast, badTokenListener)
    }

    class ApplicationContextWrapper(base: Context, toast: Toast, badTokenListener: AppToastBadTokenListener) : ContextWrapper(base) {
        private var toast = toast
        private var badTokenListener = badTokenListener

        override fun getSystemService(name: String): Any {
            if (Context.WINDOW_SERVICE == name) {
                return WindowManagerWrapper(baseContext.getSystemService(name) as WindowManager, toast, badTokenListener)
            }
            return super.getSystemService(name)
        }
    }

    class WindowManagerWrapper(base: WindowManager, toast: Toast, badTokenListener: AppToastBadTokenListener) :
        WindowManager {

        private var base: WindowManager = base
        private var badTokenListener: AppToastBadTokenListener = badTokenListener
        private var toast: Toast = toast

        override fun getDefaultDisplay(): Display {
            return base.defaultDisplay
        }

        override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
            try {
                base.addView(view, params)
            } catch (e: WindowManager.BadTokenException) {
                if (badTokenListener != null) badTokenListener?.onBadTokenCaught(toast)
            } catch (t: Throwable) {
                Logger.e("[addView]", t.toString())
            }
        }

        override fun updateViewLayout(view: View?, params: ViewGroup.LayoutParams?) {
            base.updateViewLayout(view, params)
        }

        override fun removeView(view: View?) {
            base.removeView(view)
        }

        override fun removeViewImmediate(view: View?) {
            base.removeViewImmediate(view)
        }
    }
}