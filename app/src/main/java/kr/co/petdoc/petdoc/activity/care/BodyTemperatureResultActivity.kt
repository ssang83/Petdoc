package kr.co.petdoc.petdoc.activity.care

import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_body_temperture_result.*
import kotlinx.android.synthetic.main.adapter_body_temperature_result_item.view.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.db.care.body.Temperature
import kr.co.petdoc.petdoc.db.care.body.TemperatureDB
import kr.co.petdoc.petdoc.domain.CareTemperature
import kr.co.petdoc.petdoc.domain.CareTemperatureResultData
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.form.InjectionForm
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: BodyTemperatureResultActivity
 * Created by kimjoonsung on 2020/04/29.
 *
 * Description :
 */
class BodyTemperatureResultActivity : AppCompatActivity() {

    private lateinit var mAdapter: ResultAdapter
    private var resultItems: MutableList<Temperature> = mutableListOf()
    private var recordListItems:MutableList<CareTemperature> = mutableListOf()

    private lateinit var temperatureDb : TemperatureDB

    private var petPk = -1
    private var date = ""

    private var offset = 0
    private val limit = 10000
    private var isReload = false
    private var isEndofData = false

    private val apiService: PetdocApiService by inject()
    private val petdocRepository: PetdocRepository by inject()
    private val scanner: Scanner by inject()
    protected lateinit var connectionLiveData: ConnectionLiveData
    private val isNetworkAvailable = MutableLiveData(true)

    private val newFormat = SimpleDateFormat("yyyy.MM.dd a KK:mm", Locale.KOREAN)
    private val oldFormat = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREAN)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
    private val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_temperture_result)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        temperatureDb = TemperatureDB.getInstance(this)!!

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this@BodyTemperatureResultActivity, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = isInternetConnected()

        btnClose.setOnClickListener { onBackPressed() }

        petPk = intent?.getIntExtra("petId", petPk)!!
        date = intent?.getStringExtra("date") ?: ""
        Logger.d("petId : $petPk, date : $date")

        loadDBData()
