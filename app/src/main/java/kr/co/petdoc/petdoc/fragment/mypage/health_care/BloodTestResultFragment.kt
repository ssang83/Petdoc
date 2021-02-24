package kr.co.petdoc.petdoc.fragment.mypage.health_care

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_blood_test_result.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentBloodTestResultBinding
import kr.co.petdoc.petdoc.extensions.fadeOutAndHideImage
import kr.co.petdoc.petdoc.viewmodel.MyHealthCareResultViewModel
import org.koin.android.viewmodel.ext.android.getViewModel

class BloodTestResultFragment
    : PetdocBaseFragment<FragmentBloodTestResultBinding, MyHealthCareResultViewModel>() {
    private var handler : Handler? = null
    private val viewModel: MyHealthCareResultViewModel by lazy { requireParentFragment().getViewModel<MyHealthCareResultViewModel>() }

    override fun getTargetViewModel(): MyHealthCareResultViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_blood_test_result

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            quickSearchBloodListPosition.observe(viewLifecycleOwner, Observer { moveToPosition ->
                (bloodResultList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(moveToPosition, 0)
            })
        }

        setUpViews()
    }

    override fun onResume() {
        super.onResume()
        if (handler?.hasMessages(0) == false) {
            handler?.sendEmptyMessageDelayed(0, 10000)
        }
    }

    override fun onPause() {
        super.onPause()
        handler?.removeMessages(0)
    }

    private fun setUpViews() {
        btnTop.setOnClickListener { bloodResultList.scrollToPosition(0) }
        btnBooking.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", "hospital")
            })

            requireActivity().finish()
        }

        handler = Handler{
            guideImg.fadeOutAndHideImage()
            handler?.sendEmptyMessageDelayed(0, 10000)
            handler?.removeMessages(0)
            true
        }
    }
}