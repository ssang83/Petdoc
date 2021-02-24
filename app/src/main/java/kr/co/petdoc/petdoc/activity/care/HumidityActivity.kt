 package kr.co.petdoc.petdoc.activity.care

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.ab180.airbridge.Airbridge
import com.aramhuvis.lite.base.*
import kotlinx.android.synthetic.main.activity_body_temperature.*
import kotlinx.android.synthetic.main.activity_humidity.*
import kotlinx.android.synthetic.main.activity_humidity.btnClose
import kotlinx.android.synthetic.main.activity_humidity.btnHistory
import kotlinx.android.synthetic.main.activity_humidity.btnScan
import kotlinx.android.synthetic.main.activity_humidity.btnVideoGuide
import kotlinx.android.synthetic.main.activity_humidity.root
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.db.care.humidity.Humidity
import kr.co.petdoc.petdoc.db.care.humidity.HumidityDB
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

 /**
 * Petdoc
 * Class: HumidityActivity
 * Created by kimjoonsung on 2020/05/18.
 *
 * Description :
 */
class HumidityActivity : AppCompatActivity() {

    private var mLiteCamera: LiteCamera? = null

    private lateinit var humidityDB : HumidityDB

    private var petPk = -1
    private var tempValue = ""
    private var humidityValue = ""

    private var mLevel = 0
    private val handler = Handler()
    private val scanHandler = Handler()
    private var humidityDrawable: ClipDrawable? = null
    private var tempDrawable: ClipDrawable? = null

    private val sdf = SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.KOREAN)
    private val yearFormat = SimpleDateFormat("yyyy", Locale.KOREAN)
    private val monthFormat = SimpleDateFormat("MM", Locale.KOREAN)
    private val dayFormat = SimpleDateFormat("dd", Locale.KOREAN)

    private var mYear = ""
    private var mMonth = ""
    private var mDay = ""

     private var btInt = -1
    private val scanner: Scanner by inject()

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_humidity)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        humidityDB = HumidityDB.getInstance(this)!!

        petPk = intent?.getIntExtra("petId", petPk)!!
        Logger.d("petPk : $petPk")

        mYear = yearFormat.format(Date(System.currentTimeMillis()))
        mMonth = monthFormat.format(Date(System.currentTimeMillis()))
        mDay = dayFormat.format(Date(System.currentTimeMillis()))
        //=========================================================================================
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HumidityResultActivity::class.java).apply {
                putExtra("petId", petPk)
            })
        }

        btnVideoGuide.setOnClickListener {
            val uri = "android.resource://${packageName}/raw/scanner_guide_temp_humidity"
            startActivity(Intent(this, ScannerGuideVideoActivity::class.java).apply {
                putExtra("uri", uri)
            })
        }

        btnClose.setOnClickListener { finish() }

        btnScan.setOnClickListener {
            FirebaseAPI(this).logEventFirebase("건강체크_온습도", "Click Event", "온습도 기록 완료")
            scanTempAndHumidity()
        }

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

        HumidityDB.destroyInstance()
        EventBus.getDefault().post("Humidity")

        scanHandler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }

    private fun scanTempAndHumidity() {
        if (mLiteCamera != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                humidityAnimaiton()
                tempAnimation()

                scanHandler.postDelayed({
                    val tempAndHumidity = mLiteCamera!!.readTemperatureAndHuminity()
                    if (tempAndHumidity[0] > 0 && tempAndHumidity[1] > 0) {
                        tempAndHumidityUI(tempAndHumidity)
                    } else {
                        tempAndHumidityErrorUI()
                    }
                }, 2000)
            }
        }
    }

    private fun tempAndHumidityUI(value: DoubleArray) {
        degree.text = String.format("%.1f", value[0])
        humidity.text = String.format("%.1f", value[1])

        tempValue = String.format("%.1f", value[0])
        humidityValue = String.format("%.1f", value[1])

        if (petPk != -1) {
            lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                Humidity().apply {
                    petId = petPk
                    tvalue = tempValue
                    hvalue = humidityValue
                    regDate = sdf.format(Date(System.currentTimeMillis()))
                    deleteYN = "N"
                    year = mYear
                    month = mMonth
                    day = mDay
                }.let {
                    humidityDB.humidityDao().insert(it)
                }

                Airbridge.trackEvent("care", "click", "온습도", null, null, null)
            }
        }
    }

    private fun tempAndHumidityErrorUI() {
        humidityStatus.apply {
            text = Helper.readStringRes(R.string.care_body_temperature_error)
            setTypeface(null, Typeface.BOLD)
            setTextSize(18f)
        }

        degree.apply {
            text = "--"
            setTextColor(Helper.readColorRes(R.color.dark_grey))
        }

        humidity.apply {
            text = "--"
            setTextColor(Helper.readColorRes(R.color.dark_grey))
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
                ACamera.LENSE_TYPE.TEMP -> {
                    Logger.d("Temperature/Humidity enable")
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

     private fun humidityAnimaiton() {
         humidityDrawable = humidityScan.drawable as ClipDrawable
         humidityDrawable?.setLevel(0)
         handler.post(humidityAnimateImage)
     }

     private fun tempAnimation() {
         tempDrawable = temperatureScan.drawable as ClipDrawable
         tempDrawable?.setLevel(0)
         handler.post(tempAnimateImage)
     }

     private val humidityAnimateImage = Runnable {
         animationHumidity()
     }

     private val tempAnimateImage = Runnable {
         animationTemp()
     }

     private fun animationHumidity() {
         mLevel += 1000
         humidityDrawable?.setLevel(mLevel)
         if (mLevel <= 10000) {
             handler.postDelayed(humidityAnimateImage, 300L)
         } else {
             handler.removeCallbacks(humidityAnimateImage)
         }
     }

     private fun animationTemp() {
         mLevel += 1000
         tempDrawable?.setLevel(mLevel)
         if (mLevel <= 10000) {
             handler.postDelayed(tempAnimateImage, 300L)
         } else {
             handler.removeCallbacks(tempAnimateImage)
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
                scanTempAndHumidity()
            }
            return true
        }
    }
}