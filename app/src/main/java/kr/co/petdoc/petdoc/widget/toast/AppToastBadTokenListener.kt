package kr.co.petdoc.petdoc.widget.toast

import android.widget.Toast

/**
 * Petdoc
 * Class: AppToastBadTokenListener
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : Toast 출력 간 WindowBadTokenException 발생을 확인하는 EventListener
 */
interface AppToastBadTokenListener {
    fun onBadTokenCaught(toast:Toast)
}