package kr.co.petdoc.petdoc.adapter.mypage.bookmark

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_bookmark_hospital_item.view.*
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.network.response.submodel.HospitalBookmarkData

/**
 * Petdoc
 * Class: HospitalAdapter
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 북마크 병원 어댑터
 */
class HospitalAdapter : RecyclerView.Adapter<HospitalAdapter.HospitalHolder>() {

    private var mItems:MutableList<HospitalBookmarkData> = mutableListOf()
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HospitalHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_bookmark_hospital_item, parent, false))

    override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
        if (mItems[position].mainImgUrl.isNullOrEmpty()) {
            holder.itemView.hospitalImage.visibility = View.GONE
        } else {
            holder.itemView.hospitalImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                    .load(mItems[position].mainImgUrl)
                    .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(15)))
                    .into(holder.itemView.hospitalImage)
        }

        holder.itemView.bookmarkType.apply {
            when (mItems[position].bookingType) {
                BookingType.A.name, BookingType.B.name -> {
                    holder.itemView.layoutBookingType.visibility = View.VISIBLE
                    text = "펫닥 예약 병원"
                }

                else -> holder.itemView.layoutBookingType.visibility = View.GONE
            }
        }

        holder.itemView.hospitalName.text = mItems[position].name
        holder.itemView.distance.apply {
            when{
                mItems[position].distance < 1000 -> text = "${mItems[position].distance}m"
                else -> {
                    val kilometer:Float = mItems[position].distance.toFloat() / 1000
                    val distnce = String.format("%.1f km", kilometer)
                    text = distnce
                }
            }
        }

        holder.itemView.address.text = mItems[position].location
        holder.itemView.status.apply {
            when (mItems[position].runStatus) {
                RunStatus.O.name -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.VISIBLE
                    holder.itemView.divider1.visibility = View.VISIBLE
                    text = RunStatus.O.status
                }
                RunStatus.C.name -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.VISIBLE
                    holder.itemView.divider1.visibility = View.VISIBLE
                    text = RunStatus.C.status
                }
                RunStatus.B.name -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.VISIBLE
                    holder.itemView.divider1.visibility = View.VISIBLE
                    text = RunStatus.B.status
                }
                RunStatus.R.name -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.VISIBLE
                    holder.itemView.divider1.visibility = View.VISIBLE
                    text = RunStatus.R.status
                }
                else -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.GONE
                    holder.itemView.divider1.visibility = View.GONE
                }
            }
        }

        holder.itemView.medicalTime.apply {
            when {
                mItems[position].allDayTime == true -> {
                    holder.itemView.layoutHospitalStatus.visibility = View.VISIBLE
                    text = "24시"
                }
                mItems[position].allDayTime == false -> {
                    if (mItems[position].startTime != null && mItems[position].endTime != null) {
                        visibility = View.VISIBLE
                        text = "${mItems[position].startTime} ~ ${mItems[position].endTime}"
                    } else {
                        holder.itemView.layoutHospitalStatus.visibility = View.GONE
                    }
                }
            }
        }

        if (mItems[position].keywordList.size > 0) {
            holder.itemView.hospitalType.visibility = View.VISIBLE
            setHospitalType(holder, mItems[position].keywordList)
        } else {
            holder.itemView.hospitalType.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            mListener?.onItemClicked(mItems[position])
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: List<HospitalBookmarkData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<HospitalBookmarkData>) {
        if(items.isNotEmpty()) {
            for(i in 0 until items.size) {
                this.mItems.add(items[i])
            }

            notifyItemInserted(mItems.size - items.size)
        }
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    private fun setHospitalType(holder: HospitalHolder, keywords: List<String>) {
        var keywordLayoutWidth = 0
        var keywordWidth = 0

        holder.itemView.hospitalType.removeAllViews()
        holder.itemView.hospitalType.post {
            keywordLayoutWidth = holder.itemView.hospitalType.width
            for (i in 0 until keywords.size) {
                val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.view_hospital_type, null, false)
                view.type.text = keywords[i]
                view.measure(0, 0)
                keywordWidth += view.measuredWidth

                if (keywordLayoutWidth > keywordWidth) {
                    holder.itemView.hospitalType.addView(view)
                } else {
                    break
                }
            }
        }
    }

    inner class HospitalHolder(view: View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onItemClicked(item:HospitalBookmarkData)
    }
}