package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_notice.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.mypage.setting.NoticeAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NoticeListResponse
import kr.co.petdoc.petdoc.network.response.submodel.NoticeData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: NoticeFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 공지사항 화면
 */
class NoticeFragment : BaseFragment(), NoticeAdapter.AdapterListener {

    private val LOGTAG = "NoticeFragment"
    private val NOTICE_LIST_REQUEST = "$LOGTAG.noticeListRequest"

    private lateinit var mAdapter:NoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        mAdapter = NoticeAdapter().apply {
            setListener(this@NoticeFragment)
        }

        noticeList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mApiClient.getNoticeList(NOTICE_LIST_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(NOTICE_LIST_REQUEST)) {
            mApiClient.cancelRequest(NOTICE_LIST_REQUEST)
        }
    }

    override fun onItemClicked(item:NoticeData) {
        bundleOf("id" to item.pk).let {
            findNavController().navigate(R.id.action_noticeFragment_to_noticeDetailFragment2, it)
        }
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
        when(response.requestTag) {
            NOTICE_LIST_REQUEST -> {
                if (response is NoticeListResponse) {
                    if (response.noticeList.size > 0) {
                        noticeList.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE

                        mAdapter.updateData(response.noticeList)
                    } else {
                        noticeList.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}