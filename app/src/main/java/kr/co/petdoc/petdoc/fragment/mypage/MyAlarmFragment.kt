package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_my_alarm.*
import kr.co.petdoc.petdoc.BR
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BannerDetailActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.activity.PetTalkDetailActivity
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.activity.chat.ChatMessageActivity
import kr.co.petdoc.petdoc.activity.chat.ChatQuitActivity
import kr.co.petdoc.petdoc.base.PetdocBaseFragment
import kr.co.petdoc.petdoc.databinding.FragmentMyAlarmBinding
import kr.co.petdoc.petdoc.domain.MyAlarm
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.viewmodel.ChatConfig
import kr.co.petdoc.petdoc.viewmodel.MyAlarmViewModel
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Petdoc
 * Class: MyAlarmFragment
 * Created by kimjoonsung on 1/25/21.
 *
 * Description :
 */
class MyAlarmFragment : PetdocBaseFragment<FragmentMyAlarmBinding, MyAlarmViewModel>() {

    private val viewModel: MyAlarmViewModel by viewModel()

    override fun getTargetViewModel(): MyAlarmViewModel = viewModel

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.fragment_my_alarm

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            start()

            moveToNotiDetailScreen.observe(viewLifecycleOwner, Observer { item ->
                moveToDetail(item)
            })

            moveToChatScreen.observe(viewLifecycleOwner, Observer { config ->
                launchNotiChat(config)
            })
        }

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }
    }

    private fun moveToDetail(item: MyAlarm) {
        when (item.eventType) {
            "chat" -> {
                viewModel.moveToChat(item.eventId)
            }
            "care" -> {
                requireContext().startActivity<HealthCareActivity>()
            }
            "hospital" -> {
                requireContext().startActivity<MyPageActivity> {
                    putExtra("fromBooking", true)
                }
            }
            "petTalk" -> {
                requireContext().startActivity<PetTalkDetailActivity> {
                    putExtra("petTalkId", item.eventId)
                }
            }
            "payment" -> {
                requireContext().startActivity<MyPageActivity> {
                    putExtra("fromPurchase", true)
                }
            }
            "banner" -> {
                requireContext().startActivity<BannerDetailActivity> {
                    putExtra("bannerId", item.eventId)
                }
            }
            else -> {
                //TODO : 프로모션으로 이동
            }
        }
    }

    private fun launchNotiChat(config: ChatConfig) {
        when (config.chatStatus) {
            "end" -> {
                requireContext().startActivity<ChatQuitActivity> {
                    putExtra("chatPk", config.chatId)
                }
            }

            else -> {
                requireContext().startActivity<ChatMessageActivity> {
                    putExtra("chatPk", config.chatId)
                    putExtra("status", config.chatStatus)
                    putExtra("name", config.chatPartner)
                }
            }
        }
    }
}