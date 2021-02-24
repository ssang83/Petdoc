package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_notice_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: NoticeDetailFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 공지사항 상세 화면
 */
class NoticeDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notice_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        noticeDetail.text = "이벤트 신청햇는데 답변없는데 어떻할건가요 이벤트 신청햇는데 답변없는데 어떻할건가요 이벤트 신청햇는데 답변없는데 어떻할건가요 이벤트 신청햇는데 답변없는데 어떻할건가요 이벤트 신청햇는데 답변없는데 어떻할건가요 이벤트 신청햇는데 답변없는데 어떻할건가요 벤트 신청햇는데 답변없는데 어떻할건가요"
        noticeDetailTitle.text = "이벤트 신청 관련 공지사항"
        noticeDetailregDate.text = "8월3일"
    }
}