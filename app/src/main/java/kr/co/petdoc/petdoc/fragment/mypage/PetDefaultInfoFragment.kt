package kr.co.petdoc.petdoc.fragment.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_chat_photo_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_default_info.*
import kotlinx.android.synthetic.main.fragment_pet_default_info.btnProfileAdd
import kotlinx.android.synthetic.main.fragment_pet_name.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.databinding.FragmentPetDefaultInfoBinding
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.petadd.PetNameFragmentDirections
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.compressImageFile
import kr.co.petdoc.petdoc.viewmodel.PetAddInfoDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

/**
 * Petdoc
 * Class: PetDefaultInfoFragment
 * Created by kimjoonsung on 2020/04/10.
 *
 * Description : 반려동물 기초 정보 화면
 */
class PetDefaultInfoFragment : BaseFragment() {

    private val LOGTAG = "PetDefaultInfoFragment"
    private val PET_IMAGE_REQUEST = "$LOGTAG.petImageRequest"

    private val RES_IMAGE = 100
    private var queryImageUrl: String = ""
    private var imageUri: Uri? = null

    private lateinit var petInfoModel: PetAddInfoDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        petInfoModel = ViewModelProvider(requireActivity()).get(PetAddInfoDataModel::class.java)

        val databinding = DataBindingUtil.inflate<FragmentPetDefaultInfoBinding>(inflater, R.layout.fragment_pet_default_info, container, false)
        databinding.lifecycleOwner = requireActivity()
        databinding.petAddInfo = petInfoModel

        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("mypage", "click", "pet_info_change", null, null, null)

        petProfile.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ), RES_IMAGE
            )
        }

        btnProfileAdd.setOnClickListener {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ), RES_IMAGE
            )
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        layoutName.setOnClickListener {
            findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petNameFragment2)
        }

        layoutPetKind.setOnClickListener {
            AppToast(requireContext()).showToastMessage(
                "반려동물 종류는 수정할 수 없습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )
        }

        layoutPetBreed.setOnClickListener {
            if (petInfoModel.petKind.value.toString() == "강아지" || petInfoModel.petKind.value.toString() == "고양이") {
                findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petBreedFragment3)
            } else {
                findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petSpeciesListFragment3)
            }
        }

        layoutPetBirth.setOnClickListener {
            findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petAgeFragment3)
        }

        layoutPetGender.setOnClickListener {
            findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petSexFragment3)
        }

        layoutPetVaccine.setOnClickListener {
            findNavController().navigate(R.id.action_petDefaultInfoFragment2_to_petVaccineFragment3)
        }

        //==========================================================================================

        petProfile.apply {
            when {
                petInfoModel.petProfile.value.toString().isNotEmpty() -> {
                    if (petInfoModel.petProfile.value.toString().startsWith("/data")) {
                        Glide.with(requireActivity())
                                .asBitmap()
                                .load(petInfoModel.petProfile.value.toString())
                                .apply(RequestOptions.circleCropTransform())
                                .into(petProfile)
                    } else {
                        Glide.with(requireActivity())
                                .load(if(petInfoModel.petProfile.value.toString().startsWith("http")) petInfoModel.petProfile.value.toString() else "${AppConstants.IMAGE_URL}${petInfoModel.petProfile.value.toString()}")
                                .apply(RequestOptions.circleCropTransform())
                                .into(petProfile)
                    }
                }

                else -> {
                    Glide.with(requireActivity())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petProfile)
                }
            }
        }

        petGender.apply {
            when (petInfoModel.petSex.value.toString()) {
                "man" -> {
                    if (petInfoModel.petNeutralization.value == true) {
                        text = "남아, 중성화 완료"
                    } else {
                        text = "남아, 중성화 전"
                    }
                }

                else -> {
                    if (petInfoModel.petNeutralization.value == true) {
                        text = "여아, 중성화 완료"
                    } else {
                        text = "여아, 중성화 전"
                    }
                }
            }
        }

        petVaccineStatus.apply {
            when(petInfoModel.petVaccine.value.toString()) {
                "donKnow" -> petVaccineStatus.text = "모름"
                "complete" -> petVaccineStatus.text = "접종 완료"
                "before" -> petVaccineStatus.text = "접종 전"
                "ing" -> petVaccineStatus.text = "접종 중"
            }
        }

        petBirth.text = "${petInfoModel.petYear.value.toString()}세${petInfoModel.petMonth.value.toString()}개월"
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
        when (event.tag) {
            PET_IMAGE_REQUEST -> {
                if ("ok" == event.status) {
                    try {
                        Logger.d("respone : ${event.response}")
                        val json = JSONObject(event.response)
                        if ("SUCCESS" == json["code"]) {
                            val obj = json.getJSONObject("resultData")
                            Logger.d("success : $obj")
                        } else {
                            Logger.d("error : ${json["messageKey"]}")
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
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
            }

            imageUri = Uri.fromFile(File(queryImageUrl))

            // /data/user/0/kr.co.petdoc.petdoc/files/Images/IMG_20200522035311.png
            Logger.d("queryImageUrl : $queryImageUrl")
            if (queryImageUrl.isNotEmpty()) {
                petInfoModel.petProfile.value = queryImageUrl

                Glide.with(this@PetDefaultInfoFragment)
                    .load(queryImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(petProfile)

                mApiClient.uploadPetNameWithProfile(
                    PET_IMAGE_REQUEST,
                    petInfoModel.petName.value.toString(),
                    queryImageUrl,
                    petInfoModel.petId.value!!
                )
            }
        }
    }
}