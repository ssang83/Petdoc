package kr.co.petdoc.petdoc.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_exisiting_magazine.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.adapter.home.MagazineAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ChatDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ExisitingMagazineFragment
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class ExisitingMagazineFragment : BaseFragment(), MagazineAdapter.AdapterListener {

    private val LOGTAG = "ExisitingMagazineFragment"
    private val MAGAZINE_LIST_REQUEST = "$LOGTAG.magazineListRequest"

    private lateinit var dataModel: ChatDataModel
    private lateinit var magazineAdapter:MagazineAdapter

    var page = 0
    private val pageOffset = 20
    var isReload = false
    var isEndofData = false

    private var categoryId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(ChatDataModel::class.java)
        return inflater.inflate(R.layout.fragment_exisiting_magazine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnTop.setOnClickListener {
            magazineList.scrollToPosition(0)
            appBar.setExpanded(true, true)
        }

        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        //==========================================================================================
        magazineAdapter = MagazineAdapter().apply { setListener(this@ExisitingMagazineFragment) }
        magazineList.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)

            adapter = magazineAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // 리스트 최 하단 체크한 후 API 호출
                    if (!isEndofData && !magazineList.canScrollVertically(1)) {
                        isReload = true
                        ++page

                        mApiClient.getLegacyMagazineList(MAGAZINE_LIST_REQUEST, categoryId, "createdAt", pageOffset, page*pageOffset)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    (layoutManager as GridLayoutManager).apply{
                        val visibleCount = findFirstVisibleItemPosition()
                        if(visibleCount > 10) {
                            btnTop.visibility = View.VISIBLE
                        } else {
                            btnTop.visibility = View.GONE
                        }
                    }
                }
            })
        }

        categoryId = dataModel.category.value?.pk.toString()

        Logger.d("categoryId : $categoryId")

        mApiClient.getLegacyMagazineList(MAGAZINE_LIST_REQUEST, categoryId, "createdAt", pageOffset, page*pageOffset)

        setPetType(dataModel.petInfo.value?.kind!!)
        setCategoryType(dataModel.category.value?.name!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(MAGAZINE_LIST_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_LIST_REQUEST)
        }
    }

    override fun onMagazineItemClicked(item: MagazineItem) {
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * TODO
     *
     * @param data
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            MAGAZINE_LIST_REQUEST -> {
                if (response is MagazineListRespone) {
                    Logger.d("magazine size : ${response.resultData.size}")
                    isEndofData = response.resultData.size < pageOffset

                    if (!isReload) {
                        magazineAdapter.setData(response.resultData)
                    } else {
                        magazineAdapter.addData(response.resultData)
                    }
                }
            }
        }

    }

    /**
     * EventBus listener. An API call failed. No error message was returned.
     *
     * @param event ApiErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorEvent) {
        Logger.p(event.throwable)
    }

    /**
     * EventBus listener. An API call failed. An error message was returned.
     *
     * @param event ApiErrorWithMessageEvent Contains the error message.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorWithMessageEvent) {
        Logger.d("error : ${event.resultMsgUser}")
    }

    private fun setPetType(kind: String) {
        when (kind) {
            "강아지" -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_dog)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_dog)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_dog))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_dog))
            }

            "고양이" -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_cat)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_cat)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_cat))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_cat))
            }

            else -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_extra)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_extra)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_extra))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_extra))
            }
        }
    }

    private fun setCategoryType(category: String) {
        when (category) {
            "행동" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_behavior)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_behavior)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_behavior))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_behavior))
            }

            "수술" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_surgery)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_surgery)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_surgery))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_surgery))
            }

            "영양" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_nutrition)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_nutrition)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_nutrition))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_nutrition))
            }

            "예방접종" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_shot)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_shot)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_shot))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_shot))
            }

            "질병" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_disease)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_disease)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_disease))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_disease))
            }

            "한방/재활" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_oriental)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_oriental)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_oriental))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_oriental))
            }

            else -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_etc)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_etc)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_etc))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_etc))
            }
        }
    }

    //==============================================================================================
}