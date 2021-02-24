package kr.co.petdoc.petdoc.fragment.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_change_my_information.*
import kotlinx.android.synthetic.main.fragment_change_my_information.emptyTouch
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_mypage_main.*
import kotlinx.android.synthetic.main.fragment_upload.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.auth.AuthorizationActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.databinding.FragmentChangeMyInformationBinding
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MyUserProfileResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.UserInfoData
import kr.co.petdoc.petdoc.utils.*
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.regex.Pattern


/**
 * petdoc-android
 * Class: ChangeMyInformationFragment
 * Created by sungminkim on 2020/04/08.
 *
 * Description : 내 정보 수정 화면
 */

class ChangeMyInformationFragment : BaseFragment() {

    private val TAG = "ChangeMyInformationFragment"
    private val USER_PROFILE_MODITY_REQUEST = "$TAG.userProfileModifyRequest"
    private val MY_USER_PROFILE_REQUEST = "$TAG.myUserProfileRequest"

    private val REQUEST_AUTH = 1001
    private val REQUEST_PHONE_CHANGE = 1002

    private val passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&.]).{8,16}$") // 영문, 숫자, 특수문자 조합

    enum class ProfilePattern{
        PASSWORD_FORMAT_ERROR, SUCCESS
    }

    private lateinit var myInformation : MyPageInformationModel
