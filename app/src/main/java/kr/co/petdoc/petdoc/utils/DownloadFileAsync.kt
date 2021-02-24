package kr.co.petdoc.petdoc.utils

import android.os.AsyncTask
import kr.co.petdoc.petdoc.log.Logger
import java.io.BufferedInputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.net.URL

/**
 * Petdoc
 * Class: DownloadFileAsync
 * Created by kimjoonsung on 2020/10/20.
 *
 * Description :
 */
class DownloadFileAsync (
    private val downloadLocation: String,
    private val callback: PostDownload?
) : AsyncTask<String?, String?, String?>() {

    private var fd:FileDescriptor? = null
    private var file:File? = null

    override fun doInBackground(vararg aurl: String?): String? {
        try {
            val url = URL(aurl[0])
            val connection = url.openConnection()
            connection.connect()

            val lenghtOfFile = connection.contentLength
            Logger.d("Lenght of the File : $lenghtOfFile")

            val input = BufferedInputStream(url.openStream())
            file = File(downloadLocation)
            val fos = FileOutputStream(file)
            Logger.d("File save at : ${file?.absolutePath}")
            fd = fos.fd

            var total:Long = 0
            fos.use { ouput ->
                val buffer = ByteArray(4 * 1024)
                var read:Int
                while (input.read(buffer).also { read = it } != -1) {
                    total += read.toLong()
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                    ouput.write(buffer, 0, read)
                }
                ouput.flush()
            }

            fos.flush()
            fos.close()
            input.close()
        } catch (e: Exception) {
            Logger.p(e)
        }

        return null
    }

    override fun onPostExecute(result: String?) {
        callback?.downloadDone(file)
    }

    interface PostDownload {
        fun downloadDone(file: File?)
    }
}