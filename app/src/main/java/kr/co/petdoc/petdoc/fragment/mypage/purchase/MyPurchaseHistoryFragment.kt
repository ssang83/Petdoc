package kr.co.petdoc.petdoc.fragment.mypage.purchase

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_my_purchase_history.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentMyPurchaseHistoryBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.extensions.startActivityForResult
import kr.co.petdoc.petdoc.nicepay.OrderStatus
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseHistoryModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Petdoc
 * Class: MyPurchaseHistoryFragment
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
const val REQUEST_RE_PURCHASE = 0x1000

class MyPurchaseHistoryFragment: PetdocBaseFragment<FragmentMyPurchaseHistoryBinding, MyPurchaseHistoryModel>() {

    private val viewModel: MyPurchaseHistoryModel by viewModel()

    override fun getTargetViewModel(): MyPurchaseHistoryModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_my_purchase_history

    private lateinit var mEventBus: EventBus

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEventBus = EventBus.getDefault()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        with(viewModel) {
            start(false)

            moveToPurchaseDetail.observe(viewLifecycleOwner, Observer { item ->
                if (item.orderStatus == OrderStatus.NET_CANCELED.name) {
                    OneBtnDialog(requireContext()).apply {
                        setTitle("결제 내역 실패")
                        setMessage("결제가 정상적으로 이뤄지지 않았습니다. 재구매 부탁드립니다.")
                        setCancelable(true)
                        setConfirmButton("확인", View.OnClickListener {
                            dismiss()
                        })
                    }.show()
                } else {
                    bundleOf(ORDER_ID to item.orderId.toString()).let {
                        findNavController().navigate(
                            R.id.action_myPurchaseHistoryFragment_to_myPurchaseDetailFragment,
                            it
                        )
                    }
                }
            })

            moveToPurchaseCancel.observe(viewLifecycleOwner, Observer { item ->
                bundleOf(CANCEL_ORDER_ID to item.orderId.toString()).let {
                    findNavController().navigate(R.id.action_myPurchaseHistoryFragment_to_myPurchaseCancelFragment, it)
                }
            })

            moveToPetCheck.observe(viewLifecycleOwner, Observer {
                requireActivity().startActivityForResult<HealthCareActivity>(REQUEST_RE_PURCHASE, {
                    putExtra("fromPurchase", true)
                })
            })

            showDeleteChooser.observe(viewLifecycleOwner, Observer { item ->
                if (item.authKeyStatus != 3 && item.orderStatus == OrderStatus.COMPLETED.name) {
                    OneBtnDialog(requireContext()).apply {
                        setTitle("결제 내역 삭제")
                        setMessage("결제상품 이용이 확인되지 않아 내역삭제가 불가능 합니다.")
                        setConfirmButton("확인", View.OnClickListener {
                            dismiss()
                        })
                    }.show()
                } else {
                    TwoBtnDialog(requireContext()).apply {
                        setTitle("결제 내역 삭제")
                        setMessage("내역 삭제 시 앞으로 결제 내역에서 조회할수 없습니다. 삭제하시겠습니까?")
                        setConfirmButton("삭제", View.OnClickListener {
                            historyDelete(item.orderId)
                            dismiss()
                        })
                        setCancelButton("취소", View.OnClickListener {
                            dismiss()
                        })
                    }.show()
                }
            })

            showToast.observe(viewLifecycleOwner, Observer { message ->
                AppToast(requireContext()).showToastMessage(
                    message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            })
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_RE_PURCHASE -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.start(true)
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: String) {
        if (data == "purchase_cancel") {
            viewModel.start(true)
        }
    }
}