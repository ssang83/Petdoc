package kr.co.petdoc.petdoc.fragment.purchase

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentPurchaseCompleteBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.viewmodel.PetCheckAdvancedPurchaseViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PurchaseCompleteFragment
    : PetdocBaseFragment<FragmentPurchaseCompleteBinding, PetCheckAdvancedPurchaseViewModel>() {

    private val viewModel: PetCheckAdvancedPurchaseViewModel by sharedViewModel()

    override fun getLayoutId(): Int = R.layout.fragment_purchase_complete

    override fun getTargetViewModel(): PetCheckAdvancedPurchaseViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnClose.setOnClickListener { requireActivity().finish() }
            btnGoHome.setOnClickListener { goToHome() }
            btnHistory.setOnClickListener {
                goToPurchaseHistory()
            }
            btnGoToPetCheckAdvanced.setOnClickListener {
                findNavController().navigate(
                    PurchaseCompleteFragmentDirections.actionPurchaseCompleteFragmentToHealthCareCodeFragment()
                )
            }
        }
    }

    private fun goToPurchaseHistory() {
        requireContext().startActivity<MyPageActivity> {
            putExtra("fromPurchase", true)
        }

        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }

    private fun goToHome() {
        requireContext().startActivity<MainActivity> {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("booking", "purchase")
        }

        activity?.finish()
    }
}