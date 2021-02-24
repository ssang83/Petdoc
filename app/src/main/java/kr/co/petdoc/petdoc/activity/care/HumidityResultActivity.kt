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
import kotlinx.android.synthetic.main.activity_humidity_result.*
import kotlinx.android.synthetic.main.adapter_humidity_result_item.view.*
import kotlinx.coroutines.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.db.care.humidity.Humidity
import kr.co.petdoc.petdoc.db.care.humidity.HumidityDB
import kr.co.petdoc.petdoc.domain.CareHumidityResultData
import kr.co.petdoc.petdoc.domain.CareTemperatureHumidity
import kr.co.petdoc.petdoc.extensions.isInternetConnected
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ConnectionLiveData
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HumidityResultActivity
 * Created by kimjoonsung on 2020/04/29.
 *
 * Description :
 */
class HumidityResultActivity : AppCompatActivity() {

    private lateinit var mAdapter: HumidityResultAdapter
    private var resultItems:MutableList<Humidity> = mutableListOf()
    private var recordListItems:MutableList<CareTemperatureHumidity> = mutableListOf()

    private lateinit var humidityDB : HumidityDB

    private var petPk = -1
    private var date = ""

    private var offset = 0
    private val limit = 10000
    private var isReload = false
    private var isEndofData = false

    private val apiService: PetdocApiService by inject()
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
        setContentView(R.layout.activity_humidity_result)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        humidityDB = HumidityDB.getInstance(this)!!

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this@HumidityResultActivity, Observer {
            isNetworkAvailable.postValue(it)
        })
        isNetworkAvailable.value = isInternetConnected()

        petPk = intent?.getIntExtra("petId", petPk)!!
        date = intent?.getStringExtra("date") ?: ""
        Logger.d("petId : $petPk, date : $date")

        btnClose.setOnClickListener { onBackPressed() }

        loadDBData()
//        setUpView()
//        loadData()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        HumidityDB.destroyInstance()
        super.onDestroy()
    }

    private fun setUpView() {
        mAdapter = HumidityResultAdapter()
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
     * 온습도 기록을 DB에서 가져온다.
     *
     */
    private fun loadDBData() {
        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            val items:List<Humidity>
            if (date.isNotEmpty()) {
                val loadDate = date.split("-")
                items = humidityDB.humidityDao().loadValueByDate(petPk, loadDate[0], loadDate[1], loadDate[2], "N")
            } else {
                items = humidityDB.humidityDao().loadAll(petPk, "N")
            }

            Logger.d("humidity items : ${items.size}")

            if (items.size > 0) {
                resultItems.addAll(items)

                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    layoutMeasureResult.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE

                    mAdapter = HumidityResultAdapter()
                    measureResultList.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = mAdapter
                    }

                    val itemTouchHelper = ItemTouchHelper(callback)
                    itemTouchHelper.attachToRecyclerView(measureResultList)
                }
            } else {
                lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                    layoutMeasureResult.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE

                    resultEmpty.text = String.format(
                        getString(R.string.care_mearsure_no_list),
                        Helper.readStringRes(R.string.care_health_humidity)
                    )
                }
            }
        }
    }

    /**
     * 온습도 기록을 API에서 가져온다.
     *
     */
    private fun loadData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO + exceptionHandler) {
                apiService.getHumidityForPet(petPk, offset*limit, limit)
            }

            if (response.code == "SUCCESS") {
                refreshData(response.resultData)
            }
        }
    }

    private fun refreshData(response:CareHumidityResultData) {
        recordListItems.clear()
        recordListItems.addAll(response.careTemperatureHumidityList)

        Logger.d("recordListItems : ${recordListItems.size}")

        isEndofData = response.careTemperatureHumidityList.size < limit
        if (recordListItems.size > 0) {
            layoutMeasureResult.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE

            for (item in recordListItems) {
                Humidity().apply {
                    petId = petPk
                    tvalue = item.tvalue.toString()
                    hvalue = item.hvalue.toString()
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
        } else {
            layoutMeasureResult.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE

            resultEmpty.text = String.format(
                getString(R.string.care_mearsure_no_list),
                Helper.readStringRes(R.string.care_health_humidity)
            )
        }
    }

    private fun getHumidityDate(date:String):String {
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
            withContext(Dispatchers.Default + exceptionHandler) {
                humidityDB.humidityDao().updateData(resultItems)
            }
        }
    }

    /**
     * 삭제된 온습도 기록을 전송한다.
     *
     */
    private fun sendDeleteAPI(positon: Int) {
        try {
            lifecycleScope.launch {
                apiService.deleteHumidityForPet(petPk, resultItems[positon].regDate)
            }
        } catch (e: Exception) {
            Logger.p(e)
        }
    }

    //==============================================================================================
    inner class HumidityResultAdapter : RecyclerView.Adapter<ResultHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ResultHolder(layoutInflater.inflate(R.layout.adapter_humidity_result_item, parent, false))

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.itemView.root.apply {
                when {
                    position % 2 == 0 -> setBackgroundResource(R.color.white)
                    else -> setBackgroundResource(R.color.colorf9f9f9)
                }
            }

            holder.setTemperature(resultItems[position].tvalue)
            holder.setHumidity(resultItems[position].hvalue)
            holder.setDate(resultItems[position].regDate)
        }

        override fun getItemCount() = resultItems.size

        fun removeItem(positon: Int) {
            try {
                if (scanner.isConnected().not()) {
                    sendDeleteAPI(positon)
                }

                lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                    withContext(Dispatchers.Default) {
                        humidityDB.humidityDao().updateDeleteYN(resultItems[positon].id!!, "Y")
                    }

                    lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                        resultItems.removeAt(positon)
                        notifyItemRemoved(positon)
                    }
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    inner class ResultHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setTemperature(_value: String) {
            item.temperature.text = "${_value}°C"
        }

        fun setHumidity(_value: String) {
            item.humidity.text = "${_value}%"
        }

        fun setDate(_date: String) {
            item.date.text = getHumidityDate(_date)
        }
    }

    //==============================================================================================
    private val callback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
}