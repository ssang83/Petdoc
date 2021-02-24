package kr.co.petdoc.petdoc.fragment.scanner.ear

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aramhuvis.lite.base.*
import kotlinx.android.synthetic.main.fragment_ear_scan.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.CareScanDataModel
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: EarScanFragment
 * Created by kimjoonsung on 2020/05/18.
 *
 * Description :
 */
class EarScanFragment : Fragment() {

    val PORE = "Pore"
    val WRINKLE = "Wrinkle"
    val MELANIN = "Melanin"
    val HEMOGLOBIN = "Hemoglobin"
    val SEBUM = "Sebum"

    val DENTAL = "DENTAL"
    val EAR = "S1"

    private var mLiteCamera: LiteCamera? = null
    private lateinit var dataModel: CareScanDataModel
    private var mode = ""
    private var leftEarFile : File? = null
    private var rightEarFile : File? = null
    private var earType = ""
    private var btInt = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(CareScanDataModel::class.java)
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_ear_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnShot.setOnClickListener { startCapture() }
        btnReScan.setOnClickListener { startPreview() }
        btnEarUse.setOnClickListener { requireActivity().onBackPressed() }
        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        //==========================================================================================
        mLiteCamera = LiteCamera.getInstance(mLiteCameraListener)
        setConnectionState(LiteCamera.getConnectionState())

        earType = dataModel.earType.value.toString()
    }

    override fun onPause() {
        if (mLiteCamera != null) {
            earType = ""
            mLiteCamera!!.takeShot()
            mLiteCamera!!.setCameraListener(null)
            mLiteCamera = null
        }

        super.onPause()
    }

    private fun startCapture() {
        if (mLiteCamera != null) {
            val isPreview = mLiteCamera!!.isPreviewing
            if (isPreview) {
                lifecycleScope.launch(Dispatchers.Default) {
                    mLiteCamera!!.takeShot()
                }
            }
        }
    }

    private fun startPreview() {
        if (mLiteCamera != null) {
            lifecycleScope.launch(Dispatchers.Default) {
                mLiteCamera?.startPreview(getLED())
            }

            layoutShot.visibility = View.VISIBLE
            layoutButton.visibility = View.GONE
        }
    }

    private fun getLED(): Int {
        Logger.d("mode : $mode")
        when (mode) {
            PORE -> return LED.LED_PORE_30
            WRINKLE -> return LED.LED_WRINKLE
            MELANIN -> return LED.LED_MELANIN
            HEMOGLOBIN -> return LED.LED_HEMOGLOBIN
            SEBUM -> return LED.LED_SEBUM
            ACamera.LENSE_TYPE.DENTAL -> return LED.LED_DENTAL_NL
            ACamera.LENSE_TYPE.EAR -> return LED.LED_EAR_EAR
        }

        return 0
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

        when (lenseType) {
            ACamera.LENSE_TYPE.EAR -> {
                Logger.d("Ear scan enable")
                mode = EAR

                startPreview()
            }

            ACamera.LENSE_TYPE.NONE -> mode = ""
            ACamera.LENSE_TYPE.BODY_TEMP -> mode = ""
            ACamera.LENSE_TYPE.DENTAL -> mode = ACamera.LENSE_TYPE.DENTAL
            ACamera.LENSE_TYPE.SKIN -> mode = PORE
            ACamera.LENSE_TYPE.TEMP -> mode = ""
        }
    }

    /**
     * 사진파일 생성
     */
    private fun createImageFile(type:String): File {
        // 이미지 파일 이름 ( scanner_{시간}_ )
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "scanner_${type}_${timeStamp}_"

        // 이미지가 저장될 폴더 이름 ( Scanner )
        val storgeDir = File("${requireActivity().externalCacheDir}/Petdoc/Scanner/ear")
        if(!storgeDir.exists()) storgeDir.mkdirs()

        // 빈 파일 생성
        return File.createTempFile(imageFileName, ".png", storgeDir)
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

        override fun onPreviewReady():Boolean {
            Logger.d("Camera Preview Ready!!!!")
            return true
        }

        override fun onShot(arg0: ByteArray?): Boolean {
            if (earType == "left") {
                leftEarFile = createImageFile(earType)

                try {
                    if (arg0 != null) {
                        FileOutputStream(leftEarFile!!).apply {
                            write(arg0)
                            flush()
                            close()
                        }

                        // /storage/emulated/0/Android/data/kr.co.petdoc.petdoc/cache/Petdoc/Scanner/scanner_20200518_164544_4361674039799378635.png
                        Logger.d("leftImgPath : ${leftEarFile?.absolutePath}")

                        BitmapFactory.decodeFile(leftEarFile?.absolutePath).let {
                            preview.setImageBitmap(it)
                        }

                        dataModel.leftEarImgPath.value = leftEarFile?.absolutePath
                        dataModel.leftEarImgByte.value = arg0
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if(earType == "right") {
                rightEarFile = createImageFile(earType)

                try {
                    if (arg0 != null) {
                        FileOutputStream(rightEarFile!!).apply {
                            write(arg0)
                            flush()
                            close()
                        }

                        // /storage/emulated/0/Android/data/kr.co.petdoc.petdoc/cache/Petdoc/Scanner/scanner_20200518_164544_4361674039799378635.png
                        Logger.d("rightImgPath : ${rightEarFile?.absolutePath}")

                        BitmapFactory.decodeFile(rightEarFile?.absolutePath).let {
                            preview.setImageBitmap(it)
                        }

                        dataModel.rightEarImgPath.value = rightEarFile?.absolutePath
                        dataModel.rightEarImgByte.value = arg0
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            layoutShot.visibility = View.GONE
            layoutButton.visibility = View.VISIBLE

            return true
        }

        override fun onPreview(bm: Bitmap?) : Boolean {
            lifecycleScope.launch(Dispatchers.Main) {
                preview.setImageBitmap(bm)
            }

//            val path = File("${requireActivity().externalCacheDir}/Petdoc/Scanner/Video")
//            val bitmapToVideoEncoder = BitmapToVideoEncoder(object : BitmapToVideoEncoder.IBitmapToVideoEncodeCallback{
//                override fun onEncodingComplete(outputFile: File) {
//                    AppToast(requireContext()).showToastMessage(
//                        "Encoding Complete!!",
//                        AppToast.DURATION_MILLISECONDS_DEFAULT,
//                        AppToast.GRAVITY_BOTTOM
//                    )
//                }
//            })
//
//            bitmapToVideoEncoder.startEncoding(bm?.width!!, bm?.height!!, path)
//            lifecycleScope.launch(Dispatchers.IO) {
//                bitmapToVideoEncoder.queueFrame(bm)
//            }
//
//            bitmapToVideoEncoder.stopEncoding()

            return true
        }

        override fun onPreviewFailed() = true

        override fun onUsbConnectionEvent(state: Int) = false

        override fun onGetLenseDetect(lenseType: String): Boolean {
            Logger.d("onGetLenseDetect, lenseType : $lenseType")
            val scanner: Scanner by inject()
            scanner.setCurrentLenseType(lenseType)
            EventBus.getDefault().post(ScannerEvent(true, false, btInt, lenseType))
            decodeButtonByLenz(lenseType)
            return true
        }

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

        override fun onKey(arg0: KEY_ID?, arg1: KEY_STATE?): Boolean {
            Logger.d("KEY_ID : $arg0, KEY_STATE : $arg1")
            if (KEY_ID.KEY_ID_MENU_CAMERA == arg0 && KEY_STATE.KEY_STATE_UP == arg1) {
                startCapture()
            }

            return true
        }
    }
}