//        setUpView()
//        loadData()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        TemperatureDB.destroyInstance()
        super.onDestroy()
    }

    private fun setUpView() {
        mAdapter = ResultAdapter()
        measureResultList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // 리스트 최 하단 체크한 후 API 호출
                    if (!isEndofData && !measureResultList.canScrollVertically(1)) {
                        isReload = true
                        ++offset

                        loadData()
                    }
                }
            })
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(measureResultList)
    }

    /**
     * 체온 기록 리스트 DB에서 가져온다.
     *
     */
    private fun loadDBData() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val items:List<Temperature>
            if (date.isNotEmpty()) {
                val loadDate = date.split("-")
                items = temperatureDb.temperatureDao().loadValueByDate(petPk, loadDate[0], loadDate[1], loadDate[2], "N")
            } else {
                items = temperatureDb.temperatureDao().loadAll(petPk, "N")
            }

            Logger.d("temperature items : ${items.size}")

            if (items.size > 0) {
                resultItems.addAll(items)

                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    mAdapter = ResultAdapter()
                    measureResultList.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = mAdapter
                    }

                    layoutMeasureResult.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE

                    val itemTouchHelper = ItemTouchHelper(callback)
                    itemTouchHelper.attachToRecyclerView(measureResultList)
                }
            } else {
                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    layoutMeasureResult.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE

                    resultEmpty.text = String.format(
                        getString(R.string.care_mearsure_no_list),
                        Helper.readStringRes(R.string.care_health_body_temp)
                    )
                }
            }
        }
    }

    /**
     * 체온 기록 리스트 API에서 가져온다.
     *
     */
    private fun loadData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO + exceptionHandler) {
                apiService.getTemparatureForPet(petPk, offset*limit, limit)
            }

            if (response.code == "SUCCESS") {
                refreshData(response.resultData)
            }
        }
    }

    private fun refreshData(response: CareTemperatureResultData) {
        recordListItems.clear()
        recordListItems.addAll(response.careTemperatureList)

        Logger.d("recordListItems : ${recordListItems.size}")

        isEndofData = response.careTemperatureList.size < limit
        if (recordListItems.size > 0) {
            for (item in recordListItems) {
                Temperature().apply {
                    petId = petPk
                    value = item.value.toString()
                    status = getBodyTempStatus(item.value)
                    dosage = item.inject
                    regDate = item.regDate
                    deleteYN = "N"
                    year = getYear(item.regDate)
                    month = getMonth(item.regDate)
                    day = getDay(item.regDate)
                }.let {
                    resultItems.add(it)
                }
            }

            mAdapter.notifyDataSetChanged()

            layoutMeasureResult.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        } else {
            layoutMeasureResult.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE

            resultEmpty.text = String.format(
                getString(R.string.care_mearsure_no_list),
                Helper.readStringRes(R.string.care_health_body_temp)
            )
        }
    }

    private fun getBodyTempStatus(value: Double) =
        if (value < 30) {
            "매우위험"
        } else if (value >= 30 && value < 37.5) {
            "저체온"
        } else if (value >= 37.5 && value <= 39.5) {
            "정상"
        } else {
            "고체온"
        }

    private fun getBodyTempDate(date:String):String {
        val oldDate = oldFormat.parse(date)
        val regDate = newFormat.format(oldDate)

        return regDate
    }

    private fun getYear(date: String): String {
        val oldDate = oldFormat.parse(date)
        val year = yearFormat.format(oldDate)

        return year
    }

    private fun getMonth(date: String): String {
        val oldDate = oldFormat.parse(date)
        val month = monthFormat.format(oldDate)

        return month
    }

    private fun getDay(date: String): String {
        val oldDate = oldFormat.parse(date)
        val day = dayFormat.format(oldDate)

        return day
    }

    /**
     * API 데이터를 DB 데이터에 밀어 넣는다.
     *
    */
    private fun syncDBData() {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                temperatureDb.temperatureDao().updateData(resultItems)
            }
        }
    }

    /**
     * 삭제된 체온 데이터를 전송한다.
     *
     */
    private fun sendDeleteAPI(position: Int) {
        try {
            lifecycleScope.launch {
                apiService.deleteTemperatureForPet(petPk, resultItems[position].regDate)
            }
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    /**
     * 투약 여부 데이터를 전송한다.
     *
     * @param position
     */
    private fun sendInjectionAPI(position: Int) {
        try {
            lifecycleScope.launch {
                val form = mutableListOf<InjectionForm>()
                InjectionForm(
                    regDate = resultItems[position].regDate,
                    inject = true
                ).let { form.add(it) }

                petdocRepository.fetchScannerInjection(
                    petPk,
                    form
                )
            }
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    //==============================================================================================
    inner class ResultAdapter : RecyclerView.Adapter<ResultHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ResultHolder(layoutInflater.inflate(R.layout.adapter_body_temperature_result_item, parent, false))

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.itemView.root.apply {
                when {
                    position % 2 == 0 -> setBackgroundResource(R.color.white)
                    else -> setBackgroundResource(R.color.colorf9f9f9)
                }
            }

            holder.setTempearture(resultItems[position].value)
            holder.setTemperatureStatus(resultItems[position].status)
            holder.setMeasureDate(resultItems[position].regDate)
            holder.setDosage(resultItems[position].dosage)
        }

        override fun getItemCount() = resultItems.size

        fun removeItem(position: Int) {
            try {
                lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                    withContext(Dispatchers.Default) {
                        temperatureDb.temperatureDao().updateDeleteYN(resultItems[position].id!!, "Y")
                        if (scanner.isConnected().not()) {
                            sendDeleteAPI(position)
                        }
                    }

                    lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                        resultItems.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    inner class ResultHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setTempearture(_value: String) {
            item.temperature.text = "${_value}°C"
        }

        fun setTemperatureStatus(_status: String) {
            item.status.text = _status
        }

        fun setMeasureDate(_date: String) {
            item.date.text = getBodyTempDate(_date)
        }

        fun setDosage(_value: Boolean) {
            item.takePill.apply {
                when {
                    _value == true -> visibility = View.VISIBLE
                    else -> visibility = View.INVISIBLE
                }
            }
        }
    }

    //==============================================================================================
    private val callback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                try {
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            Logger.d("dosage true")
                            if (scanner.isConnected().not()) {
                                sendInjectionAPI(position)
                            }

                            val id = resultItems[position].id!!
                            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                                withContext(Dispatchers.Default) { temperatureDb.temperatureDao().updateDosage(id, true) }
                                async {
                                    val items = temperatureDb.temperatureDao().loadAll(petPk, "N")
                                    resultItems.clear()
                                    resultItems.addAll(items)
                                    Logger.d("dosage : ${resultItems[position].dosage}")
                                }.await()

                                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                                    mAdapter.notifyItemChanged(position)
                                }
                            }
                        }

                        ItemTouchHelper.LEFT -> {
                            mAdapter.removeItem(position)
                            Logger.d("delete position : $position")
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(Helper.readColorRes(R.color.orange))
                    .addSwipeLeftLabel("삭제")
                    .setSwipeLeftLabelColor(Helper.readColorRes(R.color.white))
                    .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
                    .setSwipeLeftLabelTypeface(Typeface.DEFAULT_BOLD)
                    .addSwipeRightBackgroundColor(Helper.readColorRes(R.color.periwinkleBlueTwo))
                    .addSwipeRightLabel("투약")
                    .setSwipeRightLabelColor(Helper.readColorRes(R.color.white))
                    .setSwipeRightLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
                    .setSwipeRightLabelTypeface(Typeface.DEFAULT_BOLD)
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
}