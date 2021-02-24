package kr.co.petdoc.petdoc.adapter.shopping

import kr.co.petdoc.petdoc.adapter.PagingAdapterDelegatesManager
import kr.co.petdoc.petdoc.adapter.PagingDelegationAdapter
import kr.co.petdoc.petdoc.model.PagingItem

class ShopAdapter
    : PagingDelegationAdapter<PagingItem>(PagingAdapterDelegatesManager()) {

    init {
        with(delegatesManager) {
            addAdapterDelegate(BannerDelegate())
            addAdapterDelegate(CustoMealDelegate())
            addAdapterDelegate(GodoMallProductDelegate())
        }
    }

}