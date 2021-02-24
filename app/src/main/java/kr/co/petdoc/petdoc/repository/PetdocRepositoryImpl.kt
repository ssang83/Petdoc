package kr.co.petdoc.petdoc.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.domain.HospitalListResponse
import kr.co.petdoc.petdoc.event.PetInfoRefreshEvent
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.repository.network.form.InjectionForm
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Response

class PetdocRepositoryImpl(
    private val apiService: PetdocApiService,
    private val cache: AppCache
) : PetdocRepository {

    override suspend fun retrieveMyPets(userId: Int): List<PetInfoItem> {
        if (cache.isMyPetsDirty()) {
            val pets = try {
                withContext(Dispatchers.IO) { apiService.getPetInfoListWithCoroutine(userId) }
            } catch (e: Exception) {
                return emptyList()
            }
            cache.updateMyPets(pets)
            EventBus.getDefault().post(PetInfoRefreshEvent())
            return pets
        } else {
            return cache.getMyPets()
        }
    }

    override suspend fun fetchHospitalList(
        form: HospitalListForm,
        dispatcher: CoroutineDispatcher
    ): HospitalListResponse {
        return withContext(dispatcher) { apiService.postHospitalList(form) }
    }

    override suspend fun fetchScannerInjection(
        petId:Int,
        form: List<InjectionForm>,
        dispatcher: CoroutineDispatcher
    ): Response<ResponseBody> {
        return withContext(dispatcher) { apiService.postInjectionForPet(petId, form)}
    }
}