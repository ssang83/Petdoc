package kr.co.petdoc.petdoc.repository.network

import android.content.Context
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.koin.core.context.GlobalContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

class PetdocApiFactory {
    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val HTTP_TIMEOUT_SECOND = 30L
        private const val HTTP_DISK_CACHE_SIZE = 30 * 1024 * 1024 // 30 MB

        fun createApiService(): PetdocApiService {
            val client = createOkHttpClient()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(PetdocApiService::class.java)
        }

        private fun createOkHttpClient(): OkHttpClient {
            val context = PetdocApplication.application.baseContext
            val httpLoggingInterceptor = HttpLoggingInterceptor { message ->
                if (message.isNotEmpty()) {
                    if (!message.contains("Content-Disposition: form-data")) {
                        Logger.l(message)
                    } else {
                        var max = message.length
                        if (max > 500) max = 500
                        Logger.i(message.substring(0, max))
                    }
                }
            }.apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }

            val cacheDir = File(context.cacheDir, "http")
            val cache = Cache(cacheDir, HTTP_DISK_CACHE_SIZE.toLong())

            return OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(PetdocInterceptor())
                .authenticator(PetdocAuthenticator())
                .cache(cache)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(HTTP_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT_SECOND, TimeUnit.SECONDS)
                .build()
        }

        private class PetdocInterceptor() : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val context = PetdocApplication.application.baseContext
                val original = chain.request()

                val accessToken = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_ACCESS_TOKEN, "")
                val refreshToken = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_REFRESH_TOKEN, "")
                val oldToken = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_TOKEN, "")
                val userAgent = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_APP_SETUP_UUID_KEY, "")
                val oldUserid = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "")
                val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")
                val authId = StorageUtils.loadIntValueFromPreference(context, AppConstants.PREF_KEY_AUTH_ID)

                val requestBuilder = original.newBuilder()
                    //.header("content-type:", "text/html")          // 인터셉트 되면서 강제로 콘텐츠 헤더가 변경되어버려서, 에러가 발생함. 기본 설정값을 따르다가 retrofit2에서 설정하기
                    .header("charset", "UTF-8")
                    .method(original.method(), original.body())

                Logger.d("accessToken : $accessToken, refreshToken : $refreshToken, oldToken : $oldToken")
                Logger.d("user_agent : $userAgent, oldUserId : $oldUserid, userId: $userId, authId : $authId")

                // 비 로그인 시 헤더에 데이터를 싫어서 보내면 값이 없어도 401 에러가 발생해서 로그인 한 경우에만 헤더에 추가해서 전송
                if(StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    requestBuilder
                        .header("accessToken", accessToken)
                        .header("refreshToken", refreshToken)
                        .header("Authorization", "token ${oldToken}")
                        .header("user_agent", userAgent)
                        .header("oldUserid", oldUserid)
                        .header("userId", userId)
                        .header("authId", if(authId != -1 || authId != 0) authId.toString() else "")
                }

                val response = chain.proceed(requestBuilder.build())
                updateTokenIfNeeded(context, response, accessToken, refreshToken)
                return response
            }

            /**
             * 서버의 AccessToken 또는 refreshToken 이 갱신된 경우
             * response 헤더에 갱신된 AccessToken 과 refreshToken 이 포함되어 내려온다.
             * 기존 accessToken 과 refreshToken 과 다르면 서버에서 갱신된 정보로 업데이트한다.
             * accessToken 과 refreshToken 둘다 다른 경우 code 401 과 errorBody값이 {"code":412} 로 내려오며
             * 이때는 다시 로그인이 필요함.
             */
            private fun updateTokenIfNeeded(
                context: Context,
                response: Response,
                accessToken: String,
                refreshToken: String
            ) {
                val accessTokenInHeader = response.header("AccessToken")
                if (accessTokenInHeader != null && accessTokenInHeader != accessToken) {
                    StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_ACCESS_TOKEN, accessTokenInHeader)
                }
                val refreshTokenInHeader = response.header("RefreshToken")
                if (refreshTokenInHeader != null && refreshTokenInHeader != refreshToken) {
                    StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_REFRESH_TOKEN, refreshTokenInHeader)
                }
            }
        }

        class PetdocAuthenticator : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                response.body()?.let {
                    val body = getBody(it)
                    if (body?.contains("\"code\":412") == true) {
                        val context = PetdocApplication.application.baseContext
                        if (StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                            StorageUtils.writeBooleanValueInPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS, false)
                            StorageUtils.writeBooleanValueInPreference(context, AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, false)
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_ACCESS_TOKEN, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_REFRESH_TOKEN, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_OLD_TOKEN, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_USER_ID, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_USER_NICK_NAME, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_USER_NAME, "")
                            StorageUtils.writeValueInPreference(context, AppConstants.PREF_KEY_ID_GODO, "")
                            StorageUtils.writeIntValueInPreference(context, AppConstants.PREF_KEY_USER_LOGIN_TYPE, 0)
                            StorageUtils.writeIntValueInPreference(context, AppConstants.PREF_KEY_AUTH_ID, 0)

                            val appCache by lazy { GlobalContext.get().koin.get<AppCache>() }
                            appCache.clear()
                            EventBus.getDefault().post(LogoutEvent())
                        }
                    }
                }
                return null
            }

            private fun getBody(responseBody: ResponseBody): String? {
                var body = ""
                val source = responseBody.source()
                try {
                    source.request(Long.MAX_VALUE) // Buffer the entire body.
                    val buffer = source.buffer()
                    var charset = Charset.forName("UTF-8")
                    val contentType = responseBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(Charset.forName("UTF-8"))
                    }
                    if (responseBody.contentLength() != 0L) {
                        body = buffer.clone().readString(charset)
                    }
                } catch (e: java.lang.Exception) {
                    Logger.p(e)
                }
                return body
            }
        }
    }
}