package kr.co.petdoc.petdoc.repository.network

import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.domain.RecommendedGodoMallProductResponse
import kr.co.petdoc.petdoc.network.response.BannerListResponse
import kr.co.petdoc.petdoc.nicepay.NicePayApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GwApiService {

    @GET("api/banners/v2")
    suspend fun getBannerListWithCoroutine(
        @Query("post_type") value: String
    ): BannerListResponse

    @GET("api/shops")
    suspend fun getRecommendProductListWithCoroutine(
        @Query("page") page: Int
    ): RecommendedGodoMallProductResponse

    companion object {
        fun createGwApiService(): NicePayApiService {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5L, TimeUnit.SECONDS)
                .readTimeout(5L, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_GW_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(NicePayApiService::class.java)
        }
    }
}