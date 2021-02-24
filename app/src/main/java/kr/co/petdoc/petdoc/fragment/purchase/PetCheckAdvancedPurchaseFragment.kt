package kr.co.petdoc.petdoc.fragment.purchase

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.base.UiState
import kr.co.petdoc.petdoc.databinding.FragmentPetCheckAdvancedPurchaseBinding
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetCheckAdvancedPurchaseViewModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PetCheckAdvancedPurchaseFragment
    : PetdocBaseFragment<FragmentPetCheckAdvancedPurchaseBinding, PetCheckAdvancedPurchaseViewModel>() {

    private val viewModel: PetCheckAdvancedPurchaseViewModel by sharedViewModel()

    override fun getLayoutId(): Int = R.layout.fragment_pet_check_advanced_purchase

    override fun getTargetViewModel(): PetCheckAdvancedPurchaseViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            start()
            uiState.observe(viewLifecycleOwner, Observer {
                if (it == UiState.Loading) {
                    showLoadingDialog()
                } else {
                    dismissLoadingDialog()
                }
            })

            showCreditCardChooser.observe(viewLifecycleOwner, Observer { creditCardList ->
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("카드를 선택해 주세요")
                val cardNames = creditCardList.map { it.name }.toTypedArray()
                builder.setItems(cardNames) { _, which ->
                    viewModel.onCreditCardSelected(creditCardList[which])
                }.create().show()
            })

            showInstallmentsChooser.observe(viewLifecycleOwner, Observer { installmentList ->
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("할부 개월을 선택해 주세요")
                val cardNames = installmentList.map { it.name }.toTypedArray()
                builder.setItems(cardNames) { _, which ->
                    viewModel.onInstallmentSelected(installmentList[which])
                }.create().show()
            })

            startNicePayScreen.observe(viewLifecycleOwner, Observer {
                val direction = PetCheckAdvancedPurchaseFragmentDirections
                    .actionPurchaseFragmentToNicepayFragment(it)
                findNavController().navigate(direction)
            })

            closeNicePayScreen.observe(viewLifecycleOwner, Observer {
                OneBtnDialog(requireContext()).apply {
                    setTitle("결제 실패")
                    setMessage("해당 결제정보를 불러오지 못하였습니다. 다시 시도해주세요.")
                    setConfirmButton(
                        Helper.readStringRes(R.string.confirm),
                        View.OnClickListener {
                            findNavController().navigateUp()
                            dismiss()
                        })
                    show()
                }
            })

            privacyTermsScreen.observe(viewLifecycleOwner, Observer {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://petdoc.co.kr/policy/privacy?isNoTitle=true")))
            })

            serviceTermsScreen.observe(viewLifecycleOwner, Observer {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://statics-assets.s3.ap-northeast-2.amazonaws.com/terms/paymentAgree.html")))
            })

            showToast.observe(viewLifecycleOwner, Observer { message ->
                AppToast(requireContext()).showToastMessage(
                    message,
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            })
        }

        binding.btnClose.setOnClickListener {
            activity?.onBackPressed()
        }
    }


}