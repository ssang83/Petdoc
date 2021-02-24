package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_pet_info_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.recyclerview.ItemActionListener
import kr.co.petdoc.petdoc.recyclerview.ItemDragListener
import kr.co.petdoc.petdoc.utils.Helper
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: PetInfoAdapter
 * Created by kimjoonsung on 2020/04/09.
 *
 * Description : 반려동물 정보 화면 어댑터
 */
class PetInfoAdapter : RecyclerView.Adapter<PetInfoAdapter.PetInfoHolder>(), ItemActionListener {

    private var mItems: MutableList<PetInfoItem> = mutableListOf()
    private var mListener: AdapterListener? = null
    private var mDragListener: ItemDragListener? = null
    private var petId: MutableList<Int> = mutableListOf()

    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)

    private var currentYear = ""
    private var currentMonth = ""

    private var isEditMode = false

    init {
        currentYear = yearFormat.format(Date(System.currentTimeMillis()))
        currentMonth = monthFormat.format(Date(System.currentTimeMillis()))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PetInfoHolder(
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.adapter_pet_info_item, parent, false)
            )

    override fun onBindViewHolder(holder: PetInfoHolder, position: Int) {
        val item = mItems[position]
        if (isEditMode) {
            holder.itemView.btnSwitch.visibility = View.VISIBLE
            holder.itemView.diagnosisTitle.visibility = View.GONE
            holder.itemView.diagnosisStatus.visibility = View.GONE
            holder.itemView.customealPurchase.visibility = View.GONE
            holder.itemView.layoutPetAddStatus.visibility = View.GONE

            holder.itemView.btnSwitch.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    mDragListener?.onStartDrag(holder)
                }
                false
            }
        } else {
            holder.itemView.btnSwitch.visibility = View.GONE
            holder.itemView.diagnosisTitle.visibility = View.VISIBLE
            holder.itemView.diagnosisStatus.visibility = View.VISIBLE
            holder.itemView.customealPurchase.visibility = View.VISIBLE
            holder.itemView.layoutPetAddStatus.visibility = View.VISIBLE

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(mItems[position])
            }

            holder.itemView.setOnLongClickListener {
                isEditMode = true
                mListener?.onEditMode(true)
                notifyDataSetChanged()

                true
            }
        }

        holder.itemView.petCrown.apply {
            when {
                position == 0 -> visibility = View.VISIBLE
                else -> visibility = View.GONE
            }
        }

        val gender = if (item.gender == "man") {
            "남아"
        } else {
            "여아"
        }

        holder.itemView.petProfileImage.apply {
            when {
                item.imageUrl!!.isEmpty() -> {
                    Glide.with(holder.itemView.context)
                            .load(R.drawable.img_pet_profile_default)
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.itemView.petProfileImage)
                }

                else -> {
                    Glide.with(holder.itemView.context)
                            .load(if (item.imageUrl!!.startsWith("http")) item.imageUrl else "${AppConstants.IMAGE_URL}${item.imageUrl}")
                            .apply(RequestOptions.circleCropTransform())
                            .into(holder.itemView.petProfileImage)
                }
            }
        }

        holder.itemView.petName.text = item.name
        holder.itemView.petBreed.apply {
            when {
                item.species!!.isNotEmpty() -> {
                    visibility = View.VISIBLE
                    text = item.species
                }
                else -> visibility = View.GONE
            }
        }

        val year = item.birthday!!.split("-")[0]
        val month = item.birthday!!.split("-")[1]
        val day = item.birthday!!.split("-")[2]
        LocalDate.now()
        val period = LocalDate.of(year.toInt(), month.toInt(), day.toInt()).until(LocalDate.now())
        Logger.d("year : ${period.years}, month : ${period.months}")

        if (item.birthday!!.startsWith("1900").not()) {
            val age = "${period.years}세${period.months}개월"
            holder.itemView.petAge.text = "${age}($gender)"
        }

        if (!isEditMode) {
            if (item.kind == "강아지") {
                holder.itemView.layoutPetInfo.visibility = View.VISIBLE
                holder.itemView.diagnosisTitle.visibility = View.VISIBLE
                holder.itemView.diagnosisStatus.visibility = View.VISIBLE
                holder.itemView.layoutPetAddStatus.visibility = View.GONE

                if (item.regPetStep == 6) {
                    holder.itemView.diagnosisTitle.visibility = View.VISIBLE
                    holder.itemView.diagnosisTitle.apply {
                        when {
                            item.regInfoAllStep == item.regInfoStep -> {
                                text = Helper.readStringRes(R.string.pet_information_diagnosis_result)
                                setTextColor(Helper.readColorRes(R.color.white))
                                setBackgroundResource(R.drawable.blue_grey_bg)
                                holder.itemView.diagnosisStatus.visibility = View.GONE
                                holder.itemView.customealPurchase.visibility = View.VISIBLE

                                setOnClickListener { mListener?.onMatchFoodResult(item) }
                                holder.itemView.customealPurchase.setOnClickListener { mListener?.onPurchase(item) }
                            }
                            else -> {
                                text = Helper.readStringRes(R.string.pet_information_diagnosis)
                                setTextColor(Helper.readColorRes(R.color.white))
                                setBackgroundResource(R.drawable.salmon_round_rect_18)
                                holder.itemView.diagnosisStatus.visibility = View.VISIBLE
                                holder.itemView.customealPurchase.visibility = View.GONE

                                val step = (item.regInfoStep.toDouble() / item.regInfoAllStep.toDouble()) * 100.0
                                if (step.toInt() != 0) {
                                    holder.itemView.diagnosisStatus.visibility = View.VISIBLE
                                    holder.itemView.diagnosisStatus.setTextColor(Helper.readColorRes(R.color.salmon))
                                    holder.itemView.diagnosisStatus.text = "${step.toInt()}% 진행중"
                                } else {
                                    holder.itemView.diagnosisStatus.visibility = View.GONE
                                }

                                setOnClickListener { mListener?.onMatchFood(item) }
                            }
                        }
                    }
                } else {
                    holder.itemView.diagnosisTitle.visibility = View.GONE
                    holder.itemView.diagnosisStatus.visibility = View.GONE
                    holder.itemView.customealPurchase.visibility = View.GONE

                    holder.itemView.layoutPetInfo.visibility = View.GONE
                    holder.itemView.layoutPetAddStatus.visibility = View.VISIBLE
                    holder.itemView.petAddStatus.setTextColor(Helper.readColorRes(R.color.blue_grey))
                    val step = (item.regPetStep.toDouble() / 6.0) * 100.0
                    holder.itemView.petAddStatus.text = "${step.toInt()}% 진행중"
                }
            } else {
                holder.itemView.diagnosisTitle.visibility = View.GONE
                holder.itemView.diagnosisStatus.visibility = View.GONE
                holder.itemView.customealPurchase.visibility = View.GONE

                if (item.regPetStep != 6) { // 반려동물 등록 안 한 경우
                    holder.itemView.layoutPetInfo.visibility = View.GONE
                    holder.itemView.layoutPetAddStatus.visibility = View.VISIBLE
                    holder.itemView.petAddStatus.setTextColor(Helper.readColorRes(R.color.blue_grey))
                    val step = (item.regPetStep.toDouble() / 6.0) * 100.0
                    holder.itemView.petAddStatus.text = "${step.toInt()}% 진행중"
                } else {
                    holder.itemView.layoutPetInfo.visibility = View.VISIBLE
                    holder.itemView.layoutPetAddStatus.visibility = View.GONE
                }
            }
        } else {
            if (item.birthday!!.startsWith("1900")) {
                holder.itemView.layoutPetInfo.visibility = View.GONE
            } else {
                holder.itemView.layoutPetInfo.visibility = View.VISIBLE
            }
        }

        holder.itemView.updateBadge.apply {
            when {
                item.needUpdate == true -> visibility = View.VISIBLE
                else -> visibility = View.GONE
            }
        }

    }

    override fun getItemCount() = mItems.size

    override fun onItemMoved(from: Int, to: Int) {
        if (from == to) {
            return
        }

        val fromItem = mItems.removeAt(from)
        mItems.add(to, fromItem)

        notifyItemMoved(from, to)
        notifyDataSetChanged()

        petId.clear()
        for (i in 0 until mItems.size) {
            petId.add(mItems[i].id)
        }

        Logger.d("petId : $petId")
    }

    override fun onItemSwiped(position: Int) {
        mListener?.onItemDelete(position)
    }

    fun removeItem(positon: Int) {
        mItems.removeAt(positon)
        notifyItemRemoved(positon)
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun setDragListener(listener: ItemDragListener) {
        this.mDragListener = listener
    }

    fun updateData(items: List<PetInfoItem>) {
        this.mItems.clear()
        this.mItems = items.toMutableList()
        notifyDataSetChanged()
    }

    fun getItem(positon: Int) = mItems[positon]

    fun getChangePetId() = this.petId

    fun setEditMode(mode: Boolean) {
        this.isEditMode = mode
        notifyDataSetChanged()
    }

    fun getEditMode() = this.isEditMode

    inner class PetInfoHolder(view: View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onItemClicked(item: PetInfoItem)
        fun onMatchFoodResult(item: PetInfoItem)
        fun onMatchFood(item: PetInfoItem)
        fun onPurchase(item: PetInfoItem)
        fun onEditMode(isMode: Boolean)
        fun onItemDelete(position: Int)
    }
}