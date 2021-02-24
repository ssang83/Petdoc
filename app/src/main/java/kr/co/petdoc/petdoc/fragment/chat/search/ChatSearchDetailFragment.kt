package kr.co.petdoc.petdoc.fragment.chat.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat_search_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.chat.SearchChatAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatDetailResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ChatSearchDetailFragment
 * Created by kimjoonsung on 2020/05/29.
 *
 * Description :
 */
class ChatSearchDetailFragment : BaseFragment() {

    private val LOGTAG = "ChatQuitActivity"
    private val MESSAGE_LIST_REQUEST = "${LOGTAG}.messageListRequest"
    private val RECOMMEND_REQUEST = "${LOGTAG}.recommendRequest"
    private val RECOMMEND_CANCEL_REQUEST = "${LOGTAG}.recommendCancelRequest"

    private lateinit var messageAdapter: SearchChatAdapter

    private var chatPk = -1
    private var shareUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_chat_search_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        chatPk = arguments?.getInt("chatId", chatPk)!!

        btnRecommend.setOnClickListener {
            if (!it.isSelected) {
                mApiClient.postChatRoomRecommend(RECOMMEND_REQUEST, chatPk)
            } else {
                mApiClient.deleteChatRoomRecommend(RECOMMEND_CANCEL_REQUEST, chatPk)
            }
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        //==========================================================================================
        messageAdapter = SearchChatAdapter(requireContext())
        messageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }

        mApiClient.getChatSearchDetail(MESSAGE_LIST_REQUEST, chatPk)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(MESSAGE_LIST_REQUEST)) {
            mApiClient.cancelRequest(MESSAGE_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(RECOMMEND_CANCEL_REQUEST)) {
            mApiClient.cancelRequest(RECOMMEND_CANCEL_REQUEST)
        }

        if (mApiClient.isRequestRunning(RECOMMEND_REQUEST)) {
            mApiClient.cancelRequest(RECOMMEND_REQUEST)
        }

        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            MESSAGE_LIST_REQUEST -> {
                if (response is ChatDetailResponse) {
                    messageAdapter.setPartnerItem(response.chatDetail.partner!!)
                    messageAdapter.setItems(response.chatDetail.messages.reversed())

                    petSpecies.text = response.chatDetail.pet.species
                    petBirth.text = response.chatDetail.pet.birthDay.replace("-", "").let {
                        it.substring(2)
                    }
                    petGender.apply {
                        when {
                            response.chatDetail.pet.gender == "man" -> text = "남아"
                            else -> text = "여아"
                        }
                    }

                    shareUrl = response.chatDetail.webUrl
                    btnRecommend.isSelected = response.chatDetail.is_recommend
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            RECOMMEND_REQUEST -> {
                if (event.status == "ok") {
                    btnRecommend.isSelected = true
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }

            RECOMMEND_CANCEL_REQUEST -> {
                if (event.status == "ok") {
                    btnRecommend.isSelected = false
                } else {
                    Logger.d("error : ${event.code}, ${event.response}")
                }
            }
        }
    }
}