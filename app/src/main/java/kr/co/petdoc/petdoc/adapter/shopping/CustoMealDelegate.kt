package kr.co.petdoc.petdoc.adapter.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.layout_shopping_custom_meal.view.*
import kotlinx.android.synthetic.main.layout_shopping_diagnosis.view.*
import kotlinx.android.synthetic.main.shop_list_delegate_item_customeal.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.CustoMealDetailActivity
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.adapter.PagingAdapterDelegate
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.model.CustoMealUIModel
import kr.co.petdoc.petdoc.model.PagingItem
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.image.StorageUtils

class CustoMealDelegate : PagingAdapterDelegate<PagingItem> {
    override fun isForViewType(item: PagingItem) =
        item is CustoMealUIModel

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return CustoMealViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.shop_list_delegate_item_customeal, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        item: PagingItem,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    ) {
        val petInfos = (item as CustoMealUIModel).items
        (holder as CustoMealViewHolder).bind(petInfos)
    }

    inner class CustoMealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var items: List<PetInfoItem>? = null
        init {
            itemView.btnCustomeal.setOnClickListener {
                Airbridge.trackEvent("shopping", "click", "customeal_detail", null, null, null)
                itemView.context.startActivity<CustoMealDetailActivity> {
                    items?.let {
                        putExtra("item", it[itemView.viewPagerContainer.currentItem])
                    }
                }
            }

            itemView.btnNoPetAdd.setOnClickListener {
                val context = itemView.context
                if (StorageUtils.loadBooleanValueFromPreference(
                        context,
                        AppConstants.PREF_KEY_LOGIN_STATUS
                    )
                ) {
                    if (StorageUtils.loadIntValueFromPreference(
                            context,
                            AppConstants.PREF_KEY_MOBILE_CONFIRM
                        ) == 1
                    ) {
                        context.startActivity<PetAddActivity> {
                            putExtra("type", PetAddType.ADD.ordinal)
                        }
                    } else {
                        context.startActivity<MobileAuthActivity> ()
                    }
                } else {
                    context.startActivity<LoginAndRegisterActivity> ()
                }
            }
        }

        fun bind(items: List<PetInfoItem>?) {
                this.items = items
                if (items.isNullOrEmpty().not()) {
                    itemView.layoutCustomMealWithPet.visibility = View.VISIBLE
                    itemView.layoutCustomMealDefault.visibility = View.GONE
                    itemView.viewPagerContainer.adapter ?: run {
                        itemView.viewPagerContainer.apply {
                            adapter = CustoMealAdapter(items?: emptyList())
                            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                        }
                        TabLayoutMediator(itemView.indicator, itemView.viewPagerContainer) { tab, position ->  }.attach()
                    }
                } else {
                    itemView.layoutCustomMealWithPet.visibility = View.GONE
                    itemView.layoutCustomMealDefault.visibility = View.VISIBLE
                }

        }
    }
}