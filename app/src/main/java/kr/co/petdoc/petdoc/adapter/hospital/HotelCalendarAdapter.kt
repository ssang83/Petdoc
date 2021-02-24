package kr.co.petdoc.petdoc.adapter.hospital

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reservation_item_day.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.BaseCalendar
import kr.co.petdoc.petdoc.utils.Helper
import java.util.*

/**
 * Petdoc
 * Class: HotelCalendarAdapter
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class HotelCalendarAdapter(calendar:BaseCalendar) : RecyclerView.Adapter<HotelCalendarAdapter.ViewHolder>() {

    private val baseCalendar = calendar

    private var selectedPosition = -1
    private var selectedPrevPosition = -1

    private var today = -1

    private var dayFlags = booleanArrayOf(false, false)

    private var mListener: AdapterListener? = null

    init {
        val date = Helper.validationDateFormat.format(Date())
        today = date.split("-")[2].toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reservation_item_day, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = BaseCalendar.LOW_OF_CALENDAR * BaseCalendar.DAYS_OF_WEEK

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (today == baseCalendar.data[position]) {
            holder.itemView.today.visibility = View.VISIBLE
        } else {
            holder.itemView.today.visibility = View.INVISIBLE
        }

        if (selectedPosition == position) {
            holder.itemView.today.visibility = View.GONE
            holder.itemView.itemLayout.setBackgroundResource(R.drawable.orange_circle)
            holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.white))
        } else {
//            when (bookingSetting?.bookingTodayYn) {
//                "Y" -> {
//                    if(baseCalendar.data[position] >= today
//                        && baseCalendar.data[position] < (today + bookingSetting?.bookingPeriod!!)) {
//                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.dark_grey))
//                    } else {
//                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.light_grey3))
//                    }
//                }
//
//                else -> {
//                    if(baseCalendar.data[position] > today
//                        && baseCalendar.data[position] <= (today + bookingSetting?.bookingPeriod!!)) {
//                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.dark_grey))
//                    } else {
//                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.light_grey3))
//                    }
//                }
//            }

            holder.itemView.itemLayout.setBackgroundResource(0)
        }

        if (position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
            holder.itemView.itemDay.text = ""
        } else {
            holder.itemView.itemDay.text = baseCalendar.data[position].toString()
        }

        holder.itemView.setOnClickListener {
            if (position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
                return@setOnClickListener
            }

            if (dayFlags[0] && dayFlags[1]) {
                selectedPosition = position
                selectedPrevPosition = position

                dayFlags[0] = true
                dayFlags[1] = false

                notifyDataSetChanged()
            } else {
                selectedPosition = position

                if (selectedPrevPosition == -1 && (selectedPrevPosition != selectedPosition)) {
                    selectedPrevPosition = selectedPosition
                }

                Logger.d("prev position : $selectedPrevPosition, position : $position")

                holder.itemView.today.visibility = View.GONE
                holder.itemView.itemLayout.setBackgroundResource(R.drawable.orange_circle)
                holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.white))

                Logger.d("dayFlags[0] : ${dayFlags[0]}, dayFlags[1] : ${dayFlags[1]}")
                if (!dayFlags[0]) {
                    dayFlags[0] = true
                } else {
                    dayFlags[1] = true
                }
            }
        }
    }

    interface AdapterListener {
        fun onDateClicked(date:String)
    }

    inner class ViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView)
}