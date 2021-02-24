package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_inquiry_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.QuestionData
import kr.co.petdoc.petdoc.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: InquiryDetailFragment
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 1:1 문이 상세 화면
 */
class InquiryDetailFragment : Fragment() {

    val DAY =  24 * 1000 * 60 * 60
    val HOUR = 1000 * 60 * 60
    val MIN = 1000 * 60
    val SEC = 1000

    private var questionData:QuestionData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inquiry_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionData = arguments?.getParcelable("item")

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        val createdAt = questionData?.createdAt?.split("T")!!
        val date = questionData?.createdAt?.split(".")!!
        val confirmedAt = date[0].replace("T", " ")

        replyTitle.text = questionData?.title
        replyDate.text = calculateDate(createdAt[0])
        replyDesc.text = questionData?.content

        if (questionData?.isConfirm!!) {
            replyYes.visibility = View.VISIBLE
            replyNo.visibility = View.GONE

            val duration = calculateTime(confirmedAt)
            if (duration / DAY > 0) {
                replyRegTime.text = "${duration/DAY}일전"
            } else if (duration / HOUR > 0) {
                replyRegTime.text = "${duration/HOUR}시간전"
            } else if (duration / MIN > 0) {
                replyRegTime.text = "${duration/MIN}분전"
            } else {
                replyRegTime.text = "${duration/SEC}초전"
            }

            replyComment.text = questionData?.confirmContent
        } else {
            replyYes.visibility = View.GONE
            replyNo.visibility = View.VISIBLE
        }
    }

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("MM월dd일", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    private fun calculateTime(createdAt: String) : Long {
        val dateNow = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val fromDate = dateFormat.parse(createdAt)

        return dateNow.time - fromDate.time
    }
}