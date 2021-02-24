package kr.co.petdoc.petdoc.adapter.chat.v2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_care_pet_footer.view.*
import kotlinx.android.synthetic.main.adapter_care_pet_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.PetAddActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils

/**
 * Petdoc
 * Class: SBPetAdapter
 * Created by kimjoonsung on 12/11/20.
 *
 * Description :
 */
class SBPetAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPetInfoItems:MutableList<PetInfoItem> = mutableListOf()
    private var mListener:Listener? = null

    val TYPE_ITEM = 0
    val TYPE_FOOTER = 1

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                TYPE_ITEM -> PetHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_care_pet_item, parent, false))
                else -> FooterHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_care_pet_footer, parent, false))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_ITEM -> {
                holder.setIsRecyclable(false)

                if (position == 0) {
                    holder.itemView.petCrown.visibility = View.VISIBLE
                } else {
                    holder.itemView.petCrown.visibility = View.GONE
                }

                if (selectedPosition == position) {
                    holder.itemView.border.setBackgroundResource(R.drawable.orange_circle_stroke)
                } else {
                    holder.itemView.border.setBackgroundResource(R.drawable.grey_full_round_stroke_rect)
                }

                (holder as PetHolder).setName(mPetInfoItems[position].name!!)
                holder.setImage(mPetInfoItems[position].imageUrl!!)

                holder.itemView.setOnClickListener {
                    mListener?.onItemClicked(mPetInfoItems[position])
                    selectedPosition = position
                    notifyDataSetChanged()
                }
            }

            else -> {
                (holder as FooterHolder).itemView.layoutAdd.setOnClickListener {
                    if (StorageUtils.loadIntValueFromPreference(
                                    context,
                                    AppConstants.PREF_KEY_MOBILE_CONFIRM
                            ) == 1
                    ) {
                        context.startActivity(Intent(context, PetAddActivity::class.java).apply {
                            putExtra("type", PetAddType.ADD.ordinal)
                        })
                    } else {
                        context.startActivity(Intent(context, MobileAuthActivity::class.java))
                    }
                }

                holder.itemView.layoutPetInfo.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() =
            if (mPetInfoItems.size == 0) {
                1
            } else {
                mPetInfoItems.size + 1
            }

    override fun getItemViewType(position: Int): Int {
        if (mPetInfoItems.size == 0) {
            return TYPE_FOOTER
        } else {
            if (position == mPetInfoItems.size) {
                return TYPE_FOOTER
            } else {
                return TYPE_ITEM
            }
        }
    }

    fun addAll(items: MutableList<PetInfoItem>) {
        this.mPetInfoItems.clear()
        this.mPetInfoItems.addAll(items)

        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    inner class PetHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setName(_name: String) {
            item.petName.text = _name
        }

        fun setImage(_url: String) {
            item.petImage.apply {
                when {
                    _url.isNotEmpty() -> {
                        Glide.with(itemView.context)
                                .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                                .apply(RequestOptions.circleCropTransform())
                                .into(item.petImage)
                    }

                    else -> setBackgroundResource(R.drawable.img_pet_profile_default)
                }
            }
        }
    }

    inner class FooterHolder(view:View) : RecyclerView.ViewHolder(view)

    interface Listener {
        fun onItemClicked(item:PetInfoItem)
    }
}