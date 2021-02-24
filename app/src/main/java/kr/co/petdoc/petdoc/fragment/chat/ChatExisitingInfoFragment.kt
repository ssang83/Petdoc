package kr.co.petdoc.petdoc.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_exisiting_chat_item.view.*
import kotlinx.android.synthetic.main.adapter_exisiting_magazine_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_info.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.activity.chat.ChatSearchDetailActivity
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.LegacyChatItem
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ChatDataModel
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatExisitingInfoFragment
 * Created by kimjoonsung on 2020/05/20.
 *
 * Description :
 */
class ChatExisitingInfoFragment : BaseFragment() {

    private val LOG_TAG = "ChatExisitingInfoFragment"
    private val LEGACY_CHAT_LIST_REQUEST = "${LOG_TAG}.legacyChatListRequest"
    private val LEGACY_CHAT_TOTAL_COUNT_REQUEST = "${LOG_TAG}.legacyChatTotalCountRequest"
    private val LEGACY_MAGAZINE_LIST_REQUEST = "${LOG_TAG}.legacyMagazineListRequest"
    private val MAGAZINE_TOTAL_COUNT_REQUEST = "${LOG_TAG}.magazineTotalCountRequest"

    private lateinit var dataModel: ChatDataModel
    private lateinit var chatAdapter:ExisitingChatAdapter
    private lateinit var magazineAdapter:ExisitingMagazineAdapter

    private var exisitingChatItems: MutableList<LegacyChatItem> = mutableListOf()
    private var exisitingMagazineItems: MutableList<MagazineItem> = mutableListOf()

    private var margin20 = 0
    private var margin7 = 0
    private var margin15 = 0

    private var petKind = ""
    private var categoryId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(ChatDataModel::class.java)
        return inflater.inflate(R.layout.fragment_chat_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        margin15 = Helper.convertDpToPx(requireContext(), 15f).toInt()
        margin20 = Helper.convertDpToPx(requireContext(), 20f).toInt()
        margin7 = Helper.convertDpToPx(requireContext(), 7f).toInt()

        //=============================================================================================
        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }

