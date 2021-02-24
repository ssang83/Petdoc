package kr.co.petdoc.petdoc.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kr.co.petdoc.petdoc.network.response.submodel.*

/**
 * Petdoc
 * Class: Response
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class ImageListResponse : AbstractApiResponse() {

    // Test code
    @SerializedName("image_list")
    @Expose
    var imageResults:MutableList<ImageResult> = mutableListOf()

}


/**
 * Petdoc
 * Class: Response
 * Created by sungminkim on 2020/04/02.
 *
 * Description: splash image list response
 */
class SplashImageResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var splashImageResults:MutableList<SplashItem> = mutableListOf()
}

/**
 * Banner list response
 */
class BannerListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var bannerList:MutableList<BannerItem> = mutableListOf()
}


class PetTalkResponse : AbstractApiResponse(){

    @SerializedName("items")
    @Expose
    var petTalkList:MutableList<PetTalkItem> = mutableListOf()
}


/**
 * Pet info response
 */
class PetInfoListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var petInfoList:MutableList<PetInfoItem> = mutableListOf()
}

/**
 * Banner Detail Response
 */
class BannerDetailResponse : AbstractApiResponse() {

    @SerializedName("banner")
    @Expose
    lateinit var bannerDetail:BannerItem
}

/**
 * Shop list Response
 */
class ShoppingListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var shopList:MutableList<ProductItem> = mutableListOf()
}

/**
 * Chat list Response
 */
class ChatListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var chatList:MutableList<ChatItem> = mutableListOf()
}

/**
 * Chat Detail Response
 */
class ChatDetailResponse : AbstractApiResponse() {

    @SerializedName("item")
    @Expose
    lateinit var chatDetail:ChatDetailItem
}

/**
 * Chat Category List Response
 */
class ChatCategoryListResponse : AbstractApiResponse() {

    @SerializedName("category_list")
    @Expose
    var chatCategoryList:MutableList<ChatCategoryItem> = mutableListOf()
}

/**
 * 상담 종료 추천제품 리스트
 */
class RecommendProductListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var recommendProductList:MutableList<RecommendProductItem> = mutableListOf()
}

/**
 * 상담 종료 병원 리스트
 */
class RelatedHospitalListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var relatedHospitalList:MutableList<RelatedHospitalItem> = mutableListOf()
}

/**
 * 상담 탑 검색어 리스트
 */
class ChatKeywordListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var keywordList:MutableList<ChatKeywordItem> = mutableListOf()
}

/**
 * 상담 탑 검색어 리스트
 */
class ChatSearchResultListResponse : AbstractApiResponse() {

    @SerializedName("totalCount")
    @Expose
    var totalCount:Int = 0

    @SerializedName("data")
    @Expose
    var searchResultList:MutableList<LegacyChatItem> = mutableListOf()
}

/**
 * 라이브 매거진 더보기 검색
 */
class LifeMagazineSearchListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:MagazineSearchData
}

/**
 * 병원 정보 가져오기
 *
 */
class HospitalListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:HospitalListData
}

/**
 * 병원 상세 정보 가져오기
 *
 */
class HospitalDetailResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:HospitaDetailData
}

/**
 * 병원 검색 추천 키워드
 *
 */
class HospitalKeywordResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var keywordList:MutableList<String> = mutableListOf()
}

/**
 * 병원 예약정보 Response
 *
 */
class HospitalReservationInfoResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:ReservationInfoData
}

class CareRecordListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    var careRecord:MutableList<CareRecordData> = mutableListOf()
}

class CareHistoryListRespone : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:ResultData
}

class BodyTemperatureRecordListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:BodyTemperatureResult
}

class HumidityRecordListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:HumidityResult
}

class HospitalBookmarkListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:MutableList<HospitalBookmarkData>
}

class MagazineBookmarkListResponse : AbstractApiResponse() {

    @SerializedName("magazines")
    @Expose
    lateinit var bookmarkList:MutableList<MagazineBookmarkData>
}

class MyPetTalkListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    lateinit var petTalkList:MutableList<MyPetTalkData>
}

class NoticeListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    lateinit var noticeList:MutableList<NoticeData>
}

class QuestionsListResponse : AbstractApiResponse() {

    @SerializedName("items")
    @Expose
    lateinit var questionList:MutableList<QuestionData>
}

class MyUserProfileResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var userData:MyUserProfileData
}

class PriceCategoryListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var resultData:MutableList<PriceCategory> = mutableListOf()
}

class PriceCalcuateResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:PriceCalculate
}

class HomePetTalkListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var items:MutableList<HomePetTalkData> = mutableListOf()
}

class UserPointResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:PointData
}

class UserPointLogListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:PointLogData
}

class DetailDiagnosisListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:DetailDiagnosisData
}

class HospitalHistoryResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:HospitalHistoryData
}

class RegisterHistoryListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var resultData:MutableList<RegisterHistoryData> = mutableListOf()
}

class BookingHistoryListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var resultData:MutableList<ReservationHistoryData> = mutableListOf()
}

class DoctorListResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var resultData:MutableList<Vet> = mutableListOf()
}

class AuthCodeStatusResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:AuthCodeStatusItem
}

class MagazineListRespone : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    var resultData:MutableList<MagazineItem> = mutableListOf()
}

class MagazineDetailResponse : AbstractApiResponse() {

    @SerializedName("resultData")
    @Expose
    lateinit var resultData:MagazineDetailItem
}