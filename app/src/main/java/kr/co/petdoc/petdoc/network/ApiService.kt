package kr.co.petdoc.petdoc.network

import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.domain.*
import kr.co.petdoc.petdoc.network.response.*
import kr.co.petdoc.petdoc.network.response.HospitalListResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Petdoc
 * Class: ApiService
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
interface ApiService {

    // Test code
    @GET("{endPoint}")
    fun getImageList(
        @Path("endPoint") endPoint: String
    ): Call<ImageListResponse>

    /**
     * Test code
     * Upload file to server
     *
     * @param firstName
     * @param lastName
     * @param image0
     * @return
     */
    @Multipart
    @POST("{endPoint}")
    fun uploadFile(
        @Path("endPoint") endPoint: String,
        @Query("first_name") firstName: String,
        @Query("last_name") lastName: String,
        @Part image0: MultipartBody.Part
    ): Call<AbstractApiResponse>


    // add by sungmin on 2020/04/02
    @GET(BuildConfig.BASE_GW_URL + "api/screens")
    fun getSplashImage(): Call<SplashImageResponse>

    /**
     *  Banner List
     */
    @GET(BuildConfig.BASE_GW_URL + "api/banners/v2")
    fun getBannerList(
        @Query("post_type") value: String
    ): Call<BannerListResponse>

    // add by sungmin on 2020/04/17
    @GET(BuildConfig.BASE_GW_URL + "api/banners/v2")
    fun getLifeBannerList(
        @Query("post_type") value: String
    ): Call<BannerListResponse>

    @GET(BuildConfig.BASE_GW_URL + "api/boards/main")
    fun getFamousPetTalk(): Call<PetTalkResponse>


