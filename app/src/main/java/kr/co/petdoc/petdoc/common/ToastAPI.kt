package kr.co.petdoc.petdoc.common

import android.content.Context
import com.toast.android.ToastSdk
import com.toast.android.push.*
import kr.co.petdoc.petdoc.log.Logger

/**
 * Petdoc
 * Class: ToastAPI
 * Created by kimjoonsung on 2020/08/04.
 *
 * Description : Naver TOAST SDK 사용하는 함수 정리
 */
object ToastAPI {

    /**
     * TOAST SDK에서 제공하는 모든 상품(Push, IAP, Log & Crash등)은 하나의 동일한 사용자 아이디를 사용
     * 서비스 로그인 단계에서 사용자 아이디 설정, 토큰 등록 기능을 구현하는 것을 권장
     * 토큰 등록 후 사용자 아이디를 설정 또는 변경하면 토큰 정보를 갱신
     *
     * @param userId
     */
    fun onLogin(userId: String) {
        ToastSdk.setUserId(userId)
    }

    /**
     * ToastPush.registerToken() 메서드를 사용하여 Push 토큰을 TOAST Push 서버로 전송합니다. 이때 수신 동의 여부(ToastPushAgreement)를 파라미터로 전달합니다.
     * 최초 토큰 등록 시 사용자 아이디가 설정되어 있지 않으면, 단말기 식별자를 사용하여 등록합니다.
     * 토큰이 성공적으로 등록되면, Push 메시지를 수신할 수 있습니다.
     *
     * @param notification : 알림 메시지 수신 여부
     * @param advertisement : 홍보성 메시지 수신 여부
     * @param nightAdbertisement : 야간 홍보성 알림 메시지 수신 여부
     * @return
     */
    fun registerToken(
        context: Context,
        notification: Boolean,
        advertisement: Boolean,
        nightAdbertisement: Boolean
    ) {
        // 수신 동의 설정 객체 생성
        val agreement = ToastPushAgreement.newBuilder(notification)
            .setAllowAdvertisements(advertisement)
            .setAllowNightAdvertisements(nightAdbertisement)
            .build()

        // 토근 등록 및 수신 동의 설정
        ToastPush.registerToken(context, agreement, object : RegisterTokenCallback {
            override fun onRegister(result: PushResult, token: TokenInfo?) {
                if (result.isSuccess) {
                    // 토큰 등록 성공
                    Logger.d("token info : ${token?.token}")
                } else {
                    // 토큰 등록 실패
                    val code = result.code
                    val message = result.message
                    Logger.d("error code : $code, $message")
                }
            }
        })
    }

    /**
     * TOAST Push 서버에 등록된 토큰 정보를 조회합니다
     *
     * @param context
     */
    fun getTokenInfo(context: Context) {
        var token = ""
        ToastPush.queryTokenInfo(context, object : QueryTokenInfoCallback {
            override fun onQuery(result: PushResult, tokenInfo: TokenInfo?) {
                if (result.isSuccess) {
                    token = tokenInfo?.token.toString()
                    val agreement = tokenInfo?.agreement

                    Logger.d("token : $token, agreement : $agreement")
                } else {
                    val code = result.code
                    val message = result.message
                    Logger.d("error code : $code, $message")
                }
            }
        })
    }

    /**
     * TOAST Push 서버에 등록된 토큰을 해제합니다. 해제된 토큰은 메시지 발송 대상에서 제외됩니다.
     * 서비스 로그아웃 후에 메시지 수신을 원치 않으시면 토큰을 해제해야 합니다.
     * 토큰이 해제되어도 단말기 상에 알림 권한은 회수되지 않습니다.
     *
     * @param context
     */
    fun unregisterToken(context: Context) {
        ToastPush.unregisterToken(context, object : UnregisterTokenCallback {
            override fun onUnregister(result: PushResult, unregisteredToken: String?) {
                if (result.isSuccess) {
                    // 토큰 해제 성공
                } else {
                    val code = result.code
                    val message = result.message
                    Logger.d("error code : $code, $message")
                }
            }
        })
    }
}