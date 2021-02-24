package kr.co.petdoc.petdoc.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.domain.HospitalListResponse
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.repository.network.form.InjectionForm
import okhttp3.ResponseBody
import org.koin.core.context.GlobalContext
import retrofit2.Response

interface PetdocRepository {

    /**
     * 펫 정보 가져오기
     * @param userId 사용자 ID
     * @return 사용자 펫정보 리스트
     */
    suspend fun retrieveMyPets(userId: Int): List<PetInfoItem>

    /**
     * 병원 정보
     * @param form 병원 List 요청 form
     * @param dispatcher CoroutineDispatcher
     * @return
     */
    suspend fun fetchHospitalList(
        form: HospitalListForm,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): HospitalListResponse

    /**
     * 스캐너 체온 투약 여부 상태를 변경한다.
     *
     * @param petId 펫 아이디
     * @param form 투약 여부 요청 form
     * @param dispatcher CoroutineDispatcher
     * @return
     */
    suspend fun fetchScannerInjection(
        petId:Int,
        form:List<InjectionForm>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Response<ResponseBody>
}

/**
 * 펫정보 추가, 삭제, 업데이트 부분이 repository 에 아직 작업되지 않아 util 함수로 추가함.
 * 펫 정보의 추가, 삭제, 업데이트, 순서변경이 되는 경우 dirtyFlag 를 reset
 * TODO. 추가, 삭제, 업데이트도 repository 를 통해서 하도록 변경 필요하며 내부에서 dirtyFlag 를 업데이트 하도록 수정
 */
fun resetMyPetsDirty() {
    if (PetdocApplication.application.baseContext.isInternetConnected()) {
        val cache by lazy { GlobalContext.get().koin.get<AppCache>() }
        cache.resetMyPetsDirty()
    }
}