        layoutGuide.setOnSingleClickListener {
            OneBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.chat_info_guide))
                setMessage(Helper.readStringRes(R.string.chat_info_guide_detail))
                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                    dismiss()
                })
                show()
            }
        }

        btnComplete.setOnSingleClickListener {
            findNavController().navigate(ChatExisitingInfoFragmentDirections.actionChatExisitingInfoFragmentToChatRequestFragment())
        }

        layoutChatMore.setOnSingleClickListener {
            findNavController().navigate(ChatExisitingInfoFragmentDirections.actionChatExisitingInfoFragmentToExisitingChatFragment())
        }

        layoutMagazineMore.setOnSingleClickListener {
            findNavController().navigate(ChatExisitingInfoFragmentDirections.actionChatExisitingInfoFragmentToExisitingMagazineFragment())
        }

        //==========================================================================================
        chatAdapter = ExisitingChatAdapter()
        chatList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = chatAdapter
        }

        magazineAdapter = ExisitingMagazineAdapter()
        magazineList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = magazineAdapter
        }
        //==========================================================================================
        scrollView.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                if (scrollView != null) {
                    if (!scrollView.canScrollVertically(1)) { // bottom of scroll view
                        btnComplete.setTextColor(Helper.readColorRes(R.color.white))
                        btnComplete.isEnabled = true
                     } else {
                        btnComplete.setTextColor(Helper.readColorRes(R.color.white_alpha))
                        btnComplete.isEnabled = false
                    }
                }
            }
        })

        //==========================================================================================
        petKind = dataModel.petInfo.value?.kind!!
        categoryId = dataModel.category.value?.pk.toString()

        Logger.d("kind : $petKind, categoryId : $categoryId")

        ChatLegacyDialogFragment().apply {
            val bundle = Bundle().apply {
                putString("petKind", petKind)
                putString("category", dataModel.category.value?.name!!)
            }
            arguments = bundle
        }.show(childFragmentManager, "Chat")

        loadData()

        setPetType(petKind)
        setCategoryType(dataModel.category.value?.name!!)

        btnComplete.isEnabled = false
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(LEGACY_CHAT_LIST_REQUEST)) {
            mApiClient.cancelRequest(LEGACY_CHAT_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(LEGACY_CHAT_TOTAL_COUNT_REQUEST)) {
            mApiClient.cancelRequest(LEGACY_CHAT_TOTAL_COUNT_REQUEST)
        }

        if (mApiClient.isRequestRunning(LEGACY_MAGAZINE_LIST_REQUEST)) {
            mApiClient.cancelRequest(LEGACY_MAGAZINE_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_TOTAL_COUNT_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_TOTAL_COUNT_REQUEST)
        }

        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            LEGACY_CHAT_LIST_REQUEST -> {
                if (event.status == "ok") {
                    exisitingChatItems = Gson().fromJson(event.response, object : TypeToken<MutableList<LegacyChatItem>>() {}.type)

                    layoutChatMore.apply {
                        if (exisitingChatItems.size >= 10) {
                            visibility = View.VISIBLE
                        } else {
                            visibility = View.GONE
                        }
                    }

                    chatAdapter.notifyDataSetChanged()

                    updateBtnUI()
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }

            LEGACY_CHAT_TOTAL_COUNT_REQUEST -> {
                if (event.status == "ok") {
                    chatCount.text = event.response

                    if (event.response == "0") {
                        chatEmpty.visibility = View.VISIBLE
                        chatList.visibility = View.GONE
                        layoutChatMore.visibility = View.GONE
                    } else {
                        chatEmpty.visibility = View.GONE
                        chatList.visibility = View.VISIBLE
                    }
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }

            MAGAZINE_TOTAL_COUNT_REQUEST -> {
                if (event.status == "ok") {
                    try {
                        val code = JSONObject(event.response)["code"]
                        if ("SUCCESS" == code) {
                            val count = JSONObject(event.response)["resultData"]

                            magazineCount.text = count.toString()

                            if (count == 0) {
                                magazineList.visibility = View.GONE
                                magazineEmpty.visibility = View.VISIBLE
                                layoutMagazineMore.visibility = View.GONE
                            } else {
                                magazineList.visibility = View.VISIBLE
                                magazineEmpty.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            LEGACY_MAGAZINE_LIST_REQUEST -> {
                if (response is MagazineListRespone) {
                    exisitingMagazineItems = response.resultData

                    layoutMagazineMore.apply {
                        if (exisitingMagazineItems.size >= 10) {
                            visibility = View.VISIBLE
                        } else {
                            visibility = View.GONE
                        }
                    }

                    magazineAdapter.notifyDataSetChanged()

                    updateBtnUI()
                }
            }
        }
    }

    private fun updateBtnUI() {
        if (exisitingChatItems.size == 0 && exisitingMagazineItems.size == 0) {
            btnComplete.apply {
                isEnabled = true
                text = Helper.readStringRes(R.string.chat_request_do)
                setTextColor(Helper.readColorRes(R.color.white))
            }
        } else {
            btnComplete.text = Helper.readStringRes(R.string.chat_info_confirm_completed)
        }
    }

    private fun loadData() {
        retryIfNetworkAbsent {
            if (!mApiClient.isRequestRunning(LEGACY_CHAT_LIST_REQUEST)) {
                mApiClient.getLegacyChatList(LEGACY_CHAT_LIST_REQUEST, petKind, categoryId, "recommendCount", 10, 0)
            }

            if (!mApiClient.isRequestRunning(LEGACY_CHAT_TOTAL_COUNT_REQUEST)) {
                mApiClient.getLegacyChatTotalCount(LEGACY_CHAT_TOTAL_COUNT_REQUEST, petKind, categoryId)
            }

            if (!mApiClient.isRequestRunning(LEGACY_MAGAZINE_LIST_REQUEST)) {
                mApiClient.getLegacyMagazineList(LEGACY_MAGAZINE_LIST_REQUEST, categoryId, "recommendCount", 10, 0)
            }

            if (!mApiClient.isRequestRunning(MAGAZINE_TOTAL_COUNT_REQUEST)) {
                mApiClient.getMagazineTotalCount(MAGAZINE_TOTAL_COUNT_REQUEST, categoryId)
            }
        }
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

    private fun onExisitingChatDetail(item: LegacyChatItem) {
        startActivity(Intent(requireActivity(), ChatSearchDetailActivity::class.java).apply {
            putExtra("chatId", item.id)
        })
    }

    private fun onExistingMagazineDetail(item: MagazineItem) {
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
    }

    //==============================================================================================
    inner class ExisitingChatAdapter : RecyclerView.Adapter<ExisitingChatHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ExisitingChatHolder(layoutInflater.inflate(R.layout.adapter_exisiting_chat_item, parent, false))

        override fun onBindViewHolder(holder: ExisitingChatHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin15
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin15
                    leftMargin = 0
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = 0
                    rightMargin = 0
                }
            }

            val createdAt = exisitingChatItems[position].createdAt.split("T")
            val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            val format = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val date = format1.parse(createdAt[0].replace("-", ""))
            val regDate = format.format(date)

            holder.setDate(regDate)
            holder.setContent(exisitingChatItems[position].counselRequestText)
            holder.setRecommendCount(exisitingChatItems[position].recommendCount.toString())

            holder.itemView.setOnSingleClickListener {
                onExisitingChatDetail(exisitingChatItems[position])
            }
        }

        override fun getItemCount() = exisitingChatItems.size
    }

    inner class ExisitingChatHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setDate(_date: String) {
            item.regDate.text = _date
        }

        fun setContent(_content: String) {
            item.chatContent.text = _content
        }

        fun setRecommendCount(_count: String) {
            item.recommendCount.text = "추천 ${_count}"
        }
    }

    //===============================================================================================
    inner class ExisitingMagazineAdapter : RecyclerView.Adapter<ExisitingMagazineHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ExisitingMagazineHolder(layoutInflater.inflate(R.layout.adapter_exisiting_magazine_item, parent, false))

        override fun onBindViewHolder(holder: ExisitingMagazineHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin20
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin20
                    leftMargin = margin7
                }
            } else {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin7
                    rightMargin = 0
                }
            }

            holder.setImage(exisitingMagazineItems[position].titleImage)
            holder.setCategory(exisitingMagazineItems[position].categoryNm)
            holder.setTitle(exisitingMagazineItems[position].title)
            holder.setLikeCount(exisitingMagazineItems[position].likeCount.toString())

            holder.itemView.setOnSingleClickListener {
                onExistingMagazineDetail(exisitingMagazineItems[position])
            }
        }

        override fun getItemCount() = exisitingMagazineItems.size
    }

    inner class ExisitingMagazineHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            Glide.with(requireContext())
                .load( if(url.startsWith("http")) url else "https://s3.ap-northeast-2.amazonaws.com/elasticbeanstalk-ap-northeast-2-176213403491/media/$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.magazineImg)
        }

        fun setCategory(_category: String) {
            item.category.text = _category
        }

        fun setTitle(_title: String) {
            item.title.text = _title
        }

        fun setLikeCount(_count: String) {
            item.likeCnt.text = _count
        }
    }
}