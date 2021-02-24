package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_customer_report.*
import kotlinx.android.synthetic.main.fragment_customer_report.btnBack
import kotlinx.android.synthetic.main.fragment_customer_report.btnWrite
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.mypage.InquiryAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.QuestionsListResponse
import kr.co.petdoc.petdoc.network.response.submodel.QuestionData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: CustomerReportFragment
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
class CustomerReportFragment : BaseFragment(), InquiryAdapter.AdapterListener {

    private val TAG = "CustomerReportFragment"
    private val QUESTION_LIST_REQUEST = "$TAG.questionListRequest"

    private lateinit var mAdapter: InquiryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_report,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnWrite.setOnClickListener {
            findNavController().navigate(CustomerReportFragmentDirections.actionCustomerReportFragmentToInquiryPostFragment())
        }

        mAdapter = InquiryAdapter().apply { setListener(this@CustomerReportFragment) }

        reportList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mApiClient.getQuestionList(QUESTION_LIST_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(QUESTION_LIST_REQUEST)) {
            mApiClient.cancelRequest(QUESTION_LIST_REQUEST)
        }
    }

    override fun onItemClicked(item:QuestionData) {
        bundleOf("item" to item).let {
            findNavController().navigate(R.id.action_customerReportFragment_to_inquiryDetailFragment, it)
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
            QUESTION_LIST_REQUEST -> {
                if (response is QuestionsListResponse) {
                    if (response.questionList.size > 0) {
                        reportList.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE

                        mAdapter.updateData(response.questionList)
                    } else {
                        reportList.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}