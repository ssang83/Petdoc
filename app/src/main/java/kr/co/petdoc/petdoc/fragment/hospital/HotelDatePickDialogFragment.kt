package kr.co.petdoc.petdoc.fragment.hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_hotel_date_dialog.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.hospital.HotelCalendarAdapter
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.BaseCalendar
import kr.co.petdoc.petdoc.utils.Helper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HotelDatePickDialogFragment
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class HotelDatePickDialogFragment(private val callback:CallbackListener) : DialogFragment() {

    private lateinit var currentAdapter: HotelCalendarAdapter
    private lateinit var nextAdapter: HotelCalendarAdapter
    private lateinit var nextAfterAdapter: HotelCalendarAdapter
    private val currentCal = BaseCalendar()
    private val nextCal = BaseCalendar()
    private val nextAfterCal = BaseCalendar()

    interface CallbackListener{
        fun onDateSelect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hotel_date_dialog, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnComplete.setOnClickListener {
            callback.onDateSelect()
            dismiss()
        }

        btnClose.setOnClickListener { dismiss() }

        currentCal.initBaseCalendar {
            val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
            currentMonth.text = sdf.format(it.time)

            Logger.d("current month : ${sdf.format(it.time)}")
        }

        currentAdapter = HotelCalendarAdapter(currentCal)
        currentCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
            adapter = currentAdapter
        }

        //==========================================================================================

        nextCal.initNextMonthCalendar(1) {
            val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
            nextMonth.text = sdf.format(it.time)

            Logger.d("next month : ${sdf.format(it.time)}")
        }

        nextAdapter = HotelCalendarAdapter(nextCal)
        nextCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
            adapter = nextAdapter
        }

        //==========================================================================================

        nextAfterCal.initNextMonthCalendar(2) {
            val sdf = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
            nextAfterMonth.text = sdf.format(it.time)

            Logger.d("next after month : ${sdf.format(it.time)}")
        }

        nextAfterAdapter = HotelCalendarAdapter(nextAfterCal)
        nextAfterCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), BaseCalendar.DAYS_OF_WEEK)
            adapter = nextAfterAdapter
        }
    }

}