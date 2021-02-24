package kr.co.petdoc.petdoc.fragment.purchase

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentNicepayBinding
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.PetCheckAdvancedPurchaseViewModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.koin.android.viewmodel.ext.android.sharedViewModel

class NicePayFragment
    : PetdocBaseFragment<FragmentNicepayBinding, PetCheckAdvancedPurchaseViewModel>() {

    private val viewModel: PetCheckAdvancedPurchaseViewModel by sharedViewModel()

    override fun getLayoutId(): Int = R.layout.fragment_nicepay

    override fun getTargetViewModel(): PetCheckAdvancedPurchaseViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    private val args: NicePayFragmentArgs by navArgs()

    protected lateinit var connectionLiveData: ConnectionLiveData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nicepayWebView.load(args.config)

        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner, Observer { hasInternet ->
            if(hasInternet.not()) {
                OneBtnDialog(requireContext()).apply {
                    setTitle("결제 실패")
                    setMessage("해당 결제정보를 불러오지 못하였습니다. 네트워크 상태를 확인하시고 다시 시도해주세요.")
                    setConfirmButton(
                        Helper.readStringRes(R.string.confirm),
                        View.OnClickListener {
                            findNavController().navigateUp()
                            dismiss()
                        })
                    show()
                }
            }
        })

        viewModel.startPurchaseCompleteScreen.observe(viewLifecycleOwner, Observer {
            moveToCompleteScreen()
        })

        viewModel.closeNicePayScreen.observe(viewLifecycleOwner, Observer {
            findNavController().navigateUp()
        })
    }

    private fun moveToCompleteScreen() {
        val direction = NicePayFragmentDirections
            .actionNicepayFragmentToPurchaseCompleteFragment()
        findNavController().navigate(direction)
    }
}