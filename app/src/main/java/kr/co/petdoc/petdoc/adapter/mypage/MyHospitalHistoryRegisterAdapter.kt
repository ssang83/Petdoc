package kr.co.petdoc.petdoc.adapter.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_my_hospital_history_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.network.response.submodel.RegisterHistoryData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyHospitalHistoryRegisterAdapter
 * Created by kimjoonsung on 2020/08/03.
 *
 * Description :
 */
class MyHospitalHistoryRegisterAdapter : RecyclerView.Adapter<MyHospitalHistoryRegisterAdapter.HistoryHolder>() {
    private var mItems:MutableList<RegisterHistoryData> = mutableListOf()
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
                BookingStatus.RD.name -> {
                    text = "대기 중 내역"

                    holder.itemView.status.isEnabled = true
                    holder.itemView.hospitalName.isEnabled = true
                    holder.itemView.petName.isEnabled = true
                    holder.itemView.category.isEnabled = true

                    holder.itemView.textViewHospital.isEnabled = true
                    holder.itemView.textViewPetName.isEnabled = true
                    holder.itemView.textViewCategory.isEnabled = true
                }

                else -> {
                    text = "이전 내역"

                    holder.itemView.status.isEnabled = false
                    holder.itemView.hospitalName.isEnabled = false
                    holder.itemView.petName.isEnabled = false
                    holder.itemView.category.isEnabled = false

                    holder.itemView.textViewHospital.isEnabled = false
                    holder.itemView.textViewPetName.isEnabled = false
                    holder.itemView.textViewCategory.isEnabled = false
                }
            }
        }

        setResigerStatus(holder, mItems[position].bookingData.statusType)

        holder.itemView.hospitalName.text = mItems[position].bookingData.centerName
        holder.itemView.petName.text = mItems[position].bookingData.petName
        holder.itemView.category.text = mItems[position].bookingData.clinicName

        holder.itemView.setOnClickListener {
            mListener?.onRegisterItemClicked(mItems[position])
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<RegisterHistoryData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    private fun setResigerStatus(holder: HistoryHolder, _status: String) {
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

    inner class HistoryHolder(view: View) : RecyclerView.ViewHolder(view)

    interface AdapterListener {
        fun onRegisterItemClicked(item: RegisterHistoryData)
    }
}