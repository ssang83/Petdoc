package kr.co.petdoc.petdoc.repository.local.cache

import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem

class AppCacheImpl : AppCache {
    private var isMyPetsDirty = true
    private var cachedPets: List<PetInfoItem> = mutableListOf()

    override fun isMyPetsDirty(): Boolean {
        return isMyPetsDirty
    }

    override fun resetMyPetsDirty() {
        isMyPetsDirty = true
    }

    override fun getMyPets(): List<PetInfoItem> {
        return cachedPets
    }

    override fun updateMyPets(pets: List<PetInfoItem>) {
        cachedPets = pets
        isMyPetsDirty = false
    }

    override fun clear() {
        isMyPetsDirty = true
        cachedPets = emptyList()
    }
}