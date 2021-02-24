package kr.co.petdoc.petdoc.fragment.care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_doctor_item.view.*
import kotlinx.android.synthetic.main.fragment_health_care_hospital_doctor.*
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.DoctorListResponse
import kr.co.petdoc.petdoc.network.response.submodel.Vet
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: HealthCareHospitalVetFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class HealthCareHospitalVetFragment : BaseFragment() {

    private val TAG = "HealthCareHospitalVetFragment"

    private val DOCTOR_LIST_REQUEST = "$TAG.doctorListRequest"

    private lateinit var doctorAdapter: DoctorAdapter
    private var doctorItems:MutableList<Vet> = mutableListOf()

    private lateinit var dataModel: HospitalDataModel

    private var margin40 = 0
    private var centerId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_hospital_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        centerId = arguments?.getInt("centerId") ?: centerId
        Logger.d("centerId : $centerId")

        doctorItems = dataModel.doctorItems.value!!

        margin40 = Helper.convertDpToPx(requireContext(), 40f).toInt()

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        //=========================================================================================
        doctorAdapter = DoctorAdapter()
        doctorList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = doctorAdapter
        }

        mApiClient.getDoctorList(DOCTOR_LIST_REQUEST, centerId)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(DOCTOR_LIST_REQUEST)) {
            mApiClient.cancelRequest(DOCTOR_LIST_REQUEST)
        }

        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    /**
     * response
     *
     * @param response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            DOCTOR_LIST_REQUEST -> {
                if (response is DoctorListResponse) {
                    if (response.code == "SUCCESS") {
                        doctorItems.clear()
                        doctorItems.addAll(response.resultData)
                        doctorAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    //===============================================================================================
    inner class DoctorAdapter : RecyclerView.Adapter<DoctorHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DoctorHolder(layoutInflater.inflate(R.layout.adapter_doctor_item, parent, false))

        override fun onBindViewHolder(holder: DoctorHolder, position: Int) {
            holder.setImage(doctorItems[position].imgUrl)

            val name = doctorItems[position].name
            val vet = doctorItems[position].position
            holder.setName("$name$vet")

            holder.setDoctorClinicType(doctorItems[position].keyword)

            val desc = doctorItems[position].desc.split("\\r\\n|\\n|\\r")
            var description = ""
            for (i in 0 until desc.size) {
                if (i == desc.size - 1) {
                    description += desc[i]
                } else {
                    description += "${desc[i]}\n"
                }
            }

            holder.setDoctorCarrier(description)

            if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    bottomMargin = margin40
                }
            }
        }

        override fun getItemCount() = doctorItems.size
    }

    inner class DoctorHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            item.doctorImg.apply {
                when {
                    url.isNotEmpty() -> {
                        Glide.with(requireContext())
                            .load(if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}${url}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.doctorImg)
                    }

                    else -> {
                        Glide.with(requireContext())
                            .load(R.drawable.img_profile)
                            .apply(RequestOptions.circleCropTransform())
                            .into(item.doctorImg)
                    }
                }
            }
        }

        fun setName(name: String) {
            item.doctorName.text = name
        }

        fun setDoctorClinicType(type: List<String>) {
            item.clinicType.removeAllViews()

            for (i in 0 until type.size) {
                item.clinicType.addView(DoctorClinicType(type[i]))
            }
        }

        fun setDoctorCarrier(career: String) {
            item.doctorCareer.text = career
        }
    }

    //==============================================================================================
    inner class DoctorClinicType(type:String) : FrameLayout(requireContext()) {

        init {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_doctor_clinic_type, null, false)

            view.type.text = type

            addView(view)
        }
    }
}