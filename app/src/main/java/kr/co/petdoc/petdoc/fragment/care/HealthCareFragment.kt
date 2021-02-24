package kr.co.petdoc.petdoc.fragment.care

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_health_care.*
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import org.koin.android.ext.android.inject

/**
 * Petdoc
 * Class: HealthCareFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareFragment : BaseFragment() {

    private val TAG = "HealthCareFragment"
    private val PET_CHECK_RESULT_LIST_REQUEST = "$TAG.petCheckResultListRequest"

    private lateinit var dataModel: HospitalDataModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window.statusBarColor = Helper.readColorRes(R.color.little_orange)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnSingleClickListener { requireActivity().onBackPressed() }
        btnTest.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                findNavController().navigate(HealthCareFragmentDirections.actionHealthCareFragmentToHealthCareCodeFragment())
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        btnPurchase.setOnSingleClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                if (StorageUtils.loadIntValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_MOBILE_CONFIRM
                    ) == 1
                ) {
                    findNavController().navigate(HealthCareFragmentDirections.actionHealthCareFragmentToPurchaseFragment())
                } else {
                    requireContext().startActivity<MobileAuthActivity>()
                }
            } else {
                requireContext().startActivity<LoginAndRegisterActivity>()
            }
        }

        btnHospitalSearch.setOnSingleClickListener {
            dataModel.isPurchseBtnShow.value = true
            bundleOf("hideHospitalList" to true).let {
                findNavController().navigate(
                    R.id.action_healthCareFragment_to_healthCareHospitalFragment,
                    it
                )
            }
        }

        resultLayer.setOnSingleClickListener {
            startActivity(Intent(requireActivity(), MyPageActivity::class.java).apply {
                putExtra("fromHealthCare", true)
                putExtra("result", true)
            })
        }

        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            layoutHeader.apply {
                when {
                    scrollY > 0 -> {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        requireActivity().window.statusBarColor = Helper.readColorRes(R.color.white)
                        setBackgroundResource(R.color.white)
                    }
                    else -> {
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        requireActivity().window.statusBarColor = Helper.readColorRes(R.color.little_orange)
                        setBackgroundResource(R.color.little_orange)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPetInfo()
        mApiClient.getPetCheckResultForIds(PET_CHECK_RESULT_LIST_REQUEST)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PET_CHECK_RESULT_LIST_REQUEST)) {
            mApiClient.cancelRequest(PET_CHECK_RESULT_LIST_REQUEST)
        }

        super.onDestroyView()
    }

    private fun loadPetInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository by inject<PetdocRepository>()
            val oldUserId = if (StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").isNotEmpty()) {
                StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
            } else {
                0
            }
            val items = repository.retrieveMyPets(oldUserId)
            if (items.isNotEmpty()) {
                PetdocApplication.mPetInfoList.clear()
                PetdocApplication.mPetInfoList.addAll(items)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when (data.tag) {
            PET_CHECK_RESULT_LIST_REQUEST -> {
                if (data.status == "ok") {
                    try {
                        val code = JSONObject(data.response)["code"]
                        val mesageKey = JSONObject(data.response)["messageKey"].toString()
                        val resultData = JSONObject(data.response)["resultData"] as JSONObject
                        if ("SUCCESS" == code) {
                            val json = resultData["petResult"] as JSONArray
                            val items: MutableList<PetInfoItem> = Gson().fromJson(json.toString(), object : TypeToken<MutableList<PetInfoItem>>() {}.type)

                            if(items.size > 0) {
                                resultLayer.visibility = View.VISIBLE

                                if (items.size == 2) {
                                    petProfile1.apply {
                                        when {
                                            items[0].imageUrl.isNullOrEmpty() -> {
                                                Glide.with(requireContext())
                                                        .load(R.drawable.img_pet_profile_default)
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile1)
                                            }
                                            else -> {
                                                Glide.with(requireContext())
                                                        .load(if (items[0].imageUrl!!.startsWith("http")) items[0].imageUrl else "${AppConstants.IMAGE_URL}${items[0].imageUrl}")
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile1)
                                            }
                                        }
                                    }

                                    petProfile2.apply {
                                        when {
                                            items[1].imageUrl.isNullOrEmpty() -> {
                                                Glide.with(requireContext())
                                                        .load(R.drawable.img_pet_profile_default)
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile2)
                                            }
                                            else -> {
                                                Glide.with(requireContext())
                                                        .load(if (items[1].imageUrl!!.startsWith("http")) items[1].imageUrl else "${AppConstants.IMAGE_URL}${items[1].imageUrl}")
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile2)
                                            }
                                        }
                                    }
                                } else {
                                    petProfile1.apply {
                                        when {
                                            items[0].imageUrl.isNullOrEmpty() -> {
                                                Glide.with(requireContext())
                                                        .load(R.drawable.img_pet_profile_default)
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile1)
                                            }
                                            else -> {
                                                Glide.with(requireContext())
                                                        .load(if (items[0].imageUrl!!.startsWith("http")) items[0].imageUrl else "${AppConstants.IMAGE_URL}${items[0].imageUrl}")
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .into(petProfile1)
                                            }
                                        }
                                    }

                                    petProfile2.visibility = View.GONE
                                }
                            } else {
                                resultLayer.visibility = View.GONE
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
}