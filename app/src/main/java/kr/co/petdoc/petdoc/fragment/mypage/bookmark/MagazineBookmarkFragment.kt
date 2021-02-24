package kr.co.petdoc.petdoc.fragment.mypage.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_bookmark_magazine.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.adapter.mypage.bookmark.PetGuideAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MagazineBookmarkListResponse
import kr.co.petdoc.petdoc.network.response.submodel.MagazineBookmarkData
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetGuideBookmarkFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 반려백과 북마크 화면
 */
class MagazineBookmarkFragment : BaseFragment(), PetGuideAdapter.AdapterListener {

    private val LOGTAG = "MagazineBookmarkFragment"
    private val BOOKMARK_LIST_REQUEST = "$LOGTAG.bookmarkListRequest"

    private lateinit var mAdapter:PetGuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark_magazine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PetGuideAdapter().apply {
            setListener(this@MagazineBookmarkFragment)
        }

        petGuideList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        mApiClient.getMagazineBookmarkList(BOOKMARK_LIST_REQUEST)
    }

    override fun onItemClicked(item:MagazineBookmarkData) {
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.pk)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(BOOKMARK_LIST_REQUEST)) {
            mApiClient.cancelRequest(BOOKMARK_LIST_REQUEST)
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
            BOOKMARK_LIST_REQUEST -> {
                if (response is MagazineBookmarkListResponse) {
                    if (response.bookmarkList.size > 0) {
                        petGuideList.visibility = View.VISIBLE
                        textViewBookmarkEmpty.visibility = View.GONE

                        mAdapter.updateData(response.bookmarkList)
                    } else {
                        petGuideList.visibility = View.GONE
                        textViewBookmarkEmpty.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}