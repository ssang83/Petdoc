package kr.co.petdoc.petdoc.repository.local.preference

import android.content.SharedPreferences

class PetDocPreferences(
    prefs: SharedPreferences
) : BasePreferences(prefs) {
    companion object {
        const val PREF_KEY_FIRST_START_USER_FLAG = "kr.co.petdoc.petdoc.PREF_KEY_FIRST_START_USER_FLAG"
        const val PREF_KEY_LOGIN_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_LOGIN_STATUS"
        const val PREF_KEY_APP_SETUP_UUID_KEY = "kr.co.petdoc.petdoc.PREF_APP_UUID_KEY"
        const val PREF_KEY_LAST_SPLASH_IMAGE_URL = "kr.co.petdoc.petdoc.PREF_LAST_SPLASH_IMAGE"
        const val PREF_KEY_ACCESS_TOKEN = "kr.co.petdoc.petdoc.PREF_KEY_ACCESS_TOKEN"
        const val PREF_KEY_REFRESH_TOKEN = "kr.co.petdoc.petdoc.PREF_KEY_REFRESH_TOKEN"
        const val PREF_KEY_OLD_TOKEN = "kr.co.petdoc.petdoc.PREF_KEY_OLD_TOKEN"
        const val PREF_KEY_OLD_USER_ID = "kr.co.petdoc.petdoc.PREF_KEY_OLD_USER_ID"
        const val PREF_KEY_USER_ID = "kr.co.petdoc.petdoc.PREF_KEY_USER_ID"
        const val PREF_KEY_PETDOC_SCAN_NAME = "kr.co.petdoc.petdoc.PREF_KEY_PETDOC_SCAN_NAME"
        const val PREF_KEY_PETDOC_SCAN_PASSWORD = "kr.co.petdoc.petdoc.PREF_KEY_PETDOC_SCAN_PASSWORD"
        const val PREF_KEY_PETDOC_SCAN_CONNECT_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS"
        const val PREF_KEY_USER_NICK_NAME = "kr.co.petdoc.petdoc.PREF_KEY_USER_NICK_NAME"
        const val PREF_KEY_USER_NAME = "kr.co.petdoc.petdoc.PREF_KEY_USER_NAME"
        const val PREF_KEY_ID_GODO = "kr.co.petdoc.petdoc.PREF_KEY_ID_GODO"
        const val PREF_KEY_CHAT_NOTI_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_CHAT_NOTI_STATUS"
        const val PREF_KEY_COMMENT_NOTI_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_COMMENT_NOTI_STATUS"
        const val PREF_KEY_GRADE_NOTI_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_GRADE_NOTI_STATUS"
        const val PREF_KEY_EMAIL_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_EMAIL_STATUS"
        const val PREF_KEY_SMS_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_SMS_STATUS"
        const val PREF_KEY_CLINIC_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_CLINIC_STATUS"
        const val PREF_KEY_REGISTER_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_REGISTER_STATUS"
        const val PREF_KEY_BOOKING_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_BOOKING_STATUS"
        const val PREF_KEY_BEAUTY_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_BEAUTY_STATUS"
        const val PREF_KEY_HOTEL_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_HOTEL_STATUS"
        const val PREF_KEY_ALLDAY_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_ALLDAY_STATUS"
        const val PREF_KEY_PARKING_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_PARKING_STATUS"
        const val PREF_KEY_CAFE_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_CAFE_STATUS"
        const val PREF_KEY_KINERGARDEN_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_KINERGARDEN_STATUS"
        const val PREF_KEY_CLINIC_FACILITY_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_CLINIC_FACILITY_STATUS"
        const val PREF_KEY_CLINIC_PET_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_CLINIC_PET_STATUS"
        const val PREF_KEY_USER_LOGIN_TYPE = "kr.co.petdoc.petdoc.PREF_KEY_USER_LOGIN_TYPE"
        const val PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE = "kr.co.petdoc.petdoc.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE"
        const val PREF_KEY_MOBILE_CONFIRM = "kr.co.petdoc.petdoc.PREF_KEY_MOBILE_CONFIRM"
        const val PREF_KEY_FCM_PUSH_TOKEN = "kr.co.petdoc.petdoc.PREF_KEY_FCM_PUSH_TOKEN"
        const val PREF_KEY_MARKETING_POPUP_NO_SHOW = "kr.co.petdoc.petdoc.PREF_KEY_MARKETING_POPUP_NO_SHOW"
        const val PREF_KEY_SCANNER_FIRMWARE_VER = "kr.co.petdoc.petdoc.PREF_KEY_SCANNER_FIRMWARE_VER"
        const val PREF_KEY_SCANNER_ADAPTER_STATUS = "kr.co.petdoc.petdoc.PREF_KEY_SCANNER_ADAPTER_STATUS"
        const val PREF_KEY_SCANNER_LAST_VERSION_CHECK_DATE = "kr.co.petdoc.petdoc.PREF_KEY_SCANNER_LAST_VERSION_CHECK_DATE"
        const val PREF_KEY_AUTH_ID = "kr.co.petdoc.petdoc.PREF_KEY_AUTH_ID"
        const val PREF_KEY_CHAT_USER_ID = "kr.co.petdoc.petdoc.PREF_KEY_CHAT_USER_ID"
        const val PREF_KEY_CHAT_USER_NICK_NAME = "kr.co.petdoc.petdoc.PREF_KEY_CHAT_USER_NICK_NAME"
        const val PREF_KEY_PET_CHECK_RESULT_IS_SHOW = "kr.co.petdoc.petdoc.PREF_KEY_PET_CHECK_RESULT_IS_SHOW"
    }

}