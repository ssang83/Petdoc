package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_my_hospital_history_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.network.response.submodel.ReservationHistoryData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyHospitalHistoryAdapter
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 접수/예약 내역 히스토리 어댑터
 */
class MyHospitalHistoryAdapter : RecyclerView.Adapter<MyHospitalHistoryAdapter.HistoryHolder>() {

    private var mItems:MutableList<ReservationHistoryData> = mutableListOf()
    private var mListener:AdapterListener? = null

    private var sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HistoryHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_my_hospital_history_item, parent, false))

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        var prevStatus = ""
        if (position != 0) {
            prevStatus = mItems[position-1].bookingData.statusType
        }

        if (prevStatus == mItems[position].bookingData.statusType) {
            holder.itemView.historyStatus.visibility = View.GONE
        } else {
            holder.itemView.historyStatus.visibility = View.VISIBLE
        }

        holder.itemView.historyStatus.apply {
            when (mItems[position].bookingData.statusType) {
                BookingStatus.BD.name -> {
                    text = "예약 내역"

                    holder.itemView.status.isEnabled = true
                    holder.itemView.hospitalName.isEnabled = true
                    holder.itemView.petName.isEnabled = true
                    holder.itemView.category.isEnabled = true
                    holder.itemView.date.isEnabled = true

                    holder.itemView.textViewHospital.isEnabled = true
                    holder.itemView.textViewPetName.isEnabled = true
                    holder.itemView.textViewCategory.isEnabled = true
                    holder.itemView.textViewDate.isEnabled = true

                    holder.itemView.setOnClickListener {
                        mListener?.onItemClicked(mItems[position])
                    }
                }
                BookingStatus.BR.name -> {
                    text = "예약 승인 대기중"

                    holder.itemView.status.isEnabled = true
                    holder.itemView.hospitalName.isEnabled = true
                    holder.itemView.petName.isEnabled = true
                    holder.itemView.category.isEnabled = true
                    holder.itemView.date.isEnabled = true

                    holder.itemView.textViewHospital.isEnabled = true
                    holder.itemView.textViewPetName.isEnabled = true
                    holder.itemView.textViewCategory.isEnabled = true
                    holder.itemView.textViewDate.isEnabled = true

                    holder.itemView.setOnClickListener {
                        mListener?.onItemClicked(mItems[position])
                    }
                }

                else -> {
                    text = "이전 내역"

                    holder.itemView.status.isEnabled = false
                    holder.itemView.hospitalName.isEnabled = false
                    holder.itemView.petName.isEnabled = false
                    holder.itemView.category.isEnabled = false
                    holder.itemView.date.isEnabled = false

                    holder.itemView.textViewHospital.isEnabled = false
                    holder.itemView.textViewPetName.isEnabled = false
                    holder.itemView.textViewCategory.isEnabled = false
                    holder.itemView.textViewDate.isEnabled = false
                }
            }
        }

        setBookingStatus(holder, mItems[position].bookingData.statusType)

        val time = mItems[position].bookingData.bookingTime.split("T")
        holder.itemView.date.text = "${calculateDate(time[0])} ${calculateTime(time[1])}"

        holder.itemView.hospitalName.text = mItems[position].bookingData.centerName
        holder.itemView.petName.text = mItems[position].bookingData.petName
        holder.itemView.category.text = mItems[position].bookingData.clinicName
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<ReservationHistoryData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    private fun calculateTime(time: String) : String {
        val format1 = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
        val format = SimpleDateFormat("a kk:mm", Locale.KOREA)

        val time = format1.parse(time)

        return format.format(time)
    }

    private fun setBookingStatus(holder: HistoryHolder, _status: String) {
        holder.itemView.status.apply {
            when (_status) {
                BookingStatus.RD.name -> {
                    text = BookingStatus.RD.status
                }
                BookingStatus.RC.name -> {
                    text = BookingStatus.RC.status
                }
                BookingStatus.BD.name -> {
                    text = BookingStatus.BD.status
                }
                BookingStatus.RV.name -> {
                    text = BookingStatus.RV.status
                }
                BookingStatus.BR.name -> {
                    text = BookingStatus.BR.status
                }
                BookingStatus.BB.name -> {
                    text = BookingStatus.BB.status
                }
                BookingStatus.BC.name -> {
                    text = BookingStatus.BC.status
                }
                BookingStatus.BS.name -> {
                    text = BookingStatus.BS.status
                }
                BookingStatus.BV.name -> {
                    text = BookingStatus.BV.status
                }
                BookingStatus.NV.name -> {
                    text = BookingStatus.NV.status
                }
            }
        }
    }

    inner class HistoryHolder(view:View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onItemClicked(item:ReservationHistoryData)
    }
}