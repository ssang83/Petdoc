package kr.co.petdoc.petdoc.nicepay

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface NicePayApiService {
    @Headers("Content-Type: application/json")
    @POST("mi/api/")
    suspend fun fetchCreditCardInfo(@Body request: RequestBody): NicePayCreditCardResponse

    companion object {
        private const val BASE_URL = "https://data.nicepay.co.kr/"
        fun createNicePayApiService(): NicePayApiService {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(NicePayApiService::class.java)
        }
    }
}