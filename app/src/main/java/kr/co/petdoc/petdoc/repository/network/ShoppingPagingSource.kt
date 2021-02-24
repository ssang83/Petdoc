package kr.co.petdoc.petdoc.repository.network

import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.model.CustoMealUIModel
import kr.co.petdoc.petdoc.model.ShopProductUIModel
import kr.co.petdoc.petdoc.model.ShopBannerUIModel
import kr.co.petdoc.petdoc.model.PagingItem
import kr.co.petdoc.petdoc.model.ShoppingSection.BannerSection
import kr.co.petdoc.petdoc.model.ShoppingSection.CustoMealSection
import kr.co.petdoc.petdoc.model.ShoppingSection.ProductSection
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import java.lang.Exception

private const val STARTING_PAGE_INDEX = 1
private const val SECTION_SIZE = 3

class ShoppingPagingSource(
    private val repository: PetdocRepository,
    private val apiService: PetdocApiService,
    private val persistentPrefs: PersistentPrefs
) : PagingSource<Int, PagingItem>() {
    private val sections = listOf(
        BannerSection,
        CustoMealSection,
        ProductSection
    )

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PagingItem> {
        val page = params.key ?: STARTING_PAGE_INDEX
        return try {
            val viewType = if (page <= (SECTION_SIZE - 1)) {
                sections[page - 1]
            } else {
                ProductSection
            }
            val models = when(viewType) {
                BannerSection -> { loadBanner() }
                CustoMealSection -> { loadCustoMeal() }
                ProductSection -> { loadProducts(page - 2) }
            }

            LoadResult.Page(
                data = models,
                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if ((viewType is ProductSection) && models.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun loadBanner(): List<PagingItem> {
        return try {
            val response = withContext(Dispatchers.IO) { apiService.getBannerListWithCoroutine("shop") }
            listOf(
                ShopBannerUIModel(
                    items = response.bannerList
                )
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun loadCustoMeal(): List<PagingItem> {
        val oldUserId = persistentPrefs.getValue(AppConstants.PREF_KEY_OLD_USER_ID, "")
        val userId = if(oldUserId.isNotEmpty()) {
            oldUserId.toInt()
        } else {
            0
        }
        return try {
            val response = repository.retrieveMyPets(userId)
            listOf(
                CustoMealUIModel(
                    items = response
                )
            )
        } catch (e: Exception) {
            listOf(
                CustoMealUIModel(
                    items = emptyList()
                )
            )
        }
    }

    private suspend fun loadProducts(page: Int): List<PagingItem> {
        return try {
            val response = apiService.getRecommendProductListWithCoroutine(page = page, limit = 100)
            return response.resultData.mapIndexed { index, product ->
                ShopProductUIModel(
                    isFirstPosition = ((page == 1) && (index == 0)),
                    item = product
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}