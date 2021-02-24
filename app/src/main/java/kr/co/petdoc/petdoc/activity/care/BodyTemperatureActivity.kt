package kr.co.petdoc.petdoc.activity.care

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import co.ab180.airbridge.Airbridge
import com.aramhuvis.lite.base.*
import kotlinx.android.synthetic.main.activity_body_temperature.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.db.care.body.Temperature
import kr.co.petdoc.petdoc.db.care.body.TemperatureDB
import kr.co.petdoc.petdoc.db.care.calibration.Calibration
import kr.co.petdoc.petdoc.db.care.calibration.CalibrationDB
import kr.co.petdoc.petdoc.fragment.scanner.CalibrationDialogFragment
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.TemperatureInfo
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: BodyTemperatureActivity
 * Created by kimjoonsung on 2020/05/14.
 *
 * Description :
 */
class BodyTemperatureActivity : AppCompatActivity(), CalibrationDialogFragment.CallbackListener {

    private var mLiteCamera: LiteCamera? = null
    private var mInfoItem:TemperatureInfo? = null

    private lateinit var calibrationDb : CalibrationDB
    private var value:Calibration? = null

    private lateinit var temperatureDb : TemperatureDB

    private var temperatureDB = ""
    private var tempStatusDB = ""

    private var petPk = -1
    private var petKind = ""

    private var temp = 0.0
    private var bodyTemp = ""

    private var mLevel = 0
    private val handler = Handler()
    private var imgDrawable:ClipDrawable? = null

