package kr.co.petdoc.petdoc.repository.network

import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.domain.*
import kr.co.petdoc.petdoc.domain.HospitalListResponse
import kr.co.petdoc.petdoc.network.response.*
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.nicepay.NicePayCreditCardResponse
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.repository.network.form.InjectionForm
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface PetdocApiService {

    @GET("app/v2/banner")
    suspend fun getBannerListWithCoroutine(
        @Query("postType") value: String
    ): BannerListResponse

    /**
     * 병원 정보 가져오기
     * @param form
     * @return HospitalListResponse
     */
    @POST("pb/center/find")
    suspend fun postHospitalList(
        @Body form: HospitalListForm
    ): HospitalListResponse

    @GET("app/pet/select")
    suspend fun getPetInfoListWithCoroutine(
        @Query("userId") id: Int
    ): List<PetInfoItem>

    @GET("app/shop")
    suspend fun getRecommendProductListWithCoroutine(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): RecommendedGodoMallProductResponse

    @GET("/app/scanner/firmware")
    suspend fun getScannerVersion(
        @Query("deviceOs") deviceOs: List<String> = listOf("AOS")
    ): ScannerVersionResponse

    /**
     * 알러지 검사 결과 코멘트 조회
     */
    @GET("dna/inspection/allergy/{categoryName}/comment")
    suspend fun getAllergyComment(
        @Path("categoryName") categoryName: String
    ): AllergyCommentResponse

    /**
     * 혈액 검사 결과 코멘트 조회
     */
    @GET("dna/inspection/blood/{categoryName}/comment")
    suspend fun getBloodComment(
        @Path("categoryName") categoryName: String
    ): BloodCommentResponse

    /**
     * 병원 검색 추천 검색어 가져오기
     *
     * @return
     */
    @GET("pb/center/keyword")
    suspend fun getHospitalSearchKeyword(): HospitalKeywordResponse

    /**
     * 종합검사 결과정보 가져오기
     *
     * @param bookingId
     * @return
     */
    @GET("/dna/inspection/booking/{bookingId}/results")
    suspend fun getHealthCareResultList(
        @Path("bookingId") bookingId: Int
    ): HealthCareResultResponse

    /**
     * 마케팅 수신 동의 변경
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/users/marketing")
    fun putMarketingStatusWithCoroutine(
        @Body body: RequestBody
    ): Response<ResponseBody>

    /**
     *  Magazine List
     */
    @GET("app/ency")
    suspend fun getMagazineListWithCoroutine(
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): MagazineListRespone

    /**
     * 홈 인기 펫톡 가져오기
     *
     * @return
     */
    @GET("app/pettalk/main")
    suspend fun getHomePetTalkListWithCoroutine(): HomePetTalkListResponse

    /**
     * 홈 숏컷 리스트를 가져온다.
     *
     * @return
     */
    @GET("/app/mainShotCut")
    suspend fun getHomeShortCutWithCoroutine(): Response<ResponseBody>

    /**
     *  Profile info
     */
    @POST("app/user/searchUserWithAuth")
    suspend fun getProfileInfoWithCoroutine(): Response<ResponseBody>

    @GET(BuildConfig.BASE_GW_URL + "api/boards/users")
    suspend fun getMyPetTalkList(): MyPetTalkListResponse

    /**
     * 체온 리스트를 조회한다.
     *
     * @param petId
     * @param limit
     * @param offset
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/temperature")
    suspend fun getTemparatureForPet(
        @Path("petId") petId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): CareTemperatureResponse

    /**
     * 체온 기록을 삭제한다.
     *
     * @param petId
     * @param idList
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/temperature")
    suspend fun deleteTemperatureForPet(
        @Path("petId") petId: Int,
        @Query("idList") regDate: String
    ): Response<ResponseBody>

    /**
     * 온습도 리스트를 조회한다.
     *
     * @param petId
     * @param limit
     * @param offset
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/humiditys")
    suspend fun getHumidityForPet(
        @Path("petId") petId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): CareHumidityResponse

    /**
     * 온습도 기록 삭제한다.
     *
     * @param petId
     * @param idList
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/humidity")
    suspend fun deleteHumidityForPet(
        @Path("petId") petId: Int,
        @Query("idList") regDate: String
    ): Response<ResponseBody>

    /**
     * 귀 사진 리스트를 조회한다.
     *
     * @param usreId
     * @param petId
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{userId}/{petId}/file")
    suspend fun getEarImageForPet(
        @Path("userId") usreId: Int,
        @Path("petId") petId: Int,
        @Query("type") type: String
    ): CareEarImageResponse

    /**
     * 귀 사진을 삭제한다.
     *
     * @param usreId
     * @param petId
     * @param body
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{userId}/{petId}/file")
    suspend fun deleteEarImageForPet(
        @Path("userId") usreId: Int,
        @Path("petId") petId: Int,
        @Query("idList") regDate: String
    ): Response<ResponseBody>

    /**
     * 체온 투약 여부 상태변경 한다.
     *
     * @param petId
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/inject")
    suspend fun postInjectionForPet(
        @Path("petId") petId: Int,
        @Body form: List<InjectionForm>
    ): Response<ResponseBody>

    /**
     * 마이페이지 알림 리스트 가져오기
     *
     * @return
     */
    @GET("app/push/logs")
    suspend fun getMyAlarmList(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): MyAlarmListResponse

    /**
     * 펫체크어드밴스 판매 아이템 정보
     */
    @GET("payments/merchants/1/items/1")
    suspend fun getItemInfoForPetCheckAdvanced(): PetcheckAdvancedItemResponse

    /**
     * 주문 조회
     */
    @GET("payments/users/{petdocUserId}/orders/{orderId}")
    suspend fun getOrderDetail(
        @Path("petdocUserId") userId: String,
        @Path("orderId") orderId: String
    ): OrderDetailResponse

    /**
     * 주문 취소
     *
     * @param orderId
     * @param body
     * @return
     */
    @HTTP(method = "DELETE", path = "payments/users/{petdocUserId}/orders/{orderId}", hasBody = true)
    suspend fun cancelOrder(
        @Path("orderId") orderId: String,
        @Path("petdocUserId") userId: String,
        @Body body: RequestBody
    ): CommonResponse

    /**
     * 주문 내역 조회
     *
     * @param userId
     * @return
     */
    @GET("payments/users/{petdocUserId}/orders")
    suspend fun getOrderList(@Path("petdocUserId") userId: Int): OrderListResponse

    /**
     * 주문 정보 삭제
     *
     * @param userId
     * @param orderId
     * @return
     */
    @PUT("payments/users/{petdocUserId}/orders/{orderId}")
    suspend fun deleteOrderInfo(
        @Path("petdocUserId") userId: Int,
        @Path("orderId") orderId: Int
    ): CommonResponse

    /**
     * 상담 카테고리 정보를 가져온다.
     *
     * @return
     */
    @GET("app/category")
    suspend fun getChatCategory(): ChatCategoryResponse

    /**
     *  스캐너 온도 정보를 가져온다.
     */
    @GET("https://cdn.petdoc.co.kr/static/temperature_info.json")
    suspend fun getTemperatureInfo(): TemperatureResponse

    /**
     * 읽지 않은 푸쉬 조회한다.
     *
     * @return
     */
    @GET("app/push/logs/has-unread")
    suspend fun getPushUnRead(): CommonResponse

    /**
     * 나이스페이 신용카드 정보를 조회한다.
     *
     * @return
     */
    @GET("payments/merchants/{merchantId}/cards")
    suspend fun getCreaditCardInfo(
        @Path("merchantId") merchantId: Int
    ): NicePayResponse

    /**
     *  상담 내역 리스트
     */
    @GET(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms")
    suspend fun getChatListWithCoroutine(): ChatListResponse
}