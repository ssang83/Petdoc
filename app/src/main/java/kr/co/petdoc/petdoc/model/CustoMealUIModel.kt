package kr.co.petdoc.petdoc.model

import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem

data class CustoMealUIModel(
    override val id: Int = 2,
    val items: List<PetInfoItem>
) : PagingItem