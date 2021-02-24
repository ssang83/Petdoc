package kr.co.petdoc.petdoc.fragment.mypage.purchase

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_purchase_detail.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.EXTRA_ALLERGY_COMMENT_CATEGORY
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentPurchaseDetailBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseDetailViewModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Petdoc
 * Class: MyPurchaseDetailFragment
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
const val ORDER_ID = "order_id"

class MyPurchaseDetailFragment: PetdocBaseFragment<FragmentPurchaseDetailBinding, MyPurchaseDetailViewModel>() {
    private val orderId by lazy {
        arguments?.getString(ORDER_ID)
            ?: throw IllegalStateException("orderId must set for starting MyPurchaseDetailFragment")
    }

    private val viewModel: MyPurchaseDetailViewModel by viewModel {
        parametersOf(orderId)
    }

    override fun getTargetViewModel(): MyPurchaseDetailViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_purchase_detail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            start()

            moveToCancel.observe(viewLifecycleOwner, Observer { orderId ->
                bundleOf(CANCEL_ORDER_ID to orderId).let {
                    findNavController().navigate(R.id.action_myPurchaseDetailFragment_to_myPurchaseCancelFragment, it)
                }
            })

            moveToPetCheck.observe(viewLifecycleOwner, Observer {
                requireContext().startActivity<HealthCareActivity>()
            })
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
    }
}