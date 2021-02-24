package kr.co.petdoc.petdoc.bindingadapter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.DrawableMarginSpan
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.PetTalkDetailActivity
import kr.co.petdoc.petdoc.adapter.mypage.MyAlarmAdapter
import kr.co.petdoc.petdoc.adapter.mypage.MyOrderAdapter
import kr.co.petdoc.petdoc.adapter.mypage.MyPostAdapter
import kr.co.petdoc.petdoc.domain.BloodComment
import kr.co.petdoc.petdoc.domain.MyAlarm
import kr.co.petdoc.petdoc.domain.Order
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.itemdecoration.MyDividerItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.MyPetTalkData
import kr.co.petdoc.petdoc.nicepay.OrderCancel
import kr.co.petdoc.petdoc.nicepay.OrderStatus
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.MyAlarmViewModel
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseCancelViewModel
import kr.co.petdoc.petdoc.viewmodel.MyPurchaseHistoryModel
import kr.co.petdoc.petdoc.widget.BloodCommentView

@BindingAdapter("hyphenBulletText")
fun setHyphenBulletText(
    textView: AppCompatTextView,
    text: String
) {
    val hyphenDrawable = ContextCompat.getDrawable(textView.context, R.drawable.hyphen)!!
    val pad = textView.context.resources.getDimensionPixelOffset(R.dimen.padding_3dp)
    val spannableString = SpannableString(text)
    spannableString.setSpan(
        DrawableMarginSpan(hyphenDrawable, pad), 0, text.length - 1,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    textView.text = spannableString
}

@BindingAdapter("myPetTalk")
fun bindMyPetTalk(
    recyclerView: RecyclerView,
    items: List<MyPetTalkData>?
) {
    val context = recyclerView.context
    items?.let {
        recyclerView.adapter = MyPostAdapter(
            clickListener = { item ->
                Logger.d("petTalk id : ${item.id}")
                context.startActivity<PetTalkDetailActivity> {
                    putExtra("petTalkId", item.id)
                }
            }
        ).apply {
            updateData(items)
        }
    }
}

@BindingAdapter(value = ["viewModel", "myAlarm"])
fun bindAlarm(
    recyclerView: RecyclerView,
    viewModel: MyAlarmViewModel,
    items:List<MyAlarm>?
) {
    items?.let {
        recyclerView.apply {
            adapter = MyAlarmAdapter(viewModel).apply { setItems(items) }
            addItemDecoration(MyDividerItemDecoration(context, R.drawable.line_divider))
        }
    }
}

@BindingAdapter("notiImgRes")
fun setNotiImgRes(
    imageView: AppCompatImageView,
    type: String
) {
    when (type) {
        "chat" -> imageView.setBackgroundResource(R.drawable.noti_ic_chat)
        "care" -> imageView.setBackgroundResource(R.drawable.noti_ic_care)
        "hospital" -> imageView.setBackgroundResource(R.drawable.noti_ic_hospital)
        "petTalk" -> imageView.setBackgroundResource(R.drawable.noti_ic_pettalk)
        "payment", "payment_cancel" -> imageView.setBackgroundResource(R.drawable.noti_ic_purchase)
        "banner" -> imageView.setBackgroundResource(0)
        "point" -> imageView.setBackgroundResource(0)
        else -> imageView.setBackgroundResource(R.drawable.noti_ic_event)
    }
}

@BindingAdapter("notiContent")
fun setNotiContent(
    textView: AppCompatTextView,
    read: Boolean
) {
    textView.apply {
        when {
            read == true -> setTypeface(null, Typeface.NORMAL)
            else -> setTypeface(null, Typeface.BOLD)
        }
    }
}

@BindingAdapter("bloodComment")
fun bindComment(
    view: BloodCommentView,
    comment: List<BloodComment>?
) {
    comment?.let {
        view.bindComment(comment)
    }
}

@BindingAdapter(value = ["bulletText", "bulletColor"])
fun setBulletText(
    textView: AppCompatTextView,
    text: String,
    @ColorInt bulletColor: Int
) {
    val bulletGap = textView.context.resources.getDimensionPixelOffset(R.dimen.textview_bullet_margin)
    val spannableString = SpannableString(text)
    spannableString.setSpan(
        BulletSpan(bulletGap, bulletColor), 0, text.length - 1,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    textView.text = spannableString
}

@BindingAdapter(value = ["viewModel", "myPurchase"])
fun bindPurchaseHistory(
    recyclerView: RecyclerView,
    viewModel: MyPurchaseHistoryModel,
    items:List<Order>?
) {
    items?.let {
        recyclerView.apply {
            adapter = MyOrderAdapter(viewModel).apply { setItems(items) }
        }
    }
}

@BindingAdapter("ordetStatusText")
fun setOrderStatusText(
    textView: AppCompatTextView,
    status: String
) {
    textView.apply {
        when (status) {
            OrderStatus.COMPLETED.name -> {
                setTextColor(Helper.readColorRes(R.color.slateGrey))
            }

            OrderStatus.CANCELED.name, OrderStatus.NET_CANCELED.name -> {
                setTextColor(Helper.readColorRes(R.color.orange))
            }
        }
    }
}

@BindingAdapter("cancelText")
fun setPurchaseCancelText(
    textView: AppCompatTextView,
    status: String?
) {
    textView.apply {
        when {
            status == "Y" -> setTextColor(Helper.readColorRes(R.color.light_grey3))
            else -> setTextColor(Helper.readColorRes(R.color.slateGrey))
        }
    }
}

@BindingAdapter(value = ["cancelBtnText", "cancelReason"])
fun setPurchaseCancelBtnText(
    textView: TextView,
    text: String?,
    reason:OrderCancel?
) {
    textView.apply {
        if (reason == OrderCancel.NONE) {
            setTextColor(Helper.readColorRes(R.color.light_grey3))
        } else {
            if (reason == OrderCancel.ETC) {
                when {
                    text?.isEmpty() == true -> setTextColor(Helper.readColorRes(R.color.light_grey3))
                    else -> setTextColor(Helper.readColorRes(R.color.orange))
                }
            } else {
                setTextColor(Helper.readColorRes(R.color.orange))
            }
        }
    }
}

@BindingAdapter(value = ["viewModel", "cancelReason"])
fun setCancelBtnEnable(
    view:RelativeLayout,
    viewModel: MyPurchaseCancelViewModel,
    reason: OrderCancel?
) {
    view.apply {
        if (reason == OrderCancel.NONE) {
            setBackgroundResource(R.drawable.bg_purchase_cancel_disable)
            isEnabled = false
        } else {
            if (reason == OrderCancel.ETC) {
                when {
                    viewModel.etcReason.value.toString().isEmpty() -> {
                        setBackgroundResource(R.drawable.bg_purchase_cancel_disable)
                        isEnabled = false
                    }
                    else -> {
                        setBackgroundResource(R.drawable.bg_white_rect)
                        isEnabled = true
                    }
                }
            } else {
                setBackgroundResource(R.drawable.bg_white_rect)
                isEnabled = true
            }
        }
    }
}
