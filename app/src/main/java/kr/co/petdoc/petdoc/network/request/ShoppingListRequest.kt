package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ShoppingListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ShopListRequest
 * Created by kimjoonsung on 2020/04/23.
 *
 * Description :
 */
class ShoppingListRequest(apiService: ApiService, tag:String, _page:Int) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ShoppingListResponse>

    private lateinit var call: Call<ShoppingListResponse>

    private var page = _page

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getShoppingList(page)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}