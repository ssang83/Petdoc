package kr.co.petdoc.petdoc.fragment.petadd

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_pet_name.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.*
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File


/**
 * Petdoc
 * Class: NameFragment
 * Created by kimjoonsung on 2020/04/06.
 *
 * Description : 반려동물 이름 등록 화면
 */
class PetNameFragment : BaseFragment() {

    private val LOGTAG = "PetNameFragment"
    private val PET_NAME_ADD_REQUEST = "$LOGTAG.petNameAddRequest"
    private val PET_NAME_ADD_WITH_PROFILE_REQUEST = "$LOGTAG.petNameAddWithProfileRequest"

    private val RES_IMAGE = 100

    private lateinit var dataModel:PetAddInfoDataModel

    private var queryImageUrl: String = ""
    private var imgPath: String = ""
    private var imageUri: Uri? = null

    private var petId = -1

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
        return inflater.inflate(R.layout.fragment_pet_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        if (dataModel.type.value == PetAddType.ADD.ordinal) {
            stepper.visibility = View.VISIBLE
            layoutHeader.visibility = View.GONE
            layoutProfile.visibility = View.VISIBLE
            btnNext.isEnabled = false

            FirebaseAPI(requireActivity()).logEventFirebase("추가_이름", "Page View", "이름 입력 단계 페이지뷰")
        } else {
            stepper.visibility = View.GONE
            layoutHeader.visibility = View.VISIBLE
            layoutProfile.visibility = View.GONE

            btnNext.text = Helper.readStringRes(R.string.confirm)
            btnNext.setTextColor(Helper.readColorRes(R.color.orange))
            btnNext.setBackgroundResource(R.drawable.main_color_round_rect)
            btnNext.isEnabled = true

            petId = dataModel.petId.value!!

            FirebaseAPI(requireActivity()).logEventFirebase("추가_이름_수정", "Page View", "이름 입력 단계 수정 페이지뷰")
        }

        imageViewProfile.setOnSingleClickListener{ imagePick() }
        btnProfileAdd.setOnSingleClickListener{ imagePick() }
        btnClose.setOnSingleClickListener { requireActivity().onBackPressed() }

        btnNext.setOnSingleClickListener {
            when (dataModel.type.value) {
                PetAddType.ADD.ordinal -> {
                    val name = editPetName.text.toString().trim()
                    if (name.isEmpty()) {
                        textViewNoInput.visibility = View.VISIBLE
                    } else {
                        textViewNoInput.visibility = View.GONE
                        sendApi()
                    }
                }

                else -> sendApi()
            }
        }

        editPetName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val name = s.toString().trim()
                btnNext.apply {
                    when {
                        name.isEmpty() == true -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey3))
                            setBackgroundResource(R.drawable.grey_round_rect)
                            textViewNoInput.visibility = View.VISIBLE
                        }

