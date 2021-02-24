package kr.co.petdoc.petdoc.fragment.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_chat_legacy.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: ChatLegacyDialogFragment
 * Created by kimjoonsung on 2020/05/27.
 *
 * Description :
 */
class ChatLegacyDialogFragment : DialogFragment() {

    private val LEGACY_CHAT_TOTAL_COUNT_REQUEST = "ChatExisitingInfoFragment.legacyChatTotalCountRequest"
    private val MAGAZINE_TOTAL_COUNT_REQUEST = "ChatExisitingInfoFragment.magazineTotalCountRequest"

    private lateinit var mEventBus: EventBus

    private var petKind = ""
    private var category = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return inflater.inflate(R.layout.dialog_chat_legacy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEventBus = EventBus.getDefault()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        btnConfirm.setOnClickListener { dismiss() }

        petKind = arguments?.getString("petKind")!!
        category = arguments?.getString("category")!!

        setPetType(petKind)
        setCategoryType(category)
    }

    override fun dismiss() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        super.dismiss()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            LEGACY_CHAT_TOTAL_COUNT_REQUEST -> {
                if ("ok" == event.status) {
                    chatCount.text = event.response
                }
            }

            MAGAZINE_TOTAL_COUNT_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        val count = JSONObject(event.response)["resultData"]
                        magazineCount.text = count.toString()
                    }
                }
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
}