package kr.co.petdoc.petdoc.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_hospital_item.view.*
import kotlinx.android.synthetic.main.adapter_hospital_item.view.address
import kotlinx.android.synthetic.main.adapter_hospital_item.view.distance
import kotlinx.android.synthetic.main.adapter_hospital_item.view.hospitalName
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingType
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: HospitalAdapter
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description :
 */
class HomeHospitalAdapter(context:Context) : RecyclerView.Adapter<HomeHospitalAdapter.HospitalHolder>() {

    private var mItems:MutableList<HospitalItem> = mutableListOf()
    private var mListener:AdapterListener? = null
    private var mContext = context

    private var margin10 = 0
    private var margin11 = 0

    init {
        margin10 = Helper.convertDpToPx(mContext, 10f).toInt()
        margin11 = Helper.convertDpToPx(mContext, 11f).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HospitalHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_hospital_item, parent, false))

    override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
        if (position == 0) {
            (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                leftMargin = margin10
            }
        } else if (position == itemCount - 1) {
            (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                rightMargin = margin10
            }
        } else {
            (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                leftMargin = margin11
            }
        }

        mItems[position].let { item ->
            holder.itemView.hospitalName.text = item.name
            holder.itemView.distance.apply {
                when{
                    item.distance < 1000 -> text = "${item.distance}m"
                    else -> {
                        val kilometer:Float = item.distance.toFloat() / 1000
                        val distnce = String.format("%.1f km", kilometer)
                        text = distnce
                    }
                }
            }

            holder.itemView.address.text = item.location
            holder.itemView.status.apply {
                when (item.runStatus) {
                    RunStatus.O.name -> {
                        visibility = View.VISIBLE
                        holder.itemView.divider1.visibility = View.VISIBLE
                        text = RunStatus.O.status
                    }
                    RunStatus.C.name -> {
                        visibility = View.VISIBLE
                        holder.itemView.divider1.visibility = View.VISIBLE
                        text = RunStatus.C.status
                    }
                    RunStatus.B.name -> {
                        visibility = View.VISIBLE
                        holder.itemView.divider1.visibility = View.VISIBLE
                        text = RunStatus.B.status
                    }
                    RunStatus.R.name -> {
                        visibility = View.VISIBLE
                        holder.itemView.divider1.visibility = View.VISIBLE
                        text = RunStatus.R.status
                    }
                    else -> {
                        visibility = View.INVISIBLE
                        holder.itemView.divider1.visibility = View.GONE
                    }
                }
            }

            when (item.bookingType) {
                BookingType.A.name, BookingType.B.name -> {
                    holder.itemView.btnReservation.visibility = View.VISIBLE
                }

                else -> {
                    holder.itemView.btnReservation.visibility = View.GONE
                }

            }

            holder.itemView.medicalTime.apply {
                when {
                    item.allDay == true -> {
                        visibility = View.VISIBLE
                        text = "24시"
                    }
                    item.allDay == false -> {
                        if (item.startTime != null && item.endTime != null) {
                            visibility = View.VISIBLE
                            text = "${item.startTime} ~ ${item.endTime}"
                        } else {
                            visibility = View.GONE
                        }
                    }
                }
            }

            if (item.keywordList.size > 0) {
                holder.itemView.hospitalType.visibility = View.VISIBLE
                setHospitalType(holder, item.keywordList)
            } else {
                holder.itemView.hospitalType.visibility = View.INVISIBLE
            }

            holder.itemView.btnReservation.setOnClickListener {
                mListener?.onReservationClicked(item)
            }

            holder.itemView.layoutHospitalCall.setOnClickListener {
                mListener?.onHospitalCall(item)
            }

            holder.itemView.setOnClickListener {
                mListener?.onHospitalItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun setData(items: List<HospitalItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
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

    class HospitalHolder(view:View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onHospitalItemClicked(item:HospitalItem)
        fun onReservationClicked(item: HospitalItem)
        fun onReceptionClicked(item: HospitalItem)
        fun onHospitalCall(item: HospitalItem)
    }
}