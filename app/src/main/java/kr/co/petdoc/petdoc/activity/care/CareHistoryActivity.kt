package kr.co.petdoc.petdoc.activity.care

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_care_history.*
import kotlinx.android.synthetic.main.adapter_care_history_item.view.*
import kotlinx.android.synthetic.main.view_clinic_type.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.db.care.body.TemperatureDB
import kr.co.petdoc.petdoc.db.care.humidity.HumidityDB
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImageDB
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.CareHistoryListRespone
import kr.co.petdoc.petdoc.network.response.submodel.CareHistoryData
import kr.co.petdoc.petdoc.utils.BitmapManager
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Petdoc
 * Class: CareHistoryActivity
 * Created by kimjoonsung on 2020/04/28.
 *
 * Description :
 */
class CareHistoryActivity : BaseActivity() {

    private val TAG = "CareHistoryActivity"
    private val CARE_HISTORY_LIST_REQUEST = "$TAG.careHistoryListRequest"

    private lateinit var mAdapter: HistoryAdapter
    private var historyItems:MutableList<CareHistoryData> = mutableListOf()

    private lateinit var earImageDB : EarImageDB
    private var earImage:EarImage? = null

    private var selectedDate = ""
    private var selectedYear = -1
    private var selectedMonth = -1
    private var selectedDay = -1
    private var petId = -1
    private var petKind = ""
    private var petImage = ""

