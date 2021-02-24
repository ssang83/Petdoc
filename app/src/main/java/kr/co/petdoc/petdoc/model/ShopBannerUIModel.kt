package kr.co.petdoc.petdoc.model

import kr.co.petdoc.petdoc.network.response.submodel.BannerItem

data class ShopBannerUIModel(
    override val id: Int = 1,
    val items: List<BannerItem>
) : PagingItem