    private var sdf = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREAN)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
    private val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)

    private var mYear = ""
    private var mMonth = ""
    private var mDay = ""

    private var btInt = -1
    private val scanner: Scanner by inject()
    private val scanHandler = Handler()

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_temperature)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        petPk = intent?.getIntExtra("petId", petPk)!!
        petKind = intent?.getStringExtra("petKind")!!
        Logger.d("petId : $petPk, petKind : $petKind")

        calibrationDb = CalibrationDB.getInstance(this)!!
        temperatureDb = TemperatureDB.getInstance(this)!!

        mYear = yearFormat.format(Date(System.currentTimeMillis()))
        mMonth = monthFormat.format(Date(System.currentTimeMillis()))
        mDay = dayFormat.format(Date(System.currentTimeMillis()))
        //==========================================================================================

        btnHistory.setOnClickListener {
            startActivity(Intent(this, BodyTemperatureResultActivity::class.java).apply {
                putExtra("petId", petPk)
            })
        }

        btnVideoGuide.setOnClickListener {
            val uri = "android.resource://${packageName}/raw/scanner_guide_temperature"
            startActivity(Intent(this, ScannerGuideVideoActivity::class.java).apply {
                putExtra("uri", uri)
            })
        }

        btnClose.setOnClickListener { finish() }

        btnScan.setOnClickListener {
            FirebaseAPI(this).logEventFirebase("건강체크_체온", "Click Event", "체온 기록 완료")
            scanBodyTemperature()
        }

        layoutCalibraiton.setOnClickListener { showCalibartionDialog() }

        //==========================================================================================

        mLiteCamera = LiteCamera.getInstance(mLiteCameraListener)
        setConnectionState(LiteCamera.getConnectionState())

        scanner.reBindProcessWithScannerNetwork()
    }

    override fun onDestroy() {
        if (mLiteCamera != null) {
            mLiteCamera!!.setCameraListener(null)
            mLiteCamera = null
        }

        CalibrationDB.destroyInstance()
        TemperatureDB.destroyInstance()

        scanHandler.removeCallbacksAndMessages(null)

        EventBus.getDefault().post("BodyTemp")

        super.onDestroy()
    }

    override fun onCalibartion(calValue: String) {
        Logger.d("calibration value : $calValue")

        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
            if (value == null) {
                val calibration = Calibration()
                calibration.petId = petPk
                calibration.calibration = calValue.toDouble()
                calibrationDb?.calibrationDAO()?.insert(calibration)
            } else {
                calibrationDb.calibrationDAO().updateCalibration(petPk, calValue.toDouble())
            }
        }

        if (layoutCalibraiton.isVisible) {
            calibrationTemp.text = "+${calValue}˚C"
        }
    }

    private fun temperatureErrorUI() {
        bodyTemperatureStatus.apply {
            text = Helper.readStringRes(R.string.care_body_temperature_error)
            setTypeface(null, Typeface.BOLD)
            setTextSize(18f)
        }

        layoutTemperatureNormal.visibility = View.VISIBLE
        layoutTemperatureHigh.visibility = View.GONE
        layoutTemperatureLow.visibility = View.GONE

        scanning.visibility = View.GONE
        mLevel = 0

        bodyTemperture.apply {
            text = "--"
            setTextColor(Helper.readColorRes(R.color.dark_grey))
        }
    }

    private fun temperatureUI(infoItem: TemperatureInfo?, degree: Double) {
        bodyTemperatureStatus.apply {
            text = infoItem?.status
            setTypeface(null, Typeface.BOLD)
            setTextSize(18f)
        }

        if(degree < 37.5) {
            layoutTemperatureNormal.visibility = View.GONE
            layoutTemperatureHigh.visibility = View.GONE
            layoutTemperatureLow.visibility = View.VISIBLE

            tempLowComment.text = infoItem?.comment

            bodyTemperture.apply {
                text = String.format("%.1f", degree)
                setTextColor(Helper.readColorRes(R.color.periwinkleBlue))
            }

            temperatureDB = String.format("%.1f", degree)
            tempStatusDB = if (degree < 30) {
                "매우위험"
            } else {
                "저체온"
            }
        } else if (degree >= 37.5 && degree <= 39.5) {
            layoutTemperatureNormal.visibility = View.VISIBLE
            layoutTemperatureHigh.visibility = View.GONE
            layoutTemperatureLow.visibility = View.GONE
            afterScan.visibility = View.VISIBLE

            bodyTemperture.apply {
                text = String.format("%.1f", degree)
                setTextColor(Helper.readColorRes(R.color.dark_grey))
            }

            temperatureDB = String.format("%.1f", degree)
            tempStatusDB = "정상"
        } else {
            layoutTemperatureNormal.visibility = View.GONE
            layoutTemperatureHigh.visibility = View.VISIBLE
            layoutTemperatureLow.visibility = View.GONE

            tempHighComment.text = infoItem?.comment

            bodyTemperture.apply {
                text = String.format("%.1f", degree)
                setTextColor(Helper.readColorRes(R.color.apricotTwo))
            }

            temperatureDB = String.format("%.1f", degree)
            tempStatusDB = "고체온"
        }

        if (petPk != -1) {
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                Temperature().apply {
                    petId = petPk
                    value = temperatureDB
                    status = tempStatusDB
                    dosage = false
                    regDate = sdf.format(Date(System.currentTimeMillis()))
                    deleteYN = "N"
                    year = mYear
                    month = mMonth
                    day = mDay
                }.let {
                    temperatureDb.temperatureDao().insert(it)
                }
            }
        }

        Airbridge.trackEvent("care", "click", "체온", null, null, null)
    }

    private fun compareTemperature(temp: Double) {
        mInfoItem = null
        val degree = String.format("%.1f", temp).toDouble()
        Logger.d("temperature : $degree")

        scanning.visibility = View.GONE
        mLevel = 0
        getInfoItem(temp)
        Logger.d("item : ${mInfoItem}")

        if (mInfoItem != null) {
            temperatureUI(mInfoItem, degree)
        } else {
            temperatureErrorUI()
        }
    }

    private fun showCalibartionDialog() {
        CalibrationDialogFragment(this).apply {
            bundleOf("petKind" to petKind, "bodyTemp" to bodyTemp).let {
                arguments = it
            }
        }.show(supportFragmentManager, "Calibration")
    }

    private fun checkCalibrationValue(calValue: Double) {
        if (calValue > 4.0) {
            val value = if(petKind == "강아지") {
                38.8 - bodyTemp.toDouble()
            } else {
                38.7 - bodyTemp.toDouble()
            }

            val calibrationValue = String.format("%.1f", value)
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                calibrationDb.calibrationDAO().updateCalibration(petPk, calibrationValue.toDouble())
            }

            layoutCalibraiton.visibility = View.VISIBLE
            calibrationTemp.text = "+${String.format("%.1f", value)}˚C"

            compareTemperature(temp + value)
        } else {
            layoutCalibraiton.visibility = View.VISIBLE
            calibrationTemp.text = "+${calValue}"

            compareTemperature(temp + calValue)
        }
    }

    private fun getInfoItem(degree: Double) {
        val temp = String.format("%.1f", degree)
        Logger.d("degree : $degree")
        for (item in PetdocApplication.mTemperatureInfoList) {
            if (item.toDegree.isNotEmpty() && item.fromDegree.isNotEmpty()) {
                if(item.fromDegree.toDouble() < temp.toDouble()
                    && item.toDegree.toDouble() > temp.toDouble()) {
                    mInfoItem = item
                    break
                }
            }
        }
    }

    private fun setConnectionState(state: CONNECTION_STATE) {
        when (state) {
            CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED,
            CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED_BY_OTHER_DEVICE -> {
                mLiteCamera?.stopPreview()
            }

            CONNECTION_STATE.CONNECTION_STATE_WIFI -> {
                decodeButtonByLenz(mLiteCamera?.currentLenzName!!)
            }

            CONNECTION_STATE.CONNECTION_STATE_USB -> {
                decodeButtonByLenz(mLiteCamera?.currentLenzName!!)
            }

            CONNECTION_STATE.CONNECTION_TRY_CONNECT_ALREADY_CONNECTED -> {
                mLiteCamera?.disconnectPrevSocket()
            }

            CONNECTION_STATE.CONNECTION_STATE_CONNECTION_REQ_FROM_OTHER_DEVICE -> {
                mLiteCamera?.disconnectCurrentSocket()
                mLiteCamera?.clear()
            }
        }
    }

    private fun decodeButtonByLenz(lenseType: String) {
        Logger.d("decodeButtonByLenz, lenseType : $lenseType")
        runOnUiThread {
            when (lenseType) {
                ACamera.LENSE_TYPE.BODY_TEMP -> {
                    Logger.d("Body Temperature enable")
                    btnScan.apply {
                        isEnabled = true
                        setTextColor(Helper.readColorRes(R.color.orange))
                        setBackgroundResource(R.drawable.main_color_round_rect)
                    }
                }
                else -> {
                    btnScan.apply {
                        isEnabled = false
                        setTextColor(Helper.readColorRes(R.color.light_grey3))
                        setBackgroundResource(R.drawable.grey_round_rect)
                    }
                }
            }
        }
    }

    private fun scanBodyTemperature() {
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            try {

                doTheAnimation()

                if (mLiteCamera != null) {
                    scanHandler.postDelayed({
                        temp = mLiteCamera!!.readBodyTemperature()
                        bodyTemp = String.format("%.1f", temp)
                        Logger.d("temperature :$temp")

                        lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                            async {
                                value = calibrationDb.calibrationDAO().getValueById(petPk)
                                value
                            }.await()

                            Logger.d("calibration : ${value}")

                            lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
                                if (value == null) {
                                    if (temp > 0) {
                                        showCalibartionDialog()
                                        compareTemperature(temp)
                                    } else {
                                        temperatureErrorUI()
                                    }
                                } else {
                                    if (temp > 0) {
                                        Logger.d("calibration value : ${value!!.calibration}")
                                        checkCalibrationValue(value!!.calibration)
                                    } else {
                                        temperatureErrorUI()
                                    }
                                }
                            }
                        }
                    }, 1500)
                }
            } catch (e: Exception) {
                Logger.p(e)
            }
        }
    }

    private fun doTheAnimation() {
        afterScan.visibility = View.GONE
        scanning.visibility = View.VISIBLE
        imgDrawable = scanning.drawable as ClipDrawable
        imgDrawable?.setLevel(0)
        handler.post(animateImage)
    }

    private val animateImage = Runnable {
        animation()
    }

    private fun animation() {
        mLevel += 1000
        imgDrawable?.setLevel(mLevel)
        if(mLevel <= 10000) {
            handler.postDelayed(animateImage, 200L)
        } else {
            handler.removeCallbacks(animateImage)
        }
    }

    // ============================================================================================
    // LiteCamera Listener
    // ============================================================================================
    private val mLiteCameraListener = object : ACameraListener {
        override fun onHydration(hydration: Int, elasticity: Int): Boolean {
            /*
             * hydration
             * -1 : Initialize error. May sensor be contaminated.
             * -2 : Timeout error.
             * 1 ~ 100, 1 is minimum, 100 is maximum.
             */

            Logger.d("hydration : $hydration, elasticity : $elasticity")
            return true
        }

        override fun onHydrationIdle(): Boolean {
            /*
             * Fit sensor to customer
             */
            Logger.d("Finish initialize. Please touch sensor.")
            return true
        }

        override fun onMessageReceived(message: String): Boolean {
            Logger.d("message : $message")
            if (message.isNotEmpty()) {
                /*
				 * bt : from 0 to 5, 5 is full battery status.
				 * ch : 4 is charging now, 1 is not charging now.
				 */
                if (message.contains("BT:")) {
                    val bt = message!!.substring(
                        message.indexOf("BT:") + "BT:".length,
                        message.indexOf("BT:") + "BT:".length + 1
                    )
                    val ch = message.substring(
                        message.indexOf("CH:") + "CH:".length,
                        message.indexOf("CH:") + "CH:".length + 1
                    )
                    btInt = bt.toInt()
                    Logger.d("bt : $bt, ch : $ch")
                }
            }

            return true
        }

        override fun onPreviewReady() = true

        override fun onShot(p0: ByteArray?) = true

        override fun onUsbConnectionEvent(state: Int) = false

        override fun onGetLenseDetect(lenseType: String): Boolean {
            Logger.d("onGetLenseDetect, lenseType : $lenseType")
            scanner.setCurrentLenseType(lenseType)
            EventBus.getDefault().post(ScannerEvent(true, false, btInt, lenseType))
            decodeButtonByLenz(lenseType)
            return true
        }

        override fun onPreviewFailed() = true

        override fun onDisconnection() = true

        override fun onEvent(event: String?, args: Any?): Boolean {
            if ("GET_SERIAL" == event) {
                Logger.d("Serial : $args")
            }

            return true
        }

        override fun onConnection(arg0: CONNECTION_STATE): Boolean {
            Logger.d("CONNECTION STATE : $arg0")
            setConnectionState(arg0)
            return true
        }

        override fun onPreview(p0: Bitmap?) = true

        override fun onKey(arg0: KEY_ID?, arg1: KEY_STATE?): Boolean {
            Logger.d("KEY_ID : $arg0, KEY_STATE : $arg1")
            if (KEY_ID.KEY_ID_MENU_CAMERA == arg0 && KEY_STATE.KEY_STATE_UP == arg1) {
                scanBodyTemperature()
            }

            return true
        }
    }
}