    private var margin50 = 0
    private var margin10 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_care_history)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        margin50 = Helper.convertDpToPx(this, 50f).toInt()
        margin10 = Helper.convertDpToPx(this, 10f).toInt()

        earImageDB = EarImageDB.getInstance(this)!!

        petId = intent?.getIntExtra("petId", petId)!!
        petKind = intent?.getStringExtra("petKind") ?: petKind
        petImage = intent?.getStringExtra("petImage") ?: petImage
        Logger.d("petId : $petId, petKind : $petKind, petImage : $petImage")

        btnClose.setOnSingleClickListener { onBackPressed() }
        layoutDate.setOnSingleClickListener { showCalendar() }
        btnTop.setOnSingleClickListener { careHistory.scrollToPosition(0) }

        //==========================================================================================
        mAdapter = HistoryAdapter()
        careHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        //=========================================================================================
        selectedDate = Helper.validationDateFormat.format(Date())
        retryIfNetworkAbsent {
            mApiClient.getCareHistoryList(CARE_HISTORY_LIST_REQUEST, petId, selectedDate)
        }
    }

    override fun onDestroy() {
        if (mApiClient.isRequestRunning(CARE_HISTORY_LIST_REQUEST)) {
            mApiClient.cancelRequest(CARE_HISTORY_LIST_REQUEST)
        }

        EarImageDB.destroyInstance()
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            CARE_HISTORY_LIST_REQUEST -> {
                if (response is CareHistoryListRespone) {
                    historyItems.clear()
                    historyItems.addAll(response.resultData.careHistoryList)
                    mAdapter.notifyDataSetChanged()

                    val date = historyItems[historyItems.size - 1].date.replace("-", ".")
                    startDate.text = "${date} ~ "
                    endDate.text = selectedDate.replace("-", ".")
                }
            }
        }
    }

    //===============================================================================================

    private fun onShowCameraRecord() {
        startActivity(Intent(this, ImageAlbumActivity::class.java).apply {
            putExtra("fromMy", false)
            putExtra("petId", petId)
            putExtra("petImage", petImage)
        })
    }

    private fun onMoreBodyTemp(position: Int) {
        startActivity(Intent(this, BodyTemperatureResultActivity::class.java).apply {
            putExtra("petId", petId)
            putExtra("date", historyItems[position].date)
        })
    }

    private fun onMoreHumidity(position: Int) {
        startActivity(Intent(this, HumidityResultActivity::class.java).apply {
            putExtra("petId", petId)
            putExtra("date", historyItems[position].date)
        })
    }

    private fun showCalendar() {
        val cal = Calendar.getInstance()
        val initialYear = if (selectedYear == -1) cal.get(Calendar.YEAR) else selectedYear
        val initialMonth = if (selectedMonth == -1) cal.get(Calendar.MONTH) else selectedMonth
        val initialDay = if (selectedDay == -1) cal.get(Calendar.DATE) else selectedDay
        DatePickerDialog(
                this,
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        selectedDate = String.format("%d-%02d-%02d", year, month+1, dayOfMonth)
                        selectedYear = year
                        selectedMonth = month
                        selectedDay = dayOfMonth
                        Logger.d("selected date : $selectedDate")

                        mApiClient.getCareHistoryList(CARE_HISTORY_LIST_REQUEST, petId, selectedDate)
                    }
                },
            initialYear, initialMonth, initialDay
        ).apply {
            datePicker.maxDate = cal.timeInMillis
        }.show()
    }

    private fun loadEarImage(petId: Int, year: String, month: String, day: String) : EarImage? =
        runBlocking {
            async { earImageDB.earImageDao().loadValueByDate(petId, year, month, day) }.await()
        }

    //==============================================================================================
    inner class HistoryAdapter : RecyclerView.Adapter<HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HistoryHolder(layoutInflater.inflate(R.layout.adapter_care_history_item, parent, false))

        override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
            holder.itemView.date.text = historyItems[position].date.replace("-", ".")
            val earStatus = holder.setEarImage(historyItems[position].date)
            if (historyItems[position].petId != 0 || earStatus) {
                holder.itemView.recordLayout.visibility = View.VISIBLE
                holder.setDate(historyItems[position].date.replace("-", "."))

                if (historyItems[position].petId != 0) {
                    if (petKind == "강아지") {
                        holder.itemView.walkLayout.visibility = View.VISIBLE
                        holder.itemView.communionLayout.visibility = View.GONE
                        holder.itemView.teethLayout.visibility = View.VISIBLE
                        holder.itemView.bathLayout.visibility = View.VISIBLE
                        holder.itemView.poopLayout.visibility = View.VISIBLE
                        holder.itemView.weightLayout.visibility = View.VISIBLE
                    } else if (petKind == "고양이") {
                        holder.itemView.walkLayout.visibility = View.GONE
                        holder.itemView.communionLayout.visibility = View.VISIBLE
                        holder.itemView.teethLayout.visibility = View.VISIBLE
                        holder.itemView.bathLayout.visibility = View.VISIBLE
                        holder.itemView.poopLayout.visibility = View.VISIBLE
                        holder.itemView.weightLayout.visibility = View.VISIBLE
                    } else {
                        holder.itemView.walkLayout.visibility = View.GONE
                        holder.itemView.teethLayout.visibility = View.GONE
                        holder.itemView.bathLayout.visibility = View.GONE
                        holder.itemView.poopLayout.visibility = View.GONE
                        holder.itemView.weightLayout.visibility = View.GONE
                    }

                    if(historyItems[position].walk == null && historyItems[position].teeth == null && historyItems[position].bath == null
                        && historyItems[position].weight == null && historyItems[position].urine == null && historyItems[position].feces == null
                        && historyItems[position].clinic == null && historyItems[position].clinicContent == null && historyItems[position].communion == null
                        && historyItems[position].memo == null) {
                        holder.itemView.careNoteLayer.visibility = View.GONE
                    } else {
                        holder.itemView.careNoteLayer.visibility = View.VISIBLE

                        holder.setWalkStatus(historyItems[position].walk)
                        holder.setCommunitonStatus(historyItems[position].communion)
                        holder.setToothStatus(historyItems[position].teeth)
                        holder.setBathStatus(historyItems[position].bath)
                        holder.setWeight(historyItems[position].weight)
                        holder.setMemo(historyItems[position].memo)

                        if (historyItems[position].urine != null && historyItems[position].feces != null) {
                            holder.itemView.poopLayout.visibility = View.VISIBLE
                            holder.setPeeStatus(historyItems[position].urine!!)
                            holder.setFecesStatus(historyItems[position].feces!!)
                        } else if (historyItems[position].urine != null) {
                            holder.itemView.poopLayout.visibility = View.VISIBLE
                            holder.itemView.fecesStatus.visibility = View.GONE
                            holder.setPeeStatus(historyItems[position].urine!!)
                        } else if (historyItems[position].feces != null) {
                            holder.itemView.poopLayout.visibility = View.VISIBLE
                            holder.itemView.peeStatus.visibility = View.GONE
                            holder.setFecesStatus(historyItems[position].feces!!)
                        } else {
                            holder.itemView.poopLayout.visibility = View.GONE
                        }

                        if (historyItems[position].clinicContent != null && historyItems[position].clinic != null) {
                            holder.itemView.clinicLayout.visibility = View.VISIBLE
                            holder.setClinicDesc(historyItems[position].clinicContent!!)
                            holder.setClinicType(historyItems[position].clinic!!)
                        } else if (historyItems[position].clinicContent != null) {
                            holder.itemView.clinicLayout.visibility = View.VISIBLE
                            holder.itemView.clinicType.visibility = View.GONE
                            holder.setClinicDesc(historyItems[position].clinicContent!!)
                        } else if (historyItems[position].clinic != null) {
                            holder.itemView.clinicLayout.visibility = View.VISIBLE
                            holder.itemView.clinicContent.visibility = View.GONE
                            holder.setClinicType(historyItems[position].clinic!!)
                        } else {
                            holder.itemView.clinicLayout.visibility = View.GONE
                        }
                    }
                } else {
                    holder.itemView.careNoteLayer.visibility = View.GONE
                }

                if (petKind == "강아지" || petKind == "고양이") {
                    holder.itemView.scannerLayout.visibility = View.VISIBLE
                    if (historyItems[position].temperature == 0.0 && historyItems[position].tvalue == 0.0 && historyItems[position].hvalue == 0.0 && earStatus.not()) {
                        holder.itemView.scannerRecordLayout.visibility = View.GONE
                        holder.itemView.scannerNoRecord.visibility = View.VISIBLE
                    } else {
                        holder.itemView.scannerRecordLayout.visibility = View.VISIBLE
                        holder.itemView.scannerNoRecord.visibility = View.GONE

                        holder.setHumidity(historyItems[position].tvalue, historyItems[position].hvalue)
                        holder.setBodyTemperture(historyItems[position].temperature)
                    }
                } else {
                    holder.itemView.scannerLayout.visibility = View.GONE
                }
            } else {
                holder.itemView.recordLayout.visibility = View.GONE
                holder.itemView.noRecored.visibility = View.VISIBLE

                holder.itemView.noRecored.text =
                    "${historyItems[position].date.replace("-", ".")} ${Helper.readStringRes(R.string.care_no_history)}"
            }

            if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    bottomMargin = margin50
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    bottomMargin = margin10
                }
            }

            holder.itemView.btnCameraRecord.setOnClickListener { onShowCameraRecord() }
            holder.itemView.temperatureLayout.setOnClickListener { onMoreBodyTemp(position) }
            holder.itemView.humidityLayout.setOnClickListener { onMoreHumidity(position) }
        }

        override fun getItemCount() = historyItems.size
    }

    inner class HistoryHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setDate(_date: String) {
            item.date.text = _date
        }

        fun setWalkStatus(_status: String?) {
            item.walkStatus.apply {
                when {
                    _status != null -> {
                        item.walkLayout.visibility = View.VISIBLE
                        text = _status
                    }

                    else -> item.walkLayout.visibility = View.GONE
                }
            }
        }

        fun setCommunitonStatus(_status: String?) {
            item.communionStatus.apply {
                when {
                    _status != null -> {
                        item.communionLayout.visibility = View.VISIBLE
                        text = _status
                    }

                    else -> item.communionLayout.visibility = View.GONE
                }
            }
        }

        fun setPeeStatus(_status: String?) {
            when (_status) {
                "fffabe" -> item.peeStatus.setBackgroundResource(R.drawable.cream_circle)
                "f8e71c" -> item.peeStatus.setBackgroundResource(R.drawable.sunny_yellow_circle)
                "f5a623" -> item.peeStatus.setBackgroundResource(R.drawable.orange_yellow_circle)
                "ffc0de" -> item.peeStatus.setBackgroundResource(R.drawable.paste_pink_circle)
                "67401f" -> item.peeStatus.setBackgroundResource(R.drawable.milk_chocolate_circle)
                "9df5bf" -> item.peeStatus.setBackgroundResource(R.drawable.sea_green_circle)
            }
        }

        fun setToothStatus(_status: String?) {
            item.teethStatus.apply {
                when {
                    _status != null -> {
                        item.teethLayout.visibility = View.VISIBLE
                        text = _status
                    }
                    else -> item.teethLayout.visibility = View.GONE
                }
            }
        }

        fun setFecesStatus(_status: String?) {
            item.fecesStatus.text = _status
        }

        fun setBathStatus(_status: String?) {
            item.bathStatus.apply {
                when {
                    _status != null -> {
                        item.bathLayout.visibility = View.VISIBLE
                        text = _status
                    }
                    else -> item.bathLayout.visibility = View.GONE
                }
            }
        }

        fun setWeight(_weight: String?) {
            item.weightValue.apply {
                when {
                    _weight != null -> {
                        item.weightLayout.visibility = View.VISIBLE
                        text = "${_weight}kg"
                    }
                    else -> item.weightLayout.visibility = View.GONE
                }
            }
        }

        fun setHumidity(_tvalue: Double, _hvalue:Double) {
            if (_tvalue != 0.0 && _hvalue != 0.0) {
                item.humidityLayout.visibility = View.VISIBLE
                item.humidity.text = "${_tvalue}°C/${_hvalue}%"
            } else {
                item.humidityLayout.visibility = View.GONE
            }
        }

        fun setBodyTemperture(_value: Double) {
            if (_value != 0.0) {
                item.temperatureLayout.visibility = View.VISIBLE
                item.bodyTemperture.text = "${_value}°C"
            } else {
                item.temperatureLayout.visibility = View.GONE
            }
        }

        fun setClinicDesc(_value: String) {
            item.clinicContent.text = _value
        }

        fun setMemo(_value: String?) {
            item.memo.apply {
                when {
                    _value != null -> {
                        item.memoLayout.visibility = View.VISIBLE
                        text = _value
                    }
                    else -> item.memoLayout.visibility = View.GONE
                }
            }
        }

        fun setClinicType(types: String) {
            item.clinicType.removeAllViews()
            val clinic = types.split("#")
            for (i in 1 until clinic.size) {
                item.clinicType.addView(
                    ClinicType(
                        item.context,
                        clinic[i]
                    )
                )
            }
        }

        fun setEarImage(_date: String):Boolean {
            val date = _date.split("-")
            earImage = loadEarImage(petId, date[0], date[1], date[2])

            Logger.d("earImage : ${earImage?.year}, ${earImage?.month}, ${earImage?.day}")
            if (earImage != null) {
                item.earLayout.visibility = View.VISIBLE
                if (earImage!!.leftEarImage != null) {
                    val leftImage = BitmapManager.byteToBitmap(earImage!!.leftEarImage!!)
                    item.leftEarScan.setImageBitmap(leftImage)
                }

                if (earImage!!.rightEarImage != null) {
                    val rightImage = BitmapManager.byteToBitmap(earImage!!.rightEarImage!!)
                    item.rightEarScan.setImageBitmap(rightImage)
                }

                return true
            } else {
                item.earLayout.visibility = View.GONE
                return false
            }
        }
    }

    inner class ClinicType(context: Context, type:String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_clinic_type, null, false)

            view.layoutType.setBackgroundResource(R.drawable.bg_clinic_type_grey)
            view.type.setTextColor(Helper.readColorRes(R.color.dark_grey))
            view.type.text = type

            addView(view)
        }
    }
}