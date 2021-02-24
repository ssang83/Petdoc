package kr.co.petdoc.petdoc.adapter.petcheckresult

import kr.co.petdoc.petdoc.adapter.AdapterDelegatesManager
import kr.co.petdoc.petdoc.adapter.DelegationAdapter
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultFooter
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection
import kr.co.petdoc.petdoc.viewmodel.MyHealthCareResultViewModel

class PetCheckBloodResultAdapter(
    private val viewModel: MyHealthCareResultViewModel
) : DelegationAdapter<PetCheckResultSection>(AdapterDelegatesManager(), emptyList()) {
    init {
        with(delegatesManager) {
            addAdapterDelegate(BloodResultHeaderDelegate())
            addAdapterDelegate(BloodWarningDelegate(viewModel))
            addAdapterDelegate(BloodResultAboutDelegate())
            addAdapterDelegate(BloodResultDetailHeaderDelegate())
            addAdapterDelegate(BloodResultDetailItemDelegate())
            addAdapterDelegate(BloodResultFooterDelegate())
        }
    }
}