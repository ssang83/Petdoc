package kr.co.petdoc.petdoc.fragment.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_mypage_main.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.ImagePickDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.network.response.submodel.UserInfoData
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


/**
 * petdoc-android
 * Class: MyPageMainFragment
 * Created by sungminkim on 2020/04/08.
 *
 * Description : 마이페이지 메인 화면
 */

class MyPageMainFragment : BaseFragment(), ImagePickDialogFragment.OnPickCompleteListener {

    private val TAG = "MyPageMainFragment"
    private val MY_USER_PROFILE_REQUEST = "$TAG.myUserProfileRequest"
    private val PET_CHECK_RESULT_LIST_REQUEST = "$TAG.petCheckResultListRequest"

    private lateinit var myInformation : MyPageInformationModel

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        myInformation = ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_mypage_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readyUIandEvent()

        retryIfNetworkAbsent {
            mApiClient.getProfileInfo(MY_USER_PROFILE_REQUEST)
            mApiClient.getPetCheckResultForIds(PET_CHECK_RESULT_LIST_REQUEST)
        }
    }

    override fun onPickCompleted(profileImage: File) {
        Logger.d("profileImage : ${profileImage.absolutePath}")
    }

    private fun readyUIandEvent(){

//        mypage_user_profile_image.setOnClickListener {
//            ImagePickDialogFragment().apply {
//                setListener(this@MyPageMainFragment)
//            }.show(childFragmentManager, "ImagePick")
//        }

        mypage_close_button.setOnSingleClickListener {
            requireActivity().finish()
        }

        mypage_user_information_update.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("정보변경", "Click Event", "정보변경 버튼 클릭")
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToChangeMyInformationFragment())
        }

        mypage_mypet_information_area.setOnSingleClickListener {
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToPetInformationFragment())
        }

        mypage_petpoint_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("펫 포인트", "Click Event", "펫포인트 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "pet_point", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToPetPointFragment())
        }

        mypage_mystory_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("내게시글", "Click Event", "내게시글 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "my_content", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPostFragment())
        }

        mypage_bookmark_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("북마크", "Click Event", "북마크 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "bookmark", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToBookmarkFragment())
        }

        mypage_setting_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("설정", "Click Event", "설정 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "settings", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToSettingMainFragment())
        }

        mypage_notice_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("공지사항", "Click Event", "공지사항 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "notice", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToNoticeFragment())
        }

        mypage_mypet_history_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("접수예약내역", "Click Event", "접수예약 클릭")
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyHospitalBookingFragment())
        }

        mypage_my_album.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("내사진앨범", "Click Event", "내 사진앨범 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "my_picture", null, null, null)
            bundleOf("fromMy" to true).let {
                findNavController().navigate(R.id.action_myPageMainFragment_to_imageAlbumFragment, it)
            }
        }

        mypage_external_device.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("외부연결기기", "Click Event", "외부연결기기 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "device", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToExternalDeviceFragment())
        }

        mypage_customer_report.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("앱불편사항접수", "Click Event", "앱 불편사항 접수 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "cs", null, null, null)
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToCustomerReportFragment())
        }

        mypage_coupon_area.setOnSingleClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("쿠폰함", "Click Event", "쿠폰함 메뉴 클릭")
            Airbridge.trackEvent("mypage", "click", "coupon", null, null, null)
            val cipherStr = getAESCipherStr()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?renderurl=/mypage/coupon.php&referer=true&jsonbody=${cipherStr}")))
        }

        mypage_health_care_area.setOnSingleClickListener {
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyHealthCareFragment())
        }

        mypage_purchase_history_area.setOnSingleClickListener {
            findNavController().navigate(MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPurchaseHistoryFragment())
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(MY_USER_PROFILE_REQUEST)) {
            mApiClient.cancelRequest(MY_USER_PROFILE_REQUEST)
        }

        if (mApiClient.isRequestRunning(PET_CHECK_RESULT_LIST_REQUEST)) {
            mApiClient.cancelRequest(PET_CHECK_RESULT_LIST_REQUEST)
        }

        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when (data.tag) {
            MY_USER_PROFILE_REQUEST -> {
                if (data.status == "ok") {
                    val profileItem: UserInfoData = Gson().fromJson(data.response, UserInfoData::class.java)

                    PetdocApplication.mUserInfo = profileItem

                    if(profileItem.profile_path.isNullOrEmpty()) {
                        Glide.with(this@MyPageMainFragment).load(R.drawable.profile_default)
                            .apply(RequestOptions.circleCropTransform())
                            .into(mypage_user_profile_image)
                    } else {
                        Glide.with(this@MyPageMainFragment).load(if(profileItem.profile_path.startsWith("http")) profileItem.profile_path else "${AppConstants.IMAGE_URL}${profileItem.profile_path}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(mypage_user_profile_image)

                        myInformation.profile_image.value = if(profileItem.profile_path.startsWith("http")) {
                            profileItem.profile_path
                        } else {
                            "${AppConstants.IMAGE_URL}${profileItem.profile_path}"
                        }
                    }

                    mypage_user_name_text.text = profileItem.nickname
                    mypage_user_grade.text = getLevelText(profileItem.activity_grade.toString())
                } else {
                    Logger.d("code : ${data.code}, response : ${data.response}")
                }
            }

            PET_CHECK_RESULT_LIST_REQUEST -> {
                if (data.status == "ok") {
                    try {
                        val code = JSONObject(data.response)["code"]
                        val mesageKey = JSONObject(data.response)["messageKey"].toString()
                        val resultData = JSONObject(data.response)["resultData"] as JSONObject
                        if ("SUCCESS" == code) {
                            val json = resultData["petResult"] as JSONArray
                            val items: MutableList<PetInfoItem> = Gson().fromJson(json.toString(), object : TypeToken<MutableList<PetInfoItem>>() {}.type)

                            if (items.size > 0) {
                                careBadge.visibility = View.VISIBLE
                            } else {
                                careBadge.visibility = View.GONE
                            }
                        } else {
                            Logger.d("error : $mesageKey")
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    private fun getLevelText(level: String) =
        when (level) {
            "1" -> "Lv.1 그냥주이니"
            "2" -> "Lv.2 초보주이니"
            "3" -> "Lv.3 중수주이니"
            "4" -> "Lv.4 고수주이니"
            "5" -> "Lv.5 만점주이니"
            else -> ""
        }

    private fun getAESCipherStr(): String {
        val json = JSONObject().apply {
            put("user_id", StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, ""))
            put("name", PetdocApplication.mUserInfo?.name)
        }

        return AES256Cipher.encryptURL(json.toString())
    }
}