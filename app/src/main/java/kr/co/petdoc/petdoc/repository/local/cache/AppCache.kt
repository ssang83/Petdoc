package kr.co.petdoc.petdoc.repository.local.cache

import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem

interface AppCache {
    fun isMyPetsDirty(): Boolean
    fun resetMyPetsDirty()
    fun getMyPets(): List<PetInfoItem>
    fun updateMyPets(pets: List<PetInfoItem>)
    fun clear()
}