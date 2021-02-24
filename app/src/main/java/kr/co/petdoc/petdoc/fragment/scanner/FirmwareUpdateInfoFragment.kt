package kr.co.petdoc.petdoc.fragment.scanner

import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentFirmwareUpdateInfoBinding
import kr.co.petdoc.petdoc.viewmodel.ExternalDeviceDetailViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FirmwareUpdateInfoFragment : PetdocBaseFragment<FragmentFirmwareUpdateInfoBinding, ExternalDeviceDetailViewModel>() {
    private val viewModel: ExternalDeviceDetailViewModel by sharedViewModel()

    override fun getTargetViewModel(): ExternalDeviceDetailViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_firmware_update_info

}
