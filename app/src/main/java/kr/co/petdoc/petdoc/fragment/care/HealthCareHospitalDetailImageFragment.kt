package kr.co.petdoc.petdoc.fragment.care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_health_care_hospital_image_detail.*
import kotlinx.android.synthetic.main.hospital_detail_image_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalDetailResponse
import kr.co.petdoc.petdoc.network.response.submodel.Img
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: HealthCareHospitalDetailImageFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class HealthCareHospitalDetailImageFragment : BaseFragment() {

    private val TAG = "HealthCareHospitalDetailImageFragment"
    private val HOSPITAL_DETAIL_REQUEST = "${TAG}.hospitalDetailRequest"

    private lateinit var dataModel: HospitalDataModel

    private lateinit var imageAdapter: ImageAdapter
    private var hospitalImagItems:MutableList<Img> = mutableListOf()

    private var totalCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(
            R.layout.fragment_health_care_hospital_image_detail,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        //=========================================================================================
        if (dataModel.hospitalImageItems.value!!.size > 0) {
            hospitalImagItems = dataModel.hospitalImageItems.value!!
            imagePager.offscreenPageLimit = hospitalImagItems.size

            totalCount = hospitalImagItems.size
            count.text = "1 / ${totalCount}"
        } else {
            val id = arguments?.getInt("id")!!
            mApiClient.getHospitalDetail(HOSPITAL_DETAIL_REQUEST, id)
        }

        imageAdapter = ImageAdapter()
        imagePager.apply {
            adapter = imageAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        imagePager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                count.text = "${position + 1} / ${totalCount}"
            }
        })
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(HOSPITAL_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_DETAIL_REQUEST)
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
            HOSPITAL_DETAIL_REQUEST -> {
                if (response is HospitalDetailResponse) {
                    hospitalImagItems.addAll(response.resultData.imgList)
                    imageAdapter.notifyDataSetChanged()

                    imagePager.offscreenPageLimit = hospitalImagItems.size

                    totalCount = hospitalImagItems.size
                    count.text = "1 / ${totalCount}"
                }
            }
        }
    }

    private fun onVideoClicked(item: Img) {
        if (item.videoStatusYn == "Y") {
            bundleOf("videoUrl" to item.videoUrl).let {
                findNavController().navigate(R.id.action_healthCareHospitalDetailImageFragment_to_hospitalVideoFragment2, it)
            }
        } else {
            AppToast(requireContext()).showToastMessage(
                "동영상 인코딩 중입니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )
        }
    }

    //===============================================================================================
    inner class ImageAdapter : RecyclerView.Adapter<ImageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ImageHolder(layoutInflater.inflate(R.layout.hospital_detail_image_item, parent, false))

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            holder.setImage(hospitalImagItems[position].imgUrl)

            if (hospitalImagItems[position].videoYn == "Y") {
                holder.itemView.videoPlay.visibility = View.VISIBLE
            } else {
                holder.itemView.videoPlay.visibility = View.GONE
            }

            holder.itemView.videoPlay.setOnClickListener { onVideoClicked(hospitalImagItems[position]) }
        }

        override fun getItemCount() = hospitalImagItems.size
    }

    inner class ImageHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            Glide.with(requireContext()).load(url).into(item.hospitalImg)
        }
    }
}