    // ---------------------------------------------------------------------------------------
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk")
    fun getPetTalkList(
        @Query("type") type: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<ResponseBody>

    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk")
    fun getPetTalkListAll(
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<ResponseBody>
    // ---------------------------------------------------------------------------------------

    /**
     *  Magazine List
     */
    @GET("app/ency")
    fun getMagazineList(
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<MagazineListRespone>


    /**
     *  Profile info
     */
    @POST("app/user/searchUserWithAuth")
    fun getProfileInfo(): Call<ResponseBody>

    /**
     *  Pet Info
     */
    @GET("app/pet/select")
    fun getPetInfoList(
        @Query("userId") id: Int
    ): Call<ResponseBody>

    @GET("app/pet/select")
    suspend fun getPetInfoListWithCoroutine(
        @Query("userId") id: Int
    ): List<PetInfoItem>

    /**
     *  User Grade
     */
    @GET(BuildConfig.BASE_GW_URL + "api/users/check-first-connect/")
    fun getUserGrade(): Call<ResponseBody>

    /**
     * Email Login
     */
    @POST("app/user/loginUserWithAuth")
    fun emailLogin(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * Splash Image
     */
    @GET(BuildConfig.BASE_GW_URL + "api/screens/")
    fun loadSplash(): Call<ResponseBody>

    /**
     *  Banner detail
     */
    @GET(BuildConfig.BASE_GW_URL + "api/banners/{id}")
    fun getBanner(
        @Path("id") id: Int
    ): Call<BannerDetailResponse>

    /**
     *  Shop List
     */
    @GET(BuildConfig.BASE_GW_URL + "api/shops")
    fun getShoppingList(
        @Query("page") page: Int
    ): Call<ShoppingListResponse>

    /**
     *  Temperature Info
     */
    @GET("https://cdn.petdoc.co.kr/static/temperature_info.json")
    fun getTemperatureInfo(
    ): Call<ResponseBody>

    /**
     *  상담 내역 리스트
     */
    @GET(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms")
    fun getChatList(): Call<ChatListResponse>

    /**
     *  상담 내역 삭제
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms/{id}")
    fun deleteChatItem(
        @Path("id") pk: Int
    ): Call<ResponseBody>

    /**
     *  상담내역 상세
     */
    @GET(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms/{id}")
    fun getChatDetail(
        @Path("id") pk: Int
    ): Call<ChatDetailResponse>

    /**
     *  상담 분류 카테고리를 가져온다.
     */
    @GET(BuildConfig.BASE_GW_URL + "api/category/")
    fun getChatCategoryList(): Call<ChatCategoryListResponse>

    /**
     *  관련 기존 상담 리스트 가져오기
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/chat")
    fun getLegacyChatList(
        @Query("kind") petKind: String,
        @Query("categoryId") categoryId: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<ResponseBody>

    /**
     *  관련 기존 상담 전체 갯수
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/chat/count")
    fun getLegacyChatTotalCount(
        @Query("kind") petKind: String,
        @Query("categoryId") categoryId: String
    ): Call<ResponseBody>

    /**
     *  관련 반려백과 리스트 가져오기
     */
    @GET("app/ency")
    fun getLegacyMagazineList(
        @Query("categoryId") categoryId: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<MagazineListRespone>

    /**
     *  반려백과 전체 갯수
     */
    @GET("app/ency/count")
    fun getMagazineTotalCount(
        @Query("categoryId") categoryId: String
    ): Call<ResponseBody>

    /**
     *  상담종료 추천제품 리스트
     */
    @GET(BuildConfig.BASE_GW_URL + "api/shops")
    fun getRecommendProductList(): Call<RecommendProductListResponse>

    @GET(BuildConfig.BASE_GW_URL + "api/shops")
    suspend fun getRecommendProductListWithCoroutine(
        @Query("page") page: Int
    ): RecommendedGodoMallProductResponse


    /**
     *  상담종료 주변 제휴병원 리스트
     */
    @GET(BuildConfig.BASE_GW_URL + "api/hospitals")
    fun getHospitalList(
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Call<RelatedHospitalListResponse>

    /**
     * 채팅방 생성
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms")
    fun postChattingRooms(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 상담 검색 탑 키워드 가져오기
     *
     */
    @GET(BuildConfig.BASE_GW_URL + "api/chattings/keyword")
    fun getChatKeywordList(): Call<ChatKeywordListResponse>

    /**
     *  상담 검색 내역 상세
     */
    @GET(BuildConfig.BASE_GW_URL + "api/chattings/search/rooms/{id}")
    fun getChatSearchDetail(
        @Path("id") pk: Int
    ): Call<ChatDetailResponse>

    /**
     *  상담 검색 결과 리스트
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/chat/search")
    fun getChatSearchResultList(
        @Query("keyword") keyword: String,
        @Query("kind") petKind: String,
        @Query("categoryId") categoryId: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<ChatSearchResultListResponse>

    /**
     * 상담 이미지 등록
     *
     * @param roomId
     * @param image
     * @return
     */
    @Multipart
    @POST(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms/{room_id}/messages")
    fun postChatImage(
        @Path("room_id") roomId: Int,
        @Part image: MultipartBody.Part
    ): Call<ResponseBody>

    /**
     * 상담 메시지 등록
     *
     * @param roomId
     * @param image
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms/{room_id}/messages")
    fun postChatMessage(
        @Path("room_id") roomId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 상담종료 요청
     *
     * @param roomId
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/chattings/protector/rooms/{id}")
    fun putChatQuit(
        @Path("id") roomId: Int
    ): Call<ResponseBody>

    /**
     * 상담종료 리뷰 등록
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/chattings/rooms/{id}/review")
    fun putChatQuitReview(
        @Path("id") roomId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 푸쉬토큰 등록
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/users/device-token")
    fun putRegisterPush(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 펫톡 상세화면 가져오기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk/{id}")
    fun getPetTalkDetail(
        @Path("id") id: Int
    ): Call<ResponseBody>

    /**
     * 펫톡 상세 조회수 증가
     *
     * @param id
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk/{pettalkId}/viewcount")
    fun postPetTalkViewCount(
        @Path("pettalkId") id: Int
    ): Call<ResponseBody>

    /**
     * 펫톡 댓글 가져오기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk/{pettalkId}/comment?limit=10")
    fun getPetTalkReplyList(
        @Path("pettalkId") id: Int
    ): Call<ResponseBody>

    /**
     * 펫톡 댓글 가져오기(paging)
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/pettalk/{pettalkId}/comment?limit=10")
    fun getPetTalkReplyPagingList(
        @Path("pettalkId") id: Int,
        @Query("baseId") baseId: Int
    ): Call<ResponseBody>

    /**
     * 배너 및 펫톡 댓글 등록하기
     *
     * @param type : board(펫톡), banner(배너)
     * @param idx
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/{type}/{idx}/comments")
    fun postReplyAdd(
        @Path("type") type: String,
        @Path("idx") idx: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 배너 및 펫톡 댓글 삭제
     *
     * @param type : board(펫톡), banner(배너)
     * @param idx
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/{type}/comments/{index}")
    fun deleteReply(
        @Path("type") type: String,
        @Path("index") idx: Int
    ): Call<ResponseBody>

    /**
     * 라이프 반려백과 더보기 가져오기
     *
     * @param categoryId
     * @param order
     * @param limit
     * @param offset
     * @return
     */
    @GET("app/ency")
    fun getLifeMagazine(
        @Query("categoryId") categoryId: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<MagazineListRespone>

    /**
     * 라이프 반려백과 검색 리스트
     *
     * @param keyword
     * @param categoryId
     * @param order
     * @param limit
     * @param offset
     * @return
     */
    @GET("app/ency/search")
    fun getLifeMagazineSearch(
        @Query("keyword") keyword: String,
        @Query("categoryId") categoryId: String,
        @Query("order") order: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Call<LifeMagazineSearchListResponse>

    /**
     * 채팅방 추천
     *
     * @param roomId
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/chattings/rooms/{room_id}/recommend")
    fun postChatRoomRecommend(
        @Path("room_id") roomId: Int
    ): Call<ResponseBody>

    /**
     * 채팅방 추천 취소
     *
     * @param roomId
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/chattings/rooms/{room_id}/recommend")
    fun deleteChatRoomRecommend(
        @Path("room_id") roomId: Int
    ): Call<ResponseBody>

    /**
     * 펫톡 게시물 업로드
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/boards")
    fun postPetTalkUpload(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 펫톡 게시물 삭제
     *
     * @param body
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/boards/{idx}")
    fun deletePetTalk(
        @Path("idx") id: Int
    ): Call<ResponseBody>

    /**
     * 펫톡 게시물 수정
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/boards/{idx}")
    fun putPetTalkUpload(
        @Path("idx") id: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 매거진 상세
     *
     * @param id
     * @return
     */
    @GET("app/ency/{magazineId}")
    fun getMagazineDetail(
        @Path("magazineId") id: Int
    ): Call<MagazineDetailResponse>

    /**
     * 매거진 상세 추천백과 리스트
     *
     * @param id
     * @return
     */
    @GET("app/ency/{magazineId}/associate?count=20")
    fun getMagazineDetailSubList(
        @Path("magazineId") id: Int
    ): Call<MagazineListRespone>

    /**
     * 매거진 상세 조회수
     *
     * @param id
     * @return
     */
    @POST("app/ency/{magazineId}/viewcount")
    fun postMagazineViewCount(
        @Path("magazineId") id: Int
    ): Call<ResponseBody>

    /**
     * 매거진 북마크, 좋아요 개수
     *
     * @param int
     * @return
     */
    @GET("app/ency/{magazineId}/count")
    fun getMagazineLikeCount(
        @Path("magazineId") int: Int
    ): Call<ResponseBody>

    /**
     * 매거진 북마크, 좋아요 여부
     *
     * @param id
     * @param userId
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/ency/{magazineId}/added/{userId}")
    fun getMagazineLikeStatus(
        @Path("magazineId") id: Int,
        @Path("userId") userId: Int
    ): Call<ResponseBody>

    /**
     * 매거진 북마크
     *
     * @param id
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/magazines/{parent_pk}/bookmarks")
    fun postMagazineBookmark(
        @Path("parent_pk") id: Int
    ): Call<ResponseBody>

    /**
     * 매거진 북마크 해제
     *
     * @param id
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/magazines/{parent_pk}/bookmarks")
    fun deleteMagazineBookmark(
        @Path("parent_pk") id: Int
    ): Call<ResponseBody>

    /**
     * 매거진 좋아요
     *
     * @param id
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/magazines/{parent_pk}/likes")
    fun postMagazineLike(
        @Path("parent_pk") id: Int
    ): Call<ResponseBody>

    /**
     * 매거진 좋아요 해제
     *
     * @param id
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/magazines/{parent_pk}/likes")
    fun deleteMagazineLike(
        @Path("parent_pk") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 정보 가져오기
     *
     * @param body
     * @return
     */
    @POST("pb/center/find")
    fun postHospitalList(
        @Body body: RequestBody
    ): Call<HospitalListResponse>

    /**
     * 병원 상세 정보 가져오기
     *
     * @param body
     * @return
     */
    @GET("pb/center/{centerId}")
    fun getHospitalDetail(
        @Path("centerId") id: Int
    ): Call<HospitalDetailResponse>

    /**
     * 병원 북마크 설정
     *
     * @param id
     * @return
     */
    @POST("pb/center/bookmark/{centerId}")
    fun postHospitalBookmark(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 북마크 해제
     *
     * @param id
     * @return
     */
    @DELETE("pb/center/bookmark/{centerId}")
    fun deleteHospitalBookmark(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 북마크 상태 가져오기
     *
     * @param id
     * @return
     */
    @GET("pb/center/bookmark/{centerId}")
    fun getHospitalBookmark(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 검색 추천 검색어 가져오기
     *
     * @return
     */
    @GET("pb/center/keyword")
    fun getHospitalSearchKeyword(): Call<HospitalKeywordResponse>

    /**
     * 병원 접수 정보 가져오기
     *
     * @return
     */
    @GET("pb/clinicroom/{centerId}/register")
    fun getHospitalRegisterInfo(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 접수하기
     *
     * @param id
     * @param roomId
     * @param body
     * @return
     */
    @POST("pb/clinicroom/{centerId}/{clinicRoomId}/register")
    fun postClinicRegister(
        @Path("centerId") id: Int,
        @Path("clinicRoomId") roomId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 병원 예약 정보 가져오기
     *
     * @param id
     * @return
     */
    @GET("pb/center/{centerId}/booking")
    fun getHospitalReservationInfo(
        @Path("centerId") id: Int
    ): Call<HospitalReservationInfoResponse>

    /**
     * 병원 예약 진료 정보 가져오기
     *
     * @param id
     * @return
     */
    @GET("pb/clinicroom/{centerId}/booking")
    fun getHospitalBookingClinicInfo(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 병원 예약 미용정보 가져오기
     *
     * @param id
     * @return
     */
    @GET("pb/beauty/{centerId}/booking")
    fun getHospitalBookingBeautyInfo(
        @Path("centerId") id: Int
    ): Call<ResponseBody>

    /**
     * 해당 날짜 예약 타임테이블 가져오기
     *
     * @param id
     * @param date
     * @return
     */
    @GET("pb/clinicroom/{centerId}/booking/slot")
    fun getTimeTableList(
        @Path("centerId") id: Int,
        @Query("checkDate") date: String
    ): Call<ResponseBody>

    /**
     * 병원 진료 예약하기
     *
     * @param id
     * @param roomId
     * @param body
     * @return
     */
    @POST("pb/clinicroom/{centerId}/{clinicRoomId}/booking")
    fun postClinicReservation(
        @Path("centerId") id: Int,
        @Path("clinicRoomId") roomId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 로그아웃
     *
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/users/logout")
    fun postLogout(): Call<ResponseBody>

    /**
     * 케어 기록 리스트 가져오기
     *
     * @param petId
     * @param date
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/care")
    fun getCareRecordList(
        @Query("pet_id") petId: Int,
        @Query("check_date") date: String
    ): Call<CareRecordListResponse>

    /**
     * 케어기록 생성
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/care")
    fun postCareRecordUpload(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 케어기록 삭제
     *
     * @param id
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "api/care/{id}")
    fun deleteCareRecord(
        @Path("id") id: Int
    ): Call<ResponseBody>

    /**
     * 케어 기록 전체 보기 리스트 가져오기
     *
     * @param petId
     * @param date
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{idx}/history")
    fun getCareHistoryList(
        @Path("idx") petId: Int,
        @Query("edate") date: String
    ): Call<CareHistoryListRespone>

    /**
     * 체온 등록
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/temperature")
    fun postBodyTemperature(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 온습도 등록
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/humidity")
    fun postHumidity(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 귀 사진 업로드
     *
     * @param userId
     * @param petId
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{userId}/{petId}/upload")
    fun postEarImage(
        @Path("userId") userId: Int,
        @Path("petId") petId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 체온 투약 여부 상태 변경
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/inject")
    fun postTemperatureInject(
        @Path("petId") petId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 체온 삭제
     *
     * @param petId
     * @param body
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/temperature")
    fun deleteTemperatureRecord(
        @Path("petId") petId: Int,
        @Query("idList") regDate: String
    ): Call<ResponseBody>

    /**
     * 온습도 삭제
     *
     * @param petId
     * @param body
     * @return
     */
    @DELETE(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/humidity")
    fun deleteHumidityRecord(
        @Path("petId") petId: Int,
        @Query("idList") regDate: String
    ): Call<ResponseBody>

    /**
     * 체온 기록 리스트
     *
     * @param petId
     * @param offset
     * @param limit
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/temperature")
    fun getBodyTemperatureRecordList(
        @Path("petId") petId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Call<BodyTemperatureRecordListResponse>

    /**
     * 온습도 기록 리스트
     *
     * @param petId
     * @param offset
     * @param limit
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/scanner/{petId}/humidity")
    fun getHumidityRecordList(
        @Path("petId") petId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Call<HumidityRecordListResponse>

    /**
     * 병원 예약/접수 리스트 가져오기
     *
     * @param type : register(접수), booking(예약)
     * @return
     */
    @GET("pb/history/register")
    fun getMyHospitalHistoryRegisterList(): Call<RegisterHistoryListResponse>

    @GET("pb/history/booking")
    fun getMyHospitalHistoryBookingList(): Call<BookingHistoryListResponse>

    /**
     * 병원 접수/예약 자세한 정보 가져오기
     *
     * @param facilityType
     * @param bookingId
     * @return
     */
    @GET("pb/history/{facilityType}/{bookingId}")
    fun getMyHospitalDetail(
        @Path("facilityType") facilityType: String,
        @Path("bookingId") bookingId: Int
    ): Call<HospitalHistoryResponse>

    /**
     * 병원 북마크 리스트 가져오기
     *
     * @param latitude
     * @param longitude
     * @return
     */
    @GET("pb/center/bookmark")
    fun getHospitalBookmarkList(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<HospitalBookmarkListResponse>

    /**
     * 매거진 북마크 리스트 가져오기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/magazines/user/bookmarks")
    fun getMagazineBookmarkList(): Call<MagazineBookmarkListResponse>

    /**
     * 나의 펫톡 게시글 가져오기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/boards/users")
    fun getMyPetTalkList(): Call<MyPetTalkListResponse>

    /**
     * 공지사항 가져오기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/notice")
    fun getNoticeList(): Call<NoticeListResponse>

    /**
     * 1:1 문의 리스트
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/questions")
    fun getQuestionList(): Call<QuestionsListResponse>

    /**
     * 1:1 문의하기
     *
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/questions")
    fun postQuestions(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * Push 알림 수신 동의 변경
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/users/notification")
    fun putNotiStatus(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 마케팅 수신 동의 변경
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/users/marketing")
    fun putMarketingStatus(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 사용자 회원 탈퇴
     *
     * @param body
     * @return
     */
    @POST("/app/user/signOutUser")
    fun deleteSignOut(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 마이페이지 사용자 정보 조회하기
     *
     * @return
     */
    @GET(BuildConfig.BASE_GW_URL + "api/users/profile")
    fun getMyUesrProfile(): Call<MyUserProfileResponse>

    /**
     * 사용자 정보 수정
     *
     * @param body
     * @return
     */
    @POST("app/user/update/form")
    fun postUserProfile(
        @Body body: RequestBody
    ): Call<MyUserProfileResponse>

    @POST("app/user/update")
    fun postPushToken(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 이메일 인증번호 요청
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/users/verify/email")
    fun postEmailVerify(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 휴대폰 인증번호 요청
     *
     * @param body
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/users/verify/phone")
    fun postPhoneVerify(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 펫 이름 등록(사진 없는 경우)
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddName(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 펫 이름 등록(사진 있는 경우)
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert/form")
    fun postPetAddNameWithPicture(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 종류 가져오기
     *
     * @return
     */
    @GET("app/pet/kind")
    fun getPetKindList(): Call<ResponseBody>

    /**
     * 품종 검색 결과 가져오기
     *
     * @param kind
     * @param name
     * @return
     */
    @GET("app/pet/species")
    fun getPetSepciesList(
        @Query("kind") kind: String,
        @Query("name") name: String
    ): Call<ResponseBody>

    /**
     *  반려동물 종류 등록
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddKind(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 품종 등록
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddSpecies(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 나이 등록
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddBirth(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 성별 등록
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddGender(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 백신 접종여부 등록
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetAddVaccine(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 반려동물 삭제
     *
     * @param body
     * @return
     */
    @POST("app/pet/insert")
    fun postPetDelete(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 주재료 및 알러지 재료 가져오기
     *
     * @return
     */
    @GET("app/cmFood/select")
    fun getMatchFoodList(): Call<ResponseBody>

    /**
     * 맞춤식 진단 받은 결과 가져오기
     *
     * @param petId
     * @return
     */
    @GET("app/cmResult/select")
    fun getMatchResult(
        @Query("petId") petId: Int
    ): Call<ResponseBody>

    /**
     * 진단 받은 결과중 품종 정보 가져오기
     *
     * @param speciesId
     * @return
     */
    @GET("app/pet/species/{speciesId}")
    fun getDiagnosisSpecies(
        @Path("speciesId") speciesId: Int
    ): Call<ResponseBody>

    /**
     * 진단 받은 커스터밀 정보
     *
     * @param petId
     * @return
     */
    @GET("app/cmInfo/select")
    fun getCustomealInfo(
        @Query("petId") petId: Int
    ): Call<ResponseBody>

    /**
     * 진단 받은 사료 정보
     *
     * @param feedId
     * @return
     */
    @GET("app/cmProdFood/{feedId}")
    fun getFeedInfo(
        @Path("feedId") feedId: Int
    ): Call<ResponseBody>

    /**
     * 진단받은 영양겔 정보
     *
     * @param pnId
     * @return
     */
    @GET("app/cmProdNutr/{pnId}")
    fun getNutritionInfo(
        @Path("pnId") pnId: Int
    ): Call<ResponseBody>

    /**
     * 예상진료비 카테고리 가져오기
     *
     * @param petKind
     * @return
     */
    @GET("app/price/clinic/categories")
    fun getClinicPriceCategory(
        @Query("type") petKind: String
    ): Call<PriceCategoryListResponse>

    /**
     * 예상 진료비 결과 값 가져오기
     *
     * @param kind
     * @param gender
     * @param age
     * @param weight
     * @param categoryId
     * @return
     */
    @GET("app/price/clinic/calculate")
    fun getClinicPriceCalculate(
        @Query("type") kind: String,
        @Query("gender") gender: String,
        @Query("age") age: Int,
        @Query("weight") weight: String,
        @Query("category_id") categoryId: Int
    ): Call<PriceCalcuateResponse>

    /**
     * 홈 인기 펫톡 가져오기
     *
     * @return
     */
    @GET("app/pettalk/main")
    fun getHomePetTalkList(): Call<HomePetTalkListResponse>

    /**
     * 사용자 포인트 가져오기
     *
     * @param userId
     * @return
     */
    @GET("app/user/selectUserPoint")
    fun getUserPoint(
        @Query("userId") userId: Int
    ): Call<UserPointResponse>

    /**
     * 사용자 포인트 내역 가져오기
     *
     * @param userId
     * @return
     */
    @GET("app/user/selectUserPointLog")
    fun getUserPointLogList(
        @Query("userId") userId: Int
    ): Call<UserPointLogListResponse>

    /**
     * 병원 예약 기능 요청하기
     *
     * @param centerId
     * @return
     */
    @POST("pb/center/{centerId}/require/booking")
    fun postHospitalRequestBooking(
        @Path("centerId") centerId: Int
    ): Call<ResponseBody>

    /**
     * 병원 정보 수정 요청하기
     *
     * @param centerId
     * @param body
     * @return
     */
    @POST("pb/center/{centerId}/require/fix")
    fun postHospitalInfoEdit(
        @Path("centerId") centerId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 맞춤형 사료 스텝별 등록하기
     *
     * @param body
     * @return
     */
    @POST("app/cmInfo/insert")
    fun postMatchFood(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 정밀진단 질문 가져오기
     *
     * @param body
     * @return
     */
    @GET("app/cmFoodResearch/{petId}/foodResearchAll/{cminfoId}")
    fun getDiagnosisDetailList(
        @Path("petId") petId: Int,
        @Path("cminfoId") cmInfoId: Int
    ): Call<DetailDiagnosisListResponse>

    /**
     * 정밀진단 등록하기
     *
     * @param petId
     * @param cmInfoId
     * @return
     */
    @POST("app/cmFoodResearch/{petId}/foodResearch/{cminfoId}")
    fun postDiagnosisDetail(
        @Path("petId") petId: Int,
        @Path("cminfoId") cmInfoId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 소셜 로그인
     *
     * @param body
     * @return
     */
    @POST("app/user/loginUserWithAuth")
    fun postSNSLogin(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 회원가입하기
     *
     * @param body
     * @return
     */
    @POST("app/user/createUserWithAuth")
    fun postCreateUser(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 중복 계정 유저 통합하기
     *
     * @param body
     * @return
     */
    @POST("app/user/setCombineUser")
    fun postCombineUser(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 인증번호 보내기
     *
     * @param body
     * @return
     */
    @POST("app/user/passwordConfirmEmailSend")
    fun postAuthCodeSend(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 인증번호 확인
     *
     * @param body
     * @return
     */
    @POST("app/user/passwordConfirmCode")
    fun postAuthCodeConfirm(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 비밀번호 재설정
     *
     * @param body
     * @return
     */
    @POST("app/user/resetPasswordForWeb")
    fun postNewPassword(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 아이디 찾기
     *
     * @param body
     * @return
     */
    @POST("app/user/findAccount")
    fun postFindID(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 본인인증 후 유저 업데이트 하기
     *
     * @param body
     * @return
     */
    @POST("app/user/userConfirm")
    fun postUpdateUser(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 폰번호로 유저 검색 카운트
     *
     * @param phone
     * @param userId
     * @return
     */
    @GET("app/user/selectUserCountByPhone")
    fun getUserByPhone(
        @Query("phone") phone: String,
        @Query("userId") userId: Int
    ): Call<ResponseBody>

    /**
     * 병원 예약/접수 취소하기
     *
     * @param centerId
     * @param type
     * @param bookingId
     * @return
     */
    @DELETE("pb/history/{centerId}/{type}/{bookingId}")
    fun deleteBookingCancel(
        @Path("centerId") centerId: Int,
        @Path("type") type: String,
        @Path("bookingId") bookingId: Int
    ): Call<ResponseBody>

    /**
     * 맞춤형 사료 구매 URL 가져온다.
     *
     * @param body
     * @return
     */
    @POST("app/cmResult/vLab")
    fun postMatchFoodResult(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 병원 상세 의료진 리스트 가져온다.
     *
     * @param centerId
     * @return
     */
    @GET("pb/center/{centerId}/vets")
    fun getDoctorList(
        @Path("centerId") centerId: Int
    ): Call<DoctorListResponse>

    /**
     * 아마존 푸시 토큰 등록한다.
     *
     * @param body
     * @return
     */
    @PUT(BuildConfig.BASE_GW_URL + "api/users/device-token")
    fun putPushToken(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 종합검사 예약하기
     *
     * @param body
     * @return
     */
    @POST("/dna/inspection/{centerId}/{clinicRoomId}/booking")
    fun postHealtCare(
        @Path("centerId") centerId: Int,
        @Path("clinicRoomId") bookingId: Int,
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 종합검사 취소하기
     *
     * @param body
     * @return
     */
    @DELETE("/dna/inspection/{centerId}/CLINIC/{bookingId}")
    fun deleteHealthCare(
        @Path("centerId") centerId: Int,
        @Path("bookingId") bookingId: Int
    ): Call<ResponseBody>

    /**
     * 종합검사 결과정보 가져오기
     *
     * @param bookingId
     * @return
     */
    @GET("/dna/inspection/booking/{bookingId}/results")
    fun getHealthCareResultList(
        @Path("bookingId") bookingId: Int
    ): Call<ResponseBody>

    /**
     * 해당 펫의 검사결과 조회한다.
     *
     * @param petdocPetId
     * @return
     */
    @GET("/dna/inspection/results/{petdocPetId}")
    fun getHealthCareResultForPet(
        @Path("petdocPetId") petId: Int
    ): Call<ResponseBody>

    /**
     * 펫닥 건강검진 상태를 조회한다.
     *
     * @param petId
     * @return
     */
    @GET("/dna/inspection/pet/{petdocPetId}/status")
    fun getHealthCareStatus(
        @Path("petdocPetId") petId: Int
    ): Call<ResponseBody>

    /**
     * 종합검진 식별코드를 인증한다.
     *
     * @param authKey
     * @return
     */
    @GET("/app/dna/authkey/checkAuthKey/{authKey}")
    fun checkAuthKey(
        @Path("authKey") authKey: String,
        @Query("petId") petId: Int
    ): Call<ResponseBody>

    /**
     * 홈 숏컷 리스트를 가져온다.
     *
     * @return
     */
    @GET("/app/mainShotCut")
    fun getHomeShortCut(): Call<ResponseBody>

    /**
     *  종합검진 식별코드에 대한 상태를 조회한다.
     *
     * @param petId
     * @return
     */
    @GET("/app/dna/authkey")
    fun getAuthCodeStatus(
        @Query("petId") petId: Int
    ): Call<AuthCodeStatusResponse>

    /**
     * authCode로 bookingID를 조회한다.
     *
     * @return
     */
    @GET("/dna/inspection/{authCode}/booking")
    fun getBookingIdForAuthCode(
        @Path("authCode") authCode: String
    ): Call<ResponseBody>

    /**
     * 병원 방문 상태를 조회한다.(회수 요청 상태 조회)
     *
     * @param bookingId
     * @return
     */
    @GET("/dna/inspection/{bookingId}/visit")
    fun getDnaVisitStatus(
        @Path("bookingId") bookingId: Int
    ):Call<ResponseBody>

    /**
     * 반려동물 순서를 수정한다.
     *
     * @return
     */
    @POST(BuildConfig.BASE_GW_URL + "api/pets/priority-change")
    fun postPetListChange(
        @Body body: RequestBody
    ): Call<ResponseBody>


    @GET(BuildConfig.BASE_GW_URL + "bizpetdoc/ency/like/count")
    fun getMagazineLikeCount(
        @Query("encyIdList") id:HashSet<Int>
    ): Call<ResponseBody>

    // TODO. 서버 API 선작업 필요
    @POST("/app/scanner/firmware")
    suspend fun getLatestFirmwareVersion(): ScannerVersionResponse

    /**
     * 펫 아이디로 펫체크 어드밴스드 결과 등록 조회한다.
     *
     * @param id
     * @return
     */
    @GET("/dna/inspection/pets/results")
    fun getPetCheckResultForIds(): Call<ResponseBody>

    /**
     * 이메일 인증 메일 발송 기능
     *
     * @param body
     * @return
     */
    @POST("/app/user/sendEmailForConfirm")
    fun postSendEmailForConfirm(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 이메일 인증 여부 확인
     *
     * @param body
     * @return
     */
    @POST("/app/user/sendEmailConfirmServerToServer")
    fun postSendEmailForConfirmServer(
        @Body body: RequestBody
    ): Call<ResponseBody>

    /**
     * 알러지 검사 결과 코멘트 조회
    */
    @GET("dna/inspection/allergy/{categoryName}/comment")
    suspend fun getAllergyComment(
        @Path("categoryName") categoryName: String
    ): AllergyCommentResponse

    /**
     * 이메일을 사용하는 유저 카운트를 조회한다.
     *
     * @param email
     * @return
     */
    @GET("app/user/selectUserCountByEmail")
    fun getEmailUserCount(
        @Query("email") email: String
    ): Call<ResponseBody>
}

