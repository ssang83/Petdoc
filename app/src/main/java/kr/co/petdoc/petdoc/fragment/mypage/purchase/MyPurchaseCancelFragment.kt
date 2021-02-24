package kr.co.petdoc.petdoc.fragment.mypage.purchase

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_purchase_cancel.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentPurchaseCancelBinding
import kr.co.petdoc.petdoc.nicepay.OrderCancel
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseCancelViewModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Petdoc
 * Class: MyPurchaseCancelFragment
 * Created by kimjoonsung on 2/8/21.
 *
 * Description :
 */
const val CANCEL_ORDER_ID = "order_id"

class MyPurchaseCancelFragment: PetdocBaseFragment<FragmentPurchaseCancelBinding, MyPurchaseCancelViewModel>() {

    private val orderId by lazy {
        arguments?.getString(CANCEL_ORDER_ID)
            ?: throw IllegalStateException("orderId must set for starting MyPurchaseCancelFragment")
    }

    private val viewModel: MyPurchaseCancelViewModel by viewModel {
        parametersOf(orderId)
    }

    override fun getTargetViewModel(): MyPurchaseCancelViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_purchase_cancel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            start()

            showCancelToast.observe(viewLifecycleOwner, Observer { message ->
                AppToast(requireContext()).showToastMessage(
                    message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )

                EventBus.getDefault().post("purchase_cancel")
                requireActivity().onBackPressed()
            })

            etcReason.observe(viewLifecycleOwner, Observer { text ->
                if (cancelReason.value.toString() == OrderCancel.ETC.name) {
                    binding.btnCancel.apply {
                        when {
                            text.isEmpty() -> {
                                setBackgroundResource(R.drawable.bg_purchase_cancel_disable)
                                isEnabled = false
                            }
                            else -> {
                                setBackgroundResource(R.drawable.bg_white_rect)
                                isEnabled = true
                            }
                        }
                    }

                    binding.tvCancel.apply {
                        when {
                            text.isEmpty() -> setTextColor(Helper.readColorRes(R.color.light_grey3))
                            else -> setTextColor(Helper.readColorRes(R.color.orange))
                        }
                    }
                }
            })
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
    }
}