//    private var observerProfileCandidate : Observer<String>? = null

    private val RES_IMAGE = 100
    private var queryImageUrl: String = ""
    private var imgPath: String = ""
    private var imageUri: Uri? = null

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        myInformation = ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java)

        val databinding = FragmentChangeMyInformationBinding.inflate(inflater, container,false)
        databinding.lifecycleOwner = requireActivity()
        databinding.myinfo = myInformation

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("mypage", "click", "my_info", null, null, null)

        readyUIandEvent()

        //=============================================================================================

        change_information_store_button.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("정보변경_저장하기", "Click Event", "정보 수정하여 변경 버튼 클릭")

            Logger.d("nickname : ${change_information_nickname_text.text}")
            mApiClient.updateMyUserProfile(
                USER_PROFILE_MODITY_REQUEST,
                change_information_nickname_text.text.toString(),
                myInformation.profile_image.value.toString(),
                change_information_password_text.text.toString(),
                myInformation.phone.value.toString(),
                myInformation.privacyPeroid.value!!
            )
        }

        emptyTouch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    if (change_information_nickname_text.isFocused) {
                        hideKeyboard(change_information_nickname_text)
                    }

                    if (change_information_password_text.isFocused) {
                        hideKeyboard(change_information_password_text)
                    }

                    if (change_information_password_confirm_text.isFocused) {
                        hideKeyboard(change_information_password_confirm_text)
                    }

                    if (change_information_phone_text.isFocused) {
                        hideKeyboard(change_information_phone_text)
                    }
                }

                return false
            }
        })

        change_information_nickname_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.toString()!!.isBlank()) {
                    change_information_store_button.isEnabled = false
                    change_information_store_button.setTextColor(Helper.readColorRes(R.color.white_alpha))
                    return
                }

                if (myInformation.nickname.value.toString() != s?.toString()) {
                    change_information_store_button.isEnabled = true
                    change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
                } else {
                    change_information_store_button.isEnabled = false
                    change_information_store_button.setTextColor(Helper.readColorRes(R.color.white_alpha))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        change_information_password_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordValidation.apply {
                    when {
                        s?.isBlank() == true -> {
                            visibility = View.GONE
                        }

                        s?.length!! >= 8 && passwordPattern.matcher(s?:"").matches() -> {
                            visibility = View.GONE
                        }

                        else -> {
                            visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        change_information_password_confirm_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.length!! < 8) return

                passwordConfirmValidation.apply {
                    when {
                        s?.isBlank() == true -> visibility = View.GONE

                        change_information_password_text.text.toString() == s?.toString() -> {
                            visibility = View.GONE

                            change_information_store_button.isEnabled = true
                            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
                        }

                        else -> {
                            visibility = View.VISIBLE

                            change_information_store_button.isEnabled = false
                            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white_alpha))
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //==========================================================================================
        change_information_store_button.isEnabled = false

        mApiClient.getProfileInfo(MY_USER_PROFILE_REQUEST)
    }

    override fun onResume() {
        super.onResume()
        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
        Handler().postDelayed( { mKeyboardHeightProvider?.start() }, 1000)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(USER_PROFILE_MODITY_REQUEST)) {
            mApiClient.cancelRequest(USER_PROFILE_MODITY_REQUEST)
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }

        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RES_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleImageRequest(data)
                }
            }

            REQUEST_AUTH -> {
                if (resultCode == Activity.RESULT_OK) {
                    requireActivity().finish()
                }
            }

            REQUEST_PHONE_CHANGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mApiClient.getProfileInfo(MY_USER_PROFILE_REQUEST)
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            USER_PROFILE_MODITY_REQUEST -> {
                if (response is MyUserProfileResponse) {
                    if ("SUCCESS" == response.code) {
                        Airbridge.trackEvent("mypage", "click", "my_info_change", null, null, null)

                        AppToast(requireContext()).showToastMessage(
                            "프로필이 수정되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event:NetworkBusResponse) {
        when (event.tag) {
            MY_USER_PROFILE_REQUEST -> {
                if (event.status == "ok") {
                    try {
                        val profileItem: UserInfoData = Gson().fromJson(event.response, UserInfoData::class.java)
                        myInformation.email.value = profileItem.email
                        myInformation.nickname.value = profileItem.nickname
                        myInformation.phone.value = if (profileItem.phone != null) profileItem.phone else ""
                        myInformation.loginType.value = profileItem.social_type.toInt()
                        myInformation.privacyPeroid.value = if (profileItem.privacy_save_period != null) profileItem.privacy_save_period else -1
                        myInformation.phoneVerification.value =
                                if (profileItem.mobile_confirm == 1) {
                                    true
                                } else {
                                    false
                                }

                        if(profileItem.profile_path != null) {
                            Glide.with(requireContext()).load(if(profileItem.profile_path.startsWith("http")) profileItem.profile_path else "${AppConstants.IMAGE_URL}${profileItem.profile_path}")
                                .apply(RequestOptions.circleCropTransform())
                                .into(change_information_profile_image)

                            myInformation.profile_image.value = profileItem.profile_path
                        } else {
                            Glide.with(requireContext()).load(R.drawable.profile_default)
                                .apply(RequestOptions.circleCropTransform())
                                .into(change_information_profile_image)
                        }

                        privacyPanelUpdate()
                        phoneVerificationButtonUpdate()
                        checkLoginType()

                        change_information_phone_text.text = profileItem.phone
                        change_information_email_id_text.text = profileItem.email
                        change_information_nickname_text.setText(profileItem.nickname)

                        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM, profileItem.mobile_confirm)
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    /**
     * 소프트 키보드가 올라오면 이벤트가 넘어온다.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.d("height:" + bus.height)

        // 키보드가 올라가면 리스트 제일 끝으로 스크롤
        if (bus.height > 100) {
            emptyTouch.visibility = View.VISIBLE
        } else {
            emptyTouch.visibility = View.GONE
        }
    }

    //==============================================================================================

    private fun readyUIandEvent(){

        information_change_back_button.setOnClickListener { requireActivity().onBackPressed() }

        change_information_period_1.setOnClickListener {
            myInformation.privacyPeroid.value = 1
            privacyPanelUpdate()

            change_information_store_button.isEnabled = true
            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
        }
        change_information_period_3.setOnClickListener {
            myInformation.privacyPeroid.value = 3
            privacyPanelUpdate()

            change_information_store_button.isEnabled = true
            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
        }
        change_information_period_5.setOnClickListener {
            myInformation.privacyPeroid.value = 5
            privacyPanelUpdate()

            change_information_store_button.isEnabled = true
            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
        }
        change_information_period_forever.setOnClickListener {
            myInformation.privacyPeroid.value = 0
            privacyPanelUpdate()

            change_information_store_button.isEnabled = true
            change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))
        }

        change_information_phone_change.setOnClickListener {
            if(StorageUtils.loadIntValueFromPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM) == 1) {
                startActivityForResult(Intent(requireActivity(), AuthorizationActivity::class.java), REQUEST_PHONE_CHANGE)
            } else {
                startActivityForResult(Intent(requireActivity(), MobileAuthActivity::class.java), REQUEST_AUTH)
            }
        }

        change_information_profile_image.setOnClickListener { imagePick()}

        btnPhoneAuth.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), MobileAuthActivity::class.java), REQUEST_AUTH)
        }
    }


    private fun privacyPanelUpdate(){
        change_information_period_1.apply{
            setTextColor(Helper.readColorRes(R.color.light_grey3))
            setBackgroundResource(0)
        }
        change_information_period_3.apply{
            setTextColor(Helper.readColorRes(R.color.light_grey3))
            setBackgroundResource(0)
        }
        change_information_period_5.apply{
            setTextColor(Helper.readColorRes(R.color.light_grey3))
            setBackgroundResource(0)
        }
        change_information_period_forever.apply{
            setTextColor(Helper.readColorRes(R.color.light_grey3))
            setBackgroundResource(0)
        }

        when(myInformation.privacyPeroid.value){
            1 -> {
                change_information_period_1.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setBackgroundResource(R.drawable.white_solid_round_rect)
                }
            }
            3 -> {
                change_information_period_3.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setBackgroundResource(R.drawable.white_solid_round_rect)
                }
            }
            5 -> {
                change_information_period_5.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setBackgroundResource(R.drawable.white_solid_round_rect)
                }
            }
            else -> {
                change_information_period_forever.apply{
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    setBackgroundResource(R.drawable.white_solid_round_rect)
                }
            }
        }
    }

    private fun phoneVerificationButtonUpdate(){
        change_information_phone_change?.post {
            if (myInformation.phoneVerification.value == true) {
                layoutPhoneNumber.visibility = View.VISIBLE
                layoutPhoneAuth.visibility = View.GONE
            } else {
                layoutPhoneNumber.visibility = View.GONE
                layoutPhoneAuth.visibility = View.VISIBLE
            }
        }
    }

    private fun checkLoginType() {
        layoutPassword.apply {
            if (myInformation.loginType.value == 1) {
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun handleImageRequest(data: Intent?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            Toast.makeText(
                requireContext(),
                t.localizedMessage ?: "Some error occured, please try again later",
                Toast.LENGTH_SHORT
            ).show()
        }

        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            if (data?.data != null) {     //Photo from gallery
                imageUri = data.data
                queryImageUrl = imageUri?.path!!
                queryImageUrl = requireActivity().compressImageFile(queryImageUrl, false, imageUri!!)
            } else {
                queryImageUrl = imgPath ?: ""
                requireActivity().compressImageFile(queryImageUrl, uri = imageUri!!)
            }
            imageUri = Uri.fromFile(File(queryImageUrl))

            // /data/user/0/kr.co.petdoc.petdoc/files/Images/IMG_20200522035311.png
            Logger.d("queryImageUrl : $queryImageUrl")
            if (queryImageUrl.isNotEmpty()) {
                Glide.with(this@ChangeMyInformationFragment)
                    .load(queryImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(change_information_profile_image)

                change_information_store_button.isEnabled = true
                change_information_store_button.setTextColor(Helper.readColorRes(R.color.white))

//                ViewModelProvider(requireActivity()).get(MyPageInformationModel::class.java).apply{
//                    profile_image_candidate.value = queryImageUrl
//                    profile_image.value = queryImageUrl
//                }
            }
        }
    }

    private fun imagePick() {
        if (checkPermission()) {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ), RES_IMAGE
            )
        }
    }

    private fun checkPermission(): Boolean {
        val isGranted = isPermissionsGranted(
            requireContext(),
            arrayOf(
                PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_CAMERA
            )
        )

        if (!isGranted) {
            askCompactPermissions(arrayOf(
                PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_READ_EXTERNAL_STORAGE,
                PermissionUtils.Manifest_CAMERA
            ),
                object : PermissionResult {
                    override fun permissionGranted() {
                        imagePick()
                    }

                    override fun permissionDenied() {
                        AppToast(requireContext()).showToastMessage(
                            "프로필 사진을 수정하려면 권한이 필요합니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }

                    override fun permissionForeverDenied() {
                        AppToast(requireContext()).showToastMessage(
                            "프로필 사진을 수정하려면 권한이 필요합니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                })
        }
        return isGranted
    }
}