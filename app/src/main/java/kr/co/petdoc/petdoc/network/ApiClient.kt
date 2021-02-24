package kr.co.petdoc.petdoc.network

import android.content.Context
import android.content.LocusId
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.db.care.body.Temperature
import kr.co.petdoc.petdoc.db.care.humidity.Humidity
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage
import kr.co.petdoc.petdoc.network.event.RequestFinishedEvent
import kr.co.petdoc.petdoc.network.request.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Petdoc
 * Class: ApiClient
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description :
 */
class ApiClient(context: Context) {

    companion object {
        private const val HTTP_TIMEOUT = 30L // Seconds by default
        private val timeUnit = TimeUnit.SECONDS
        private lateinit var retrofit: Retrofit

        const val API_JAVA_DEV_URL = "https://gw-dev.petdoc.co.kr/v1/"
        const val API_JAVA_REAL_URL = "https://gw.petdoc.co.kr/v1/"
        const val API_JAVA_QA_URL = "https://gw-qa.petdoc.co.kr/v1/"
        const val API_APP_DEV_URL = "http://develbpetdocappserver-1964074790.ap-northeast-2.elb.amazonaws.com/"
        const val API_APP_REAL_URL = "https://app-real.petdoc.co.kr/"
        const val API_APP_QA_URL = "http://QAELBPetdocApp-524426134.ap-northeast-2.elb.amazonaws.com/"
        const val API_LOCAL_URL = "http://192.168.30.236:8082/" // 화영님 로컬 주소

        private var API_BASE_URL = BuildConfig.BASE_URL

        /**
         * Makes the ApiService calls.
         */
        private lateinit var mApiService: ApiService

        /**
         * The list of running requests. Used to cancel requests.
         */
        private lateinit var requests: MutableMap<String, AbstractApiRequest>

        fun getHttpTimeout() = HTTP_TIMEOUT
    }

    private val context: Context

    init {
        this.context = context
        requests = mutableMapOf()
        EventBus.getDefault().register(this)

        initAPIClient()
    }

    fun getApiService() = mApiService

    /**
     * A request has finished. Remove it from the list of running requests.
     *
     * @param event The event posted on the EventBus.
     */
    @Subscribe
    fun onEvent(event: RequestFinishedEvent) {
        System.gc()
        requests.remove(event.requestTag)
    }

    // ============================================================================================
    // Request functions
    // ============================================================================================

    /**
     * Execute a request to retrieve the update message. See {@link ApiService#getImageList(String)} for
     * parameter details.
     *
     * Test Request
     *
     * @param requestTag The tag for identifying the request.
     */
    fun getImageList(requestTag:String) {
        val request = ImageListRequest(mApiService, requestTag)
        requests.put(requestTag, request)
        request.execute()
    }

    fun uploadImage(requestTag: String, file:String) {
        val request = UploadImageRequest(mApiService, requestTag, file)
        requests.put(requestTag, request)
        request.execute()
    }


    fun getBannerList(requestTag: String, type:String) {
        val request = BannerListRequest(mApiService, requestTag, type)
        requests.put(requestTag, request)
        request.execute()
    }

    fun getMagazineList(requestTag: String, order:String, limit:Int, page:Int) {
        val request = MagazineListRequest(mApiService, requestTag, order, limit, page)
        requests[requestTag] = request
        request.execute()
    }

