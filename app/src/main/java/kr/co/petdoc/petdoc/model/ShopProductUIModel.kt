package kr.co.petdoc.petdoc.model

import kr.co.petdoc.petdoc.domain.GodoMallProduct

data class ShopProductUIModel(
    override val id: Int = 3,
    val isFirstPosition: Boolean = false,
    val item: GodoMallProduct
) : PagingItem