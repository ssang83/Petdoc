package kr.co.petdoc.petdoc.log

import android.os.Environment
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: LogController
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class LogController {

    companion object {
        private const val DEFAULT_FILE_NAME = "Log.txt"
        private const val MAX_LENGTH = 4000
    }

    private var mFilePath: String = ""
    private var mFileName: String = ""

    fun e(tag: String, message: String) { Log.e(tag, getCaller(message)) }

    fun w(tag: String, message: String) { Log.w(tag, getCaller(message)) }

    fun i(tag: String, message: String) { Log.i(tag, getCaller(message)) }

    fun d(tag: String, message: String) { Log.d(tag, getCaller(message)) }

    fun v(tag: String, message: String) { Log.v(tag, getCaller(message)) }

    fun c(tag:String, message: String) {
        var msg:String

        val e = Exception()
        if(e.stackTrace != null && e.stackTrace.size > 3) {
            val el:StackTraceElement
            if (e.stackTrace[4].toString().contains(".handleMessage")) {
                el = e.stackTrace[4]
            } else {
                el = e.stackTrace[3]
            }

            StringBuilder().apply {
                append(getCaller(message))
                append("   [ Called From : ")
                append("${el.fileName} ")
                append("${el.lineNumber} ")
            }.let {
                msg = it.toString()
            }
        } else {
            msg = message
        }

        Log.i(tag, msg)
    }

    fun l(tag: String, message: String) {
        val len = message.length
        if(len > MAX_LENGTH) {
            val count = (len / MAX_LENGTH) + 1
            for (i in 0 until count) {
                val start = MAX_LENGTH * i
                var end = start + MAX_LENGTH
                if (end > len) {
                    end = len
                }

                Log.i(tag, message.substring(start, end))
            }
        } else {
            Log.i(tag, message)
        }
    }

    fun h(tag: String, message: String) {
        StringBuilder().apply {
            append(getCaller(""))
            append("\n=====================================================")
            append("\n║  $message")
            append("\n=====================================================")
        }.let { Log.e(tag, it.toString()) }
    }

    fun json(tag: String, message: String) {
        val JSON_INDENT = 4

        try {
            if(message.startsWith("{")) {
                val obj = JSONObject(message)
                Log.d(tag, getCaller(obj.toString(JSON_INDENT)))
            } else if(message.startsWith("[")) {
                val array = JSONArray(message)
                Log.d(tag, getCaller(array.toString(JSON_INDENT)))
            }
        } catch (e: JSONException) {
            p(tag, e)
        }
    }

    fun json2(tag: String, message: String) {
        val JSON_INDENT = 4

        try {
            if(message.startsWith("{")) {
                val obj = JSONObject(message)
                val msg = obj.toString(JSON_INDENT)

                val lines = msg.split(System.getProperty("line.separator")!!)
                for (line in lines) {
                    Log.d(tag, getCaller(line))
                }
            } else if(message.startsWith("[")) {
                val jArray = JSONArray(message)
                Log.d(tag, getCaller(jArray.toString(JSON_INDENT)))
            }
        } catch (e: JSONException) {
            p(tag, e)
        }
    }

    fun p(tag: String, throwable: Throwable) {
        if(throwable == null) return

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        e(tag, sw.buffer.toString())
    }

    fun setFilePath(path: String) {
        mFilePath = path
    }

    fun setFileName(name: String) {
        mFileName = name
    }

    fun s(tag: String, log:String) {
        var log = getCaller(log)

        Log.d(tag, log)

        saveLog(tag, log, 0)
    }

    private fun saveLog(tag: String, message: String, state: Int) {
        if(mFilePath.isEmpty()) return

        val status = Environment.getExternalStorageState()
        if(status.equals(Environment.MEDIA_MOUNTED, true) == false) {
            e(tag, "SDCard Status : ${status}")
            return
        }

        val dir = File(mFilePath)
        if(dir.exists() == false) {
            dir.mkdirs()
        }

        if(mFileName.isEmpty()) {
            mFileName = DEFAULT_FILE_NAME
        }

        try {
            val f = File(mFilePath, mFileName)

            val fos: FileOutputStream
            if(state == 1) {
                fos = FileOutputStream(f, false)
            } else {
                fos = FileOutputStream(f, true)
            }

            val sdf = SimpleDateFormat("MM/dd HH:mm:ss:SSS", Locale.KOREA)
            val logTime = sdf.format(Date())
            val p = "${logTime}\t${message}\n\r"

            fos.write(p.toByteArray())
            fos.close()
        } catch (e:Exception) {
            p(tag, e)
        }
    }

    private fun getCaller(message: String): String {
        val e = Exception()
        if(e.stackTrace != null && e.stackTrace.size > 2) {
            val el = e.stackTrace[3]

            StringBuilder("(").apply {
                append("${el.fileName} ")
                append("${el.lineNumber}) ")
                append(message)
            }.let { return it.toString() }
        } else {
            return message
        }
    }



}