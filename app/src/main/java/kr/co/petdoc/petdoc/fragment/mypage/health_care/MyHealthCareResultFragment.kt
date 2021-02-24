package kr.co.petdoc.petdoc.fragment.mypage.health_care

import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_my_health_care_result.*
import kotlinx.android.synthetic.main.layout_health_care_bottom.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.databinding.FragmentMyHealthCareResultBinding
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.MyHealthCareResultViewModel
import kr.co.petdoc.petdoc.viewmodel.MyPageInformationModel
import kr.co.petdoc.petdoc.widget.BottomSheetViewPager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Petdoc
 * Class: MyHealthCareResultFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
const val HEALTH_CARE_RESULT_CONFIG = "myHealthCareConfig"

@Parcelize
data class MyHeathCareResultConfig(
    val bookingId: Int,
    val petName: String,
    val date: String,
    val petImgUrl: String?
) : Parcelable

class MyHealthCareResultFragment : PetdocBaseFragment<FragmentMyHealthCareResultBinding, MyHealthCareResultViewModel>() {

    //private lateinit var dataModel : MyPageInformationModel
    private val config by lazy {
        arguments?.getParcelable<MyHeathCareResultConfig>(HEALTH_CARE_RESULT_CONFIG)
    }

    private val viewModel: MyHealthCareResultViewModel by viewModel { parametersOf(config) }

    override fun getTargetViewModel(): MyHealthCareResultViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_my_health_care_result

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.start()

        setUpViews()
    }

    private fun setUpViews() {
        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        layoutInfo.setOnClickListener { HealthCareInfoDialogFragment().show(childFragmentManager, HealthCareInfoDialogFragment().tag) }

        name.text = config?.petName
        date.text = config?.date?.replace("-", ".")
        petImage.apply {
            when {
                config?.petImgUrl.isNullOrEmpty().not() -> {
                    val imageUrl = config?.petImgUrl?:""
                    Glide.with(requireContext())
                        .load(if (imageUrl.startsWith("http")) imageUrl else "${AppConstants.IMAGE_URL}${imageUrl}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(petImage)
                }
                else -> {
                    Glide.with(requireContext())
                        .load(R.drawable.img_pet_profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(petImage)
                }
            }
        }

        bottomSheetPager.apply {
            adapter = BottomSheetPagerAdapter(childFragmentManager)
            setSwipeEnable(false)
        }

        setTabLayoutFont()
    }

    private fun setTabLayoutFont() {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount

        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    tabViewChild.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/notosanskr_bold.otf")
                }
            }
        }
    }

    //===============================================================================================
    class BottomSheetPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val mFragmentList: MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(BloodTestResultFragment())
                add(AllergyTestResultFragment())
            }
        }

        override fun getCount() = mFragmentList.size
        override fun getItem(position: Int) = mFragmentList[position]
        override fun getPageTitle(position: Int) =
            when (position) {
                0 -> "혈액 검사"
                else -> "알러지 검사"
            }

    }
}