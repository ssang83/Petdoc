package kr.co.petdoc.petdoc.fragment.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_my_post.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.PetTalkDetailActivity
import kr.co.petdoc.petdoc.adapter.mypage.MyPostAdapter
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MyPetTalkListResponse
import kr.co.petdoc.petdoc.network.response.submodel.MyPetTalkData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: MyPostFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 내 게시글 화면
 */
class MyPostFragment : BaseFragment() {

    private val LOGTAG = "MyPostFragment"
    private val PETTLAK_LIST_REQUEST = "$LOGTAG.petTalkListRequest"

    private lateinit var mAdapter:MyPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = MyPostAdapter(
            clickListener = { item ->
                requireContext().startActivity<PetTalkDetailActivity> {
                    putExtra("petTalkId", item.id)
                }
            }
        )

        myPostList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

    }

    override fun onResume() {
        super.onResume()
        mApiClient.getMyPetTalkList(PETTLAK_LIST_REQUEST)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PETTLAK_LIST_REQUEST)) {
            mApiClient.cancelRequest(PETTLAK_LIST_REQUEST)
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
        when(response.requestTag) {
            PETTLAK_LIST_REQUEST -> {
                if (response is MyPetTalkListResponse) {
                    if (response.petTalkList.size > 0) {
                        textViewMyPostEmpty.visibility = View.GONE
                        myPostList.visibility = View.VISIBLE

                        mAdapter.updateData(response.petTalkList)
                    } else {
                        textViewMyPostEmpty.visibility = View.VISIBLE
                        myPostList.visibility = View.GONE
                    }
                }
            }
        }
    }
}