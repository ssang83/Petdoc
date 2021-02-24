package kr.co.petdoc.petdoc.adapter.hospital

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reservation_item_day.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.BookingSetting
import kr.co.petdoc.petdoc.utils.BaseCalendar
import kr.co.petdoc.petdoc.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: CalendarAdapter
 * Created by kimjoonsung on 2020/06/17.
 *
 * Description :
 */
class CalendarAdapter(calendar: BaseCalendar): RecyclerView.Adapter<CalendarAdapter.ViewHolderHelper>() {

    private val baseCalendar = calendar
    private var selectedPosition = -1
    private var today = -1
    private var currentMonth = ""
    private var month = ""
    private var isChageMonth = false
    private var isNextMonth = false
    private var periodOffset = -1

    private var bookingSetting:BookingSetting? = null
    private var mListener:AdapterListener? = null

    init {
        val date = Helper.validationDateFormat.format(Date())
        today = date.split("-")[2].toInt()

        val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
        currentMonth = dayFormat.format(Date())
        month = dayFormat.format(Date())

        Logger.d("current month : $currentMonth, today : $today")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHelper {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reservation_item_day, parent, false)
        return ViewHolderHelper(view)
    }

    override fun getItemCount() = BaseCalendar.LOW_OF_CALENDAR * BaseCalendar.DAYS_OF_WEEK

    override fun onBindViewHolder(holder: ViewHolderHelper, position: Int) {
        if (isChageMonth) {
            holder.itemView.today.visibility = View.GONE
            holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.light_grey3))
            holder.itemView.itemLayout.setBackgroundResource(0)

            if (isNextMonth && periodOffset > 0) {
                when (position) {
                    in 0..periodOffset-1 -> {
                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        holder.itemView.itemDay.setOnClickListener {
                            selectedPosition = position
                            mListener?.onDateClicked(baseCalendar.data[position].toString())
                            notifyDataSetChanged()
                        }
                    }
                }
            }

            if (selectedPosition == position) {
                holder.itemView.itemLayout.setBackgroundResource(R.drawable.orange_circle)
                holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.white))
            }
        } else {
            if (today == baseCalendar.data[position]) {
                holder.itemView.today.visibility = View.VISIBLE
            } else {
                holder.itemView.today.visibility = View.GONE
            }

            holder.itemView.itemLayout.setBackgroundResource(0)
            holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.light_grey3))

            periodOffset = (today + bookingSetting?.bookingPeriod!!) - baseCalendar.currentMonthMaxDate
            when (bookingSetting?.bookingTodayYn) {
                "Y" -> {
                    if(baseCalendar.data[position] >= today
                        && baseCalendar.data[position] < (today + bookingSetting?.bookingPeriod!!)) {
                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        holder.itemView.itemDay.setOnClickListener {
                            selectedPosition = position
                            mListener?.onDateClicked(baseCalendar.data[position].toString())
                            notifyDataSetChanged()
                        }
                    }
                }

                else -> {
                    if(baseCalendar.data[position] > today
                        && baseCalendar.data[position] <= (today + bookingSetting?.bookingPeriod!!)) {
                        holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.dark_grey))
                        holder.itemView.itemDay.setOnClickListener {
                            selectedPosition = position
                            mListener?.onDateClicked(baseCalendar.data[position].toString())
                            notifyDataSetChanged()
                        }
                    }
                }
            }

            if (selectedPosition == position) {
                holder.itemView.itemLayout.setBackgroundResource(R.drawable.orange_circle)
                holder.itemView.itemDay.setTextColor(Helper.readColorRes(R.color.white))
            }
        }

        if (position < baseCalendar.prevMonthTailOffset || position >= baseCalendar.prevMonthTailOffset + baseCalendar.currentMonthMaxDate) {
            holder.itemView.itemDay.text = ""
            holder.itemView.today.visibility = View.GONE
        } else {
            holder.itemView.itemDay.text = baseCalendar.data[position].toString()
        }
    }

    fun changeToPrevMonth() {
        baseCalendar.changeToPrevMonth {
            val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
            month = dayFormat.format(it.time)
            Logger.d("month : $month")
            selectedPosition = -1

            if (currentMonth == month) {
                isChageMonth = false
            } else {
                isChageMonth = true
            }

            refreshView(it)
        }
    }

    fun changeToNextMonth() {
        baseCalendar.changeToNextMonth {
            val dayFormat = SimpleDateFormat("YYYY-MM", Locale.KOREAN)
            month = dayFormat.format(it.time)
            Logger.d("month : $month")
            selectedPosition = -1

            if (currentMonth == month) {
                isChageMonth = false
                isNextMonth = false
            } else {
                isChageMonth = true
                isNextMonth = true
            }

            refreshView(it)
        }
    }

    fun setBoodkingSetting(bookingSetting: BookingSetting?) {
        this.bookingSetting = bookingSetting
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    private fun refreshView(calendar: Calendar) {
        notifyDataSetChanged()
        mListener?.refreshCurrentMonth(calendar)
    }

    interface AdapterListener {
        fun refreshCurrentMonth(calendar: Calendar)
        fun onDateClicked(date:String)
    }

    inner class ViewHolderHelper(containerView: View) : RecyclerView.ViewHolder(containerView)
}