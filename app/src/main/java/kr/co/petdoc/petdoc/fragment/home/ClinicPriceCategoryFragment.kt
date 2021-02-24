package kr.co.petdoc.petdoc.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.fragment_clinic_price_category.*
import kotlinx.android.synthetic.main.view_chat_category.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.fragment.scanner.CalibrationDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.PriceCalcuateResponse
import kr.co.petdoc.petdoc.network.response.PriceCategoryListResponse
import kr.co.petdoc.petdoc.network.response.submodel.Category
import kr.co.petdoc.petdoc.network.response.submodel.PriceCategory
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ClinicPriceDataModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ClinicPriceCategoryFragment
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class ClinicPriceCategoryFragment : BaseFragment(), PriceCalculateDialogFragment.CallbackListener {

    private val TAG = "ClinicPriceCategoryFragment"
    private val CATEGORY_LIST_REQUEST = "$TAG.categoryListRequest"
    private val CLINIC_CALCULATE_REQUEST = "$TAG.clinicCalculateRequest"

    private var firstCategoryItems:MutableList<PriceCategory> = mutableListOf()
    private var secondCategoryItems:MutableList<Category> = mutableListOf()
    private var optionItems:MutableList<Category> = mutableListOf()
    private lateinit var firstCategoryAdapter:FirstCategoryAdapter
    private var secondCagegoryAdapter:SecondCategoryAdapter? = null
    private var optionAdapter:OptionAdapter? = null

    private lateinit var clinicDataModel: ClinicPriceDataModel

    private var firstCategoryId = 0
    private var secondCategoryId = 0
    private var optionId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        clinicDataModel = ViewModelProvider(requireActivity()).get(ClinicPriceDataModel::class.java)
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_clinic_price_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnConfirm.setOnClickListener {
            mApiClient.getPriceCalculate(
                CLINIC_CALCULATE_REQUEST,
                clinicDataModel.petKind.value.toString(),
                clinicDataModel.petGender.value.toString(),
                clinicDataModel.petYear.value ?: 0,
                clinicDataModel.petWeight.value.toString(),
                if(secondCategoryId != 0) secondCategoryId else optionId
            )
        }

        firstCategoryAdapter = FirstCategoryAdapter()
        firstCategoryList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

            adapter = firstCategoryAdapter
        }

        mApiClient.getPriceCategoryList(CATEGORY_LIST_REQUEST, clinicDataModel.petKind.value.toString())

        btnConfirm.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(CATEGORY_LIST_REQUEST)) {
            mApiClient.cancelRequest(CATEGORY_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(CLINIC_CALCULATE_REQUEST)) {
            mApiClient.cancelRequest(CLINIC_CALCULATE_REQUEST)
        }
    }

    override fun onDissmiss() {
        requireActivity().finish()
        EventBus.getDefault().post("Hospital")
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            CATEGORY_LIST_REQUEST -> {
                if (response is PriceCategoryListResponse) {
                    if ("SUCCESS" == response.code) {
                        firstCategoryItems = response.resultData
                        firstCategoryAdapter.notifyDataSetChanged()
                    } else {
                        Logger.d("error : ${response.messageKey}")
                    }
                }
            }

            CLINIC_CALCULATE_REQUEST -> {
                if (response is PriceCalcuateResponse) {
                    if ("SUCCESS" == response.code) {
                        clinicDataModel.priceMin.value = response.resultData.priceMin
                        clinicDataModel.priceMax.value = response.resultData.priceMax

                        Airbridge.trackEvent("home", "click", "price_result", null, null, null)
                        PriceCalculateDialogFragment(this@ClinicPriceCategoryFragment).show(childFragmentManager, "Price Clinic")
                    } else {
                        Logger.d("error : ${response.messageKey}")
                    }
                }
            }
        }
    }

    private fun onFirstCategoryClicked(item: PriceCategory) {
        firstCategoryId = item.id
        layoutOption.visibility = View.GONE
        layoutSecondCategory.visibility = View.GONE

        btnConfirm.isEnabled = false
        btnConfirm.setTextColor(Helper.readColorRes(R.color.white_alpha))

        if (item.child.size > 0) {
            layoutSecondCategory.visibility = View.VISIBLE

            secondCategoryItems.clear()
            secondCategoryItems.addAll(item.child)
            secondCagegoryAdapter = SecondCategoryAdapter()
            secondCategoryList.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }

                adapter = secondCagegoryAdapter
            }
        } else {
            layoutSecondCategory.visibility = View.GONE
        }
    }

    private fun onSecondCategoryClicked(item: Category) {
        if (item.child.size > 0) {
            layoutOption.visibility = View.VISIBLE
            secondCategoryId = 0

            btnConfirm.isEnabled = false
            btnConfirm.setTextColor(Helper.readColorRes(R.color.white_alpha))

            optionItems.clear()
            optionItems.addAll(item.child)
            optionAdapter = OptionAdapter()
            optionList.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }

                adapter = optionAdapter
            }
        } else {
            layoutOption.visibility = View.GONE

            secondCategoryId = item.id

            btnConfirm.isEnabled = true
            btnConfirm.setTextColor(Helper.readColorRes(R.color.white))
        }
    }

    private fun onOptionClicked(item: Category) {
        optionId = item.id

        btnConfirm.isEnabled = true
        btnConfirm.setTextColor(Helper.readColorRes(R.color.white))
    }

    //==============================================================================================
    inner class FirstCategoryAdapter : RecyclerView.Adapter<CategoryHolder>() {
        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(layoutInflater.inflate(R.layout.view_chat_category, parent, false))

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            if (selectedPosition == position) {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.orange))
                holder.itemView.category.setBackgroundResource(R.drawable.orange_round_rect_6)
            } else {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.dark_grey))
                holder.itemView.category.setBackgroundResource(R.drawable.lightbluegrey_round_rect_6)
            }

            holder.setCategory(firstCategoryItems[position].name)

            holder.itemView.setOnClickListener {
                Logger.d("position : $position")
                onFirstCategoryClicked(firstCategoryItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = firstCategoryItems.size
    }

    //==============================================================================================
    inner class SecondCategoryAdapter : RecyclerView.Adapter<CategoryHolder>() {
        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(layoutInflater.inflate(R.layout.view_chat_category, parent, false))

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            if (selectedPosition == position) {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.orange))
                holder.itemView.category.setBackgroundResource(R.drawable.orange_round_rect_6)
            } else {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.dark_grey))
                holder.itemView.category.setBackgroundResource(R.drawable.lightbluegrey_round_rect_6)
            }

            holder.setCategory(secondCategoryItems[position].name)

            holder.itemView.setOnClickListener {
                Logger.d("position : $position")
                onSecondCategoryClicked(secondCategoryItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = secondCategoryItems.size
    }

    //==============================================================================================
    inner class OptionAdapter : RecyclerView.Adapter<CategoryHolder>() {
        var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CategoryHolder(layoutInflater.inflate(R.layout.view_chat_category, parent, false))

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            if (selectedPosition == position) {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.orange))
                holder.itemView.category.setBackgroundResource(R.drawable.orange_round_rect_6)
            } else {
                holder.itemView.category.setTextColor(Helper.readColorRes(R.color.dark_grey))
                holder.itemView.category.setBackgroundResource(R.drawable.lightbluegrey_round_rect_6)
            }

            holder.setCategory(optionItems[position].name)

            holder.itemView.setOnClickListener {
                Logger.d("position : $position")
                onOptionClicked(optionItems[position])
                selectedPosition = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = optionItems.size
    }

    inner class CategoryHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setCategory(_category: String) {
            item.category.text = _category
        }
    }
}