package kr.co.petdoc.petdoc.adapter.petcheckresult

import kr.co.petdoc.petdoc.adapter.AdapterDelegatesManager
import kr.co.petdoc.petdoc.adapter.DelegationAdapter
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class PetCheckAllergyResultAdapter
    : DelegationAdapter<PetCheckResultSection>(AdapterDelegatesManager(), emptyList()) {
    init {
        with(delegatesManager) {
            addAdapterDelegate(AllergyResultHeaderDelegate())
            addAdapterDelegate(AllergyClassDelegate())
            addAdapterDelegate(AllergyResultAboutDelegate())
            addAdapterDelegate(AllergyResultDetailHeaderDelegate())
            addAdapterDelegate(AllergyResultDetailItemDelegate())
        }
    }
}