    fun getFamousPetTalk(requestTag: String) {
        val request = FamousPetTalkRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetTalkList(requestTag: String, category: String, limit:Int, offset:Int) {
        val request = PetTalkListRequest(mApiService, requestTag, category, limit, offset)
        requests[requestTag] = request
        request.execute()
    }


    fun getProfileInfo(requestTag: String) {
        val request = ProfileRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetInfoList(requestTag: String) {
        val request = PetInfoListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getUserGrade(requestTag: String) {
        val request = UserGradeRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun emailLogin(requestTag: String, _email:String, _password:String, _uuid:String){
        val request = EmailLoginRequest(mApiService, requestTag, _email, _password, _uuid)
        requests[requestTag] = request
        request.execute()
    }

    fun loadSplash(requestTag: String){
        val request = SplashImageRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getBanner(requestTag: String, id: Int) {
        val request = BannerDetailRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getShopList(requestTag: String, page: Int) {
        val request = ShoppingListRequest(mApiService, requestTag, page)
        requests[requestTag] = request
        request.execute()
    }

    fun getTemperatureListInfo(requestTag: String) {
        val request = TemperatureInfoListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatList(requestTag: String) {
        val request = ChatListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteChatItem(requestTag: String, pk:Int) {
        val request = ChatItemDeleteRequest(mApiService, requestTag, pk)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatDetail(requestTag: String, pk:Int) {
        val request = ChatDetailRequest(mApiService, requestTag, pk)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatCategoryList(requestTag: String) {
        val request = ChatCategoryListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getLegacyChatList(requestTag: String, kind:String, categoryId:String, order:String, limit:Int, offset:Int) {
        val request = LegacyChatListRequest(mApiService, requestTag, kind, categoryId, order, limit, offset)
        requests[requestTag] = request
        request.execute()
    }

    fun getLegacyChatTotalCount(requestTag: String, kind:String, categoryId: String) {
        val request = LegacyChatTotalCountRequest(mApiService, requestTag, kind, categoryId)
        requests[requestTag] = request
        request.execute()
    }

    fun getLegacyMagazineList(requestTag: String, categoryId: String, order:String, limit:Int, offset:Int) {
        val request = LegacyMagazineListRequest(mApiService, requestTag, categoryId, order, limit, offset)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineTotalCount(requestTag: String, categoryId: String) {
        val request = MagazineTotalCountRequest(mApiService, requestTag, categoryId)
        requests[requestTag] = request
        request.execute()
    }

    fun getRecommedProductList(requestTag: String) {
        val request = RecommendProductListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalList(requestTag: String, page:Int, count:Int) {
        val request = RelatedHospitalListRequest(mApiService, requestTag, page, count)
        requests[requestTag] = request
        request.execute()
    }

    fun postChatRoomsNoPicture(requestTag: String, pk:Int, message:String, isSerach:String) {
        val request = ChatRoomsRequest(mApiService, requestTag, pk, message, isSerach)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatKeywordList(requestTag: String) {
        val request = ChatKeywordListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatSearchDetail(requestTag: String, pk:Int) {
        val request = ChatSearchDetailRequest(mApiService, requestTag, pk)
        requests[requestTag] = request
        request.execute()
    }

    fun getChatSearchResultList(requestTag: String, keyword:String, kind:String, categoryId:String, order:String, limit:Int, offset:Int) {
        val request = ChatSearchResultListRequest(mApiService, requestTag, keyword, kind, categoryId, order, limit, offset)
        requests[requestTag] = request
        request.execute()
    }

    fun postChatAddImage(requestTag: String, roomId: Int, file: String) {
        val request = ChatRoomAddImageRequest(mApiService, requestTag, roomId, file)
        requests[requestTag] = request
        request.execute()
    }

    fun postChatAddMessage(requestTag: String, roomId: Int, message: String) {
        val request = ChatRoomAddMessageRequest(mApiService, requestTag, roomId, message)
        requests[requestTag] = request
        request.execute()
    }

    fun putChatRoomQuit(requestTag: String, roomId: Int) {
        val request = ChatRoomQuitRequest(mApiService, requestTag, roomId)
        requests[requestTag] = request
        request.execute()
    }

    fun putChatReview(requestTag: String, roomId: Int, rate:String) {
        val request = ChatReviewRequest(mApiService, requestTag, roomId, rate)
        requests[requestTag] = request
        request.execute()
    }

    fun putRegusterPush(requestTag: String) {
        val request = RegisterPushRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetTalkDetail(requestTag: String, id: Int) {
        val request = PetTalkDetailRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun postPetTalkViewCount(requestTag: String, id: Int) {
        val request = PetTalkViewCountRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetTalkReplyList(requestTag: String, petTalkId: Int) {
        val request = PetTalkReplyListRequest(mApiService, requestTag, petTalkId)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetTalkReplyPagingList(requestTag: String, petTalkId: Int, baseId:Int) {
        val request = PetTalkReplyPagingListRequest(mApiService, requestTag, petTalkId, baseId)
        requests[requestTag] = request
        request.execute()
    }

    fun postReplyAdd(requestTag: String, type: String, petTalkId: Int, comment: String) {
        val request = ReplyAddRequest(mApiService, requestTag, type, petTalkId, comment)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteReply(requestTag: String, type: String, id: Int) {
        val request = ReplyDeleteRequest(mApiService, requestTag, type, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getLifeMagazineList(requestTag: String, categoryId:String, order:String, limit:Int, offset:Int) {
        val request = LifeMagazineListRequest(mApiService, requestTag, categoryId, order, limit, offset)
        requests[requestTag] = request
        request.execute()
    }

    fun getLifeMagazineSearchList(requestTag: String, keyword: String, categoryId:String, order:String, limit:Int, offset:Int) {
        val request = LifeMagazineSearchListRequest(mApiService, requestTag, keyword, categoryId, order, limit, offset)
        requests[requestTag] = request
        request.execute()
    }

    fun postChatRoomRecommend(requestTag: String, roomId: Int) {
        val request = ChatRoomsRecommendRequest(mApiService, requestTag, roomId)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteChatRoomRecommend(requestTag: String, roomId: Int) {
        val request = ChatRoomsRecommendCancelRequest(mApiService, requestTag, roomId)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetTalk(requestTag: String, title:String, content:String, type: String, images:List<String>) {
        val request = PetTalkUploadRequest(mApiService, requestTag, title, content, type, images)
        requests[requestTag] = request
        request.execute()
    }

    fun modifyPetTalk(requestTag: String, id:Int, title:String, content:String, type: String, images:List<String>) {
        val request = PetTalkModifyRequest(mApiService, requestTag, id, title, content, type, images)
        requests[requestTag] = request
        request.execute()
    }

    fun deletePetTalk(requestTag: String, id: Int) {
        val request = PetTalkDeleteRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineDetail(requestTag: String, id: Int) {
        val request = MagazineDetailRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineDetailSubList(requestTag: String, id: Int) {
        val request = MagazineDetailSubListRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun postMagazineViewcount(requestTag: String, id: Int) {
        val request = MagazineDetailViewCountRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineLikeAndBookmarkCount(requestTag: String, id: Int) {
        val request = MagazineDetailLikeCountRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineLikeAndBookmarkStatus(requestTag: String, id: Int) {
        val request = MagazineDetailLikeStatusRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun setMagazineLike(requestTag: String, id: Int) {
        val request = MagazineDetailLikeRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun cancelMagazineLike(requestTag: String, id: Int) {
        val request = MagazineDetailLikeCancelRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun setMagazineBookmark(requestTag: String, id: Int) {
        val request = MagazineDetailBookmarkRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun cancelMagazineBookmark(requestTag: String, id: Int) {
        val request = MagazineDetailBookmarkCancelRequet(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getHomeHospitalList(
        requestTag: String,
        latitude: String,
        longitude: String,
        keyword: String,
        working:String,
        register:String,
        booking:String,
        beauty:String,
        hotel:String,
        allDay:String,
        parking:String,
        healthCheck:String,
        petIdList:MutableList<Int>,
        size: Int,
        page: Int
    ) {
        val request = HospitalListRequest(
            mApiService,
            requestTag,
            latitude,
            longitude,
            keyword,
            working,
            register,
            booking,
            beauty,
            hotel,
            allDay,
            parking,
            healthCheck,
            petIdList,
            size,
            page)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalDetail(requestTag: String, centerId: Int) {
        val request = HospitalDetailRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun addBookmarkHospital(requestTag: String, centerId: Int) {
        val request = HospitalAddBookmarkRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun cancelBookmarkHospital(requestTag: String, centerId: Int) {
        val request = HospitalCancelBookmarkRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalKeyword(requestTag: String) {
        val request = HospitalSearchKeywordRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalRegisterInfo(requestTag: String, id: Int) {
        val request = HospitalRegisterInfoRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun postHospitalClinicRegisterRequest(
        requestTag: String,
        centerId: Int,
        roomId: Int,
        petId: Int,
        memo: String,
        clinicIdList: MutableList<Int>
    ) {
        val request = HospitalClinicRegisterRequest(mApiService, requestTag, centerId, roomId, petId, memo, clinicIdList)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalReservationInfo(requestTag: String, id: Int) {
        val request = HospitalReservationInfoRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalReservationClinicInfo(requestTag: String, id: Int) {
        val request = HospitalReservationClinicInfoRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalReservationBeautyInfo(requestTag: String, id: Int) {
        val request = HospitalReservationBeautyInfoRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getTimeTableList(requestTag: String, id: Int, date:String) {
        val request = HospitalReservationTimeTableListRequest(mApiService, requestTag, id, date)
        requests[requestTag] = request
        request.execute()
    }

    fun postHospitalClinicReservationRequest(
        requestTag: String,
        centerId: Int,
        roomId: Int,
        petId: Int,
        memo: String,
        bookingTime:String,
        clinicIdList: MutableList<Int>
    ) {
        val request = HospitalClinicReservationRequest(mApiService, requestTag, centerId, roomId, petId, memo, bookingTime, clinicIdList)
        requests[requestTag] = request
        request.execute()
    }

    fun postLogout(requestTag: String) {
        val request = LogoutRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getCareRecordList(requestTag: String, petId: Int, date: String) {
        val request = CareRecordListRequest(mApiService, requestTag, petId, date)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadCareRecord(
        requestTag: String,
        petId: Int,
        value: String,
        date: String,
        type: String
    ) {
        val request = CareRecordUploadRequest(mApiService, requestTag, petId, value, date, type)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteCareRecord(requestTag: String, petId: Int) {
        val request = CareRecordDeleteRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getCareHistoryList(requestTag: String, petId: Int, date: String) {
        val request = CareHistoryListRequest(mApiService, requestTag, petId, date)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadTemperatureRecord(requestTag: String, items: List<Temperature>) {
        val request = TemperatureUploadRequest(mApiService, requestTag, items)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadHumidityRecord(requestTag: String, items: List<Humidity>) {
        val request = HumidityUploadRequest(mApiService, requestTag, items)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadEarImageRecord(requestTag: String, petId: Int, items: List<EarImage>) {
        val request = EarImageUploadRequest(mApiService, requestTag, petId, items)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadInjectionRecord(requestTag: String, petId: Int, items: List<Temperature>) {
        val request = TempInjectUploadRequest(mApiService, requestTag, petId, items)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteTemperatureRecord(requestTag: String, petId: Int, items: List<Temperature>) {
        val request = TemperatureDeleteRequest(mApiService, requestTag, petId, items)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteHumidityRecord(requestTag: String, petId: Int, items: List<Humidity>) {
        val request = HumidityDeleteRequest(mApiService, requestTag, petId, items)
        requests[requestTag] = request
        request.execute()
    }

    fun getBodyTemperatureRecordList(requestTag: String, petId: Int, offset: Int, limit: Int) {
        val request = BodyTemperatureRecordListRequest(mApiService, requestTag, petId, offset, limit)
        requests[requestTag] = request
        request.execute()
    }

    fun getHumidityRecordList(requestTag: String, petId: Int, offset: Int, limit: Int) {
        val request = HumidityRecordListRequest(mApiService, requestTag, petId, offset, limit)
        requests[requestTag] = request
        request.execute()
    }

    fun getMyHospitalHistoryRegisterList(requestTag: String) {
        val request = MyHospitalHistoryRegisterListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getMyHospitalHistoryBookingList(requestTag: String) {
        val request = MyHospitalHistoryBookingListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getMyHospitalHistoryDetail(requestTag: String, type: String, bookingId: Int) {
        val request = MyHospitalHistoryDetailRequest(mApiService, requestTag, type, bookingId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalBookmarkList(requestTag: String, latitude: Double, longitude: Double) {
        val request = HospitalBookmarkListRequest(mApiService, requestTag, latitude, longitude)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineBookmarkList(requestTag: String) {
        val request = MagazineBookmarkListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getMyPetTalkList(requestTag: String) {
        val request = MyPetTalkListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getNoticeList(requestTag: String) {
        val request = NoticeListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getQuestionList(requestTag: String) {
        val request = QuestionListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadQuestion(requestTag: String, title: String, content: String) {
        val request = QuestionUploadRequest(mApiService, requestTag, title, content)
        requests[requestTag] = request
        request.execute()
    }

    fun putPushStatus(requestTag: String, chatNoti: String, commentNoti: String, gradeNoti:String) {
        val request = PushStatusRequest(mApiService, requestTag, chatNoti, commentNoti, gradeNoti)
        requests[requestTag] = request
        request.execute()
    }

    fun putMarketingStatus(requestTag: String, status: String) {
        val request = MarketingStatusRequest(mApiService, requestTag, status)
        requests[requestTag] = request
        request.execute()
    }

    fun deleteSignOut(requestTag: String) {
        val request = SignOutRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getMyUserProfile(requestTag: String) {
        val request = MyUserProfileRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun updateMyUserProfile(
        requestTag: String,
        nickName: String,
        image: String,
        passoword: String,
        phone: String,
        privacyPeriod: Int
    ) {
        val request = MyUserProfileModifyRequest(
            mApiService,
            requestTag,
            nickName,
            image,
            passoword,
            phone,
            privacyPeriod
        )

        requests[requestTag] = request
        request.execute()
    }

    fun postEmailVerify(requestTag: String, email:String) {
        val request = MyUserProfileEmailVerifyRequest(mApiService, requestTag, email)
        requests[requestTag] = request
        request.execute()
    }

    fun postPhoneVerify(requestTag: String, number:String) {
        val request = MyUserProfilePhoneVerifyRequest(mApiService, requestTag, number)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetName(requestTag: String, name:String, petId: Int) {
        val request = PetAddNameRequest(mApiService, requestTag, name, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetNameWithProfile(requestTag: String, name:String, image: String, petId: Int) {
        val request = PetAddNameWithProfileRequest(mApiService, requestTag, name, image, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetKindList(requestTag: String) {
        val request = PetKindListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetSpeciesList(requestTag: String, kind: String, name: String) {
        val request = PetSpeciesListRequest(mApiService, requestTag, kind, name)
        requests[requestTag] = request
        request.execute()
    }

    fun deletePet(requestTag: String, petId: Int) {
        val request = PetDeleteRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetKind(requestTag: String, petId: Int, kind:String) {
        val request = PetAddKindRequest(mApiService, requestTag, petId, kind)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetSpecies(requestTag: String, petId: Int, species:String, speciesId:Int) {
        val request = PetAddSpeciesRequest(mApiService, requestTag, petId, species, speciesId)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetAge(requestTag: String, petId: Int, birtday:String, ageType:Int) {
        val request = PetAddBirthRequest(mApiService, requestTag, petId, birtday, ageType)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetGender(requestTag: String, petId: Int, gender:String, isNeutrion:Boolean) {
        val request = PetAddGenderRequest(mApiService, requestTag, petId, gender, isNeutrion)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPetVaccineStatus(requestTag: String, petId: Int, status: String) {
        val request = PetAddVaccineStatusRequest(mApiService, requestTag, petId, status)
        requests[requestTag] = request
        request.execute()
    }

    fun getMatchFoodList(requestTag: String) {
        val request = FoodRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getMatchFoodResult(requestTag: String, petId: Int) {
        val request = DiagnosisResultRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getSpeciesInfo(requestTag: String, speciesId: Int) {
        val request = SpeciesInfoRequest(mApiService, requestTag, speciesId)
        requests[requestTag] = request
        request.execute()
    }

    fun getCustomealInfo(requestTag: String, petId: Int) {
        val request = CustomealInfoRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getFeedInfo(requestTag: String, feedId: Int) {
        val request = FeedInfoRequest(mApiService, requestTag, feedId)
        requests[requestTag] = request
        request.execute()
    }

    fun getNutritionInfo(requestTag: String, pnId: Int) {
        val request = NutritionInfoRequest(mApiService, requestTag, pnId)
        requests[requestTag] = request
        request.execute()
    }

    fun getPriceCategoryList(requestTag: String, type:String) {
        val request = PriceCategoryListRequest(mApiService, requestTag, type)
        requests[requestTag] = request
        request.execute()
    }

    fun getPriceCalculate(requestTag: String, type:String, gender: String, age:Int, weight:String, categoryId: Int) {
        val request = PriceCalculateRequest(mApiService, requestTag, type, gender, age, weight, categoryId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHomePetTalkList(requestTag: String) {
        val request = HomePetTalkListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getUserPoint(requestTag: String) {
        val request = PointRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getUserPointLogList(requestTag: String) {
        val request = PointLogListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun postHospitalBookingRequire(requestTag: String, centerId: Int) {
        val request = HospitalRequireBookingRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun postHospitalEdit(requestTag: String, centerId: Int, type:HashSet<String>) {
        val request = HospitalInfoEditRequest(mApiService, requestTag, centerId, type)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadWeight(requestTag: String, petId: Int, cmInfoId: Int, weight: Float) {
        val request = WeightUploadRequest(mApiService, requestTag, petId, cmInfoId, weight)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadBodyType(requestTag: String, petId: Int, cmInfoId: Int, bodyType: Int) {
        val request = BodyTypeUploadRequest(mApiService, requestTag, petId, cmInfoId, bodyType)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadWalkTime(requestTag: String, petId: Int, cmInfoId: Int, time:String) {
        val request = WalkTimeUploadRequest(mApiService, requestTag, petId, cmInfoId, time)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadLikeFood(requestTag: String, petId: Int, cmInfoId: Int, food: HashSet<Int>) {
        val request = LikeFoodUploadRequest(mApiService, requestTag, petId, cmInfoId, food)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadAllergeFood(requestTag: String, petId: Int, cmInfoId: Int, food: HashSet<Int>) {
        val request = AllergeFoodUploadRequest(mApiService, requestTag, petId, cmInfoId, food)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadCurrentFeed(
        requestTag: String,
        petId: Int,
        cmInfoId: Int,
        dryFood: String,
        wetFood: String,
        snack: String,
        dryFoodName:String,
        wetFoodName:String,
        snackName:String
    ) {
        val request = CurrentFeedUploadRequest(
            mApiService,
            requestTag,
            petId,
            cmInfoId,
            dryFood,
            wetFood,
            snack,
            dryFoodName,
            wetFoodName,
            snackName
        )
        requests[requestTag] = request
        request.execute()
    }

    fun uploadPregnantStatus(requestTag: String, petId: Int, cmInfoId: Int, pregnantValue:String, pregnantState:Int) {
        val request = PregnantStatusUploadRequest(mApiService, requestTag, petId, cmInfoId, pregnantValue, pregnantState)
        requests[requestTag] = request
        request.execute()
    }

    fun getDetailDiagnosisList(requestTag: String, petId: Int, cmInfoId: Int) {
        val request = DetailDiagnosisListRequest(mApiService, requestTag, petId, cmInfoId)
        requests[requestTag] = request
        request.execute()
    }

    fun uploadDetailDiagnosis(requestTag: String, petId: Int, cmInfoId: Int, questionId:HashSet<Int>) {
        val request = DetailDiagnosisUploadRequest(mApiService, requestTag, petId, cmInfoId, questionId)
        requests[requestTag] = request
        request.execute()
    }

    fun snsLogin(requestTag: String, socialKey: String, socialType: Int, token:String = "") {
        val request = SNSLoginRequest(mApiService, requestTag, socialKey, socialType, token)
        requests[requestTag] = request
        request.execute()
    }

    fun emailSignUp(requestTag: String, email: String, password: String, nickName: String) {
        val request = EmailSignUpRequest(mApiService, requestTag, email, password, nickName)
        requests[requestTag] = request
        request.execute()
    }

    fun snsSignUp(
        requestTag: String,
        email: String,
        nickName: String,
        socialKey: String,
        socialType: Int,
        token: String = ""
    ) {
        val request = SNSSignUpRequest(mApiService, requestTag, email, nickName, socialKey, socialType, token)
        requests[requestTag] = request
        request.execute()
    }

    fun combineUser(requestTag: String, userId: Int, phone: String) {
        val request = CombineUserRequest(mApiService, requestTag, userId, phone)
        requests[requestTag] = request
        request.execute()
    }

    fun findID(requestTag: String, name: String, phone: String) {
        val request = FindIDRequest(mApiService, requestTag, name, phone)
        requests[requestTag] = request
        request.execute()
    }

    fun sendAuthCode(requestTag: String, email: String) {
        val request = AuthCodeSendRequest(mApiService, requestTag, email)
        requests[requestTag] = request
        request.execute()
    }

    fun confirmAuthCode(requestTag: String, email: String, code:String) {
        val request = AuthCodeConfirmRequest(mApiService, requestTag, email, code)
        requests[requestTag] = request
        request.execute()
    }

    fun newPassword(requestTag: String, email: String, password: String, code:String) {
        val request = NewPasswordUploadRequest(mApiService, requestTag, email, password, code)
        requests[requestTag] = request
        request.execute()
    }

    fun updateUser(
        requestTag: String,
        userId: Int,
        name: String,
        birthday: String,
        gender: String,
        phone: String
    ) {
        val request = UpdateUserRequest(mApiService, requestTag, userId, name, birthday, gender, phone)
        requests[requestTag] = request
        request.execute()
    }

    fun getUserCountByPhone(requestTag: String, userId: Int, phone: String) {
        val request = UserCountByPhoneRequest(mApiService, requestTag, userId, phone)
        requests[requestTag] = request
        request.execute()
    }

    fun bookingCancel(requestTag: String, centerId: Int, type: String, bookingId: Int) {
        val request = HospitalBookingCancelRequest(mApiService, requestTag, centerId, type, bookingId)
        requests[requestTag] = request
        request.execute()
    }

    fun getMatchFoodResultForVLab(requestTag: String, petId: Int) {
        val request = MatchFoodResultRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getDoctorList(requestTag: String, centerId: Int) {
        val request = DoctorListRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun registerFCMToken(requestTag: String, token: String) {
        val request = PushTokenRegisterRequest(mApiService, requestTag, token)
        requests[requestTag] = request
        request.execute()
    }

    fun postHealthCare(
        requestTag: String,
        centerId: Int,
        roomId: Int,
        petId: Int,
        authCode: String,
        bookingTime: String
    ) {
        val request = HealthCareBookingRequest(
            mApiService,
            requestTag,
            centerId,
            roomId,
            petId,
            authCode,
            bookingTime
        )

        requests[requestTag] = request
        request.execute()
    }

    fun cancelHealthCare(requestTag: String, centerId: Int, bookingId: Int) {
        val request = HealthCareBookingCancelRequest(mApiService, requestTag, centerId, bookingId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHealthCareResultList(requestTag: String, bookingId: Int) {
        val request = HealthCareResultListRequest(mApiService, requestTag, bookingId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHealthCareResultForPet(requestTag: String, petId: Int) {
        val request = HealthCareResultForPetRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHealthCareStatus(requestTag: String, petId: Int) {
        val request = HealthCareStatusRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun checkAuthCode(requestTag: String, authCode: String, petId: Int) {
        val request = HealthCareCheckAuthCodeRequest(mApiService, requestTag, authCode, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHomeShortcutList(requestTag: String) {
        val request = HomeShorCutListRequest(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun getAuthCodeStatus(requestTag: String, petId: Int) {
        val request = HealthCareAuthCodeStatusRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getBookingIdForAuthCode(requestTag: String, authCode: String) {
        val request = HealthCareBookingIdRequest(mApiService, requestTag, authCode)
        requests[requestTag] = request
        request.execute()
    }

    fun getDnaVisitStatusRequest(requestTag: String, bookingId: Int) {
        val request = HealthCareVisitRequest(mApiService, requestTag, bookingId)
        requests[requestTag] = request
        request.execute()
    }

    fun getHospitalBookmarkStatus(requestTag: String, centerId: Int) {
        val request = HospitalBookmarkStatusRequest(mApiService, requestTag, centerId)
        requests[requestTag] = request
        request.execute()
    }

    fun changePetList(requestTag: String, petId:MutableList<Int>) {
        val request = PetListChangeRequest(mApiService, requestTag, petId)
        requests[requestTag] = request
        request.execute()
    }

    fun getMagazineLikeCount(requestTag: String, id:HashSet<Int>) {
        val request = MagazineLikeCountRequest(mApiService, requestTag, id)
        requests[requestTag] = request
        request.execute()
    }

    fun getPetCheckResultForIds(requestTag: String) {
        val request = HealthCareResultForPetIds(mApiService, requestTag)
        requests[requestTag] = request
        request.execute()
    }

    fun postEmailConfirm(requestTag: String, email: String) {
        val request = EmailConfirmRequest(mApiService, requestTag, email)
        requests[requestTag] = request
        request.execute()
    }

    fun postEmailConfirmToServer(requestTag: String, emailKey: String, email: String) {
        val request = EmailConfirmToServerRequest(mApiService, requestTag, emailKey, email)
        requests[requestTag] = request
        request.execute()
    }

    fun getUserCountByEmail(requestTag: String, email: String) {
        val request = UserCountByEmailRequest(mApiService, requestTag, email)
        requests[requestTag] = request
        request.execute()
    }

    // ============================================================================================
    // Public functions
    // ============================================================================================

    /**
     * Look up the event with the passed tag in the event list. If the request is found, cancel it
     * and remove it from the list.
     *
     * @param requestTag Identifies the request.
     * @return True if the request was cancelled, false otherwise.
     */
    fun cancelRequest(requestTag: String) : Boolean {
        System.gc()
        val request = requests[requestTag]

        if(request != null) {
            request.cancel()
            requests.remove(requestTag)
            return true
        } else {
            return false
        }
    }

    fun isRequestRunning(requestTag: String) = requests.containsKey(requestTag)

    fun changeApiBaseUrl(newApiBaseUrl:String) {
        API_BASE_URL = newApiBaseUrl
        initAPIClient()
    }

    private fun initAPIClient() {
        val okBuilder = MyOkHttpBuilder.getOkHttpBuilder(context)

//        okBuilder.retryOnConnectionFailure(true);
//        okBuilder.followRedirects(false);

        val httpClient = okBuilder.connectTimeout(HTTP_TIMEOUT, timeUnit)
            .writeTimeout(HTTP_TIMEOUT, timeUnit)
            .readTimeout(HTTP_TIMEOUT, timeUnit)
            .build()

        val builder = Retrofit.Builder()
        retrofit = builder
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(API_BASE_URL)
            .client(httpClient)
            .build()

        mApiService = retrofit.create(ApiService::class.java)
    }
}