                        else -> {
                            setTextColor(Helper.readColorRes(R.color.orange))
                            setBackgroundResource(R.drawable.main_color_round_rect)

                            btnNext.isEnabled = true
                            textViewNoInput.visibility = View.GONE
                        }
                    }
                }
            }
        })

        editPetName.setText(dataModel.petName.value.toString())
        editPetName.setFilters(arrayOf<InputFilter>(EmojiExcludeFilter(), InputFilter.LengthFilter(10)))

        if (dataModel.petProfile.value.toString().isNotEmpty()) {
            Glide.with(requireActivity())
                .asBitmap()
                .load("${AppConstants.IMAGE_URL}${dataModel.petProfile.value.toString()}")
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewProfile)
        } else {
            Glide.with(requireActivity())
                .asBitmap()
                .load(R.drawable.img_pet_profile_default)
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewProfile)
        }

        // 카메라 및 파일 접근 권한 추가
        Helper.permissionCheck(requireActivity(),
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            object : PermissionCallback {
                override fun callback(status: PermissionStatus) {
                    when(status){
                        PermissionStatus.ALL_GRANTED -> { }
                        PermissionStatus.DENIED_SOME -> { }
                    }
                }
            },true )
    }

    override fun onResume() {
        super.onResume()
        dataModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PET_NAME_ADD_REQUEST)) {
            mApiClient.cancelRequest(PET_NAME_ADD_REQUEST)
        }

        if (mApiClient.isRequestRunning(PET_NAME_ADD_WITH_PROFILE_REQUEST)) {
            mApiClient.cancelRequest(PET_NAME_ADD_WITH_PROFILE_REQUEST)
        }
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RES_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == RES_IMAGE) {
                        handleImageRequest(data)
                    }
                }
            }
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            PET_NAME_ADD_REQUEST -> {
                if ("ok" == event.status) {
                    Logger.d("respone : ${event.response}")
                    try {
                        val json = JSONObject(event.response)
                        if ("SUCCESS" == json["code"]) {
                            dataModel.petName.value = editPetName.text.toString()
                            if (dataModel.petId.value == -1) {
                                val obj = json.getJSONObject("resultData")
                                petId = obj["petId"] as Int

                                if (dataModel.type.value == PetAddType.ADD.ordinal) {
                                    dataModel.petId.value = petId
                                    findNavController().navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                                } else {
                                    requireActivity().onBackPressed()
                                }
                            } else {
                                if (dataModel.type.value == PetAddType.ADD.ordinal) {
                                    findNavController().navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                                } else {
                                    requireActivity().onBackPressed()
                                }
                            }
                        } else {
                            Logger.d("error : ${json["messageKey"]}")
                            AppToast(requireContext()).showToastMessage(
                                    "이미 등록한 이름입니다. 다른 이름으로 등록해주세요.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM
                            )
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }

            PET_NAME_ADD_WITH_PROFILE_REQUEST -> {
                if ("ok" == event.status) {
                    Logger.d("respone : ${event.response}")
                    try {
                        val json = JSONObject(event.response)
                        if ("SUCCESS" == json["code"]) {
                            dataModel.petName.value = editPetName.text.toString()
                            if (dataModel.petId.value == -1) {
                                val obj = json.getJSONObject("resultData")
                                petId = obj["petId"] as Int

                                if (dataModel.type.value == PetAddType.ADD.ordinal) {
                                    dataModel.petId.value = petId
                                    findNavController().navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                                } else {
                                    requireActivity().onBackPressed()
                                }
                            } else {
                                if (dataModel.type.value == PetAddType.ADD.ordinal) {
                                    findNavController().navigate(PetNameFragmentDirections.actionPetNameFragmentToPetKindFragment())
                                } else {
                                    requireActivity().onBackPressed()
                                }
                            }
                        } else {
                            if (json["messageKey"] == "error.pet.update.exist.pet") {
                                AppToast(requireContext()).showToastMessage(
                                    "이미 등록한 이름입니다. 다른 이름으로 등록해주세요.",
                                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                                    AppToast.GRAVITY_BOTTOM
                                )
                            }
                            Logger.d("error : ${json["messageKey"]}")
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    private fun imagePick() {
        if (checkPermission()) {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RES_IMAGE)
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
                Glide.with(requireActivity())
                    .asBitmap()
                    .load(queryImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageViewProfile)

                dataModel.petProfile.value = queryImageUrl
            }
        }
    }

    private fun sendApi() {
        if (queryImageUrl.isNotEmpty()) {
            mApiClient.uploadPetNameWithProfile(
                    PET_NAME_ADD_WITH_PROFILE_REQUEST,
                    editPetName.text.toString(),
                    queryImageUrl,
                    dataModel.petId.value!!
            )
        } else {
            mApiClient.uploadPetName(
                    PET_NAME_ADD_REQUEST,
                    editPetName.text.toString(),
                    dataModel.petId.value!!
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

    //=============================================================================================
    inner class EmojiExcludeFilter : InputFilter {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            for (i in start until end) {
                val type = Character.getType(source[i])
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return ""
                }
            }
            return null
        }
    }

}