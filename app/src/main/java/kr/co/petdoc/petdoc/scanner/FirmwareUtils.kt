package kr.co.petdoc.petdoc.scanner

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.log.Logger
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Petdoc
 * Class: FirmwareUtils
 * Created by kimjoonsung on 2020/10/15.
 *
 * Description :
 */
suspend fun uploadFirmware(upgradeFilePath: String): Int {
    return withContext(Dispatchers.IO) {
        val arg = arrayOfNulls<String>(2)
        arg[0] = upgradeFilePath
        arg[1] = "root@192.168.1.1:/tmp/sysupgrade.bin"

        var fis: FileInputStream? = null
        try {
            val lfile = arg[0]!!
            arg[1] = arg[1]!!.substring(arg[1]!!.indexOf('@') + 1)
            var rfile = arg[1]!!.substring(arg[1]!!.indexOf(':') + 1)

            val jsch = JSch()

            // username and password will be given via UserInfo interface.
            val session = jsch.getSession("root", "192.168.1.1", 22)
            session.setPassword("aramhuvisqoejrwls")
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session.setConfig(config)
            session.connect()
            val ptimestamp = true

            // exec 'scp -t rfile' remotely
            rfile = rfile.replace("'", "'\"'\"'")
            rfile = "'$rfile'"
            var command = "scp " + (if (ptimestamp) "-p" else "") + " -t " + rfile
            val channel = session.openChannel("exec")
            (channel as ChannelExec).setCommand(command)

            // get I/O streams for remote scp
            val output = channel.outputStream
            val input = channel.inputStream

            channel.connect()
            if (checkAck(input) != 0) {
                System.exit(0)
            }

            val _lfile = File(lfile)
            if (ptimestamp) {
                command = "T " + _lfile.lastModified() / 1000 + " 0"

                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += " ${_lfile.lastModified() / 1000} 0\n"
                output.write(command.toByteArray())
                output.flush()
                if (checkAck(input) != 0) {
                    System.exit(0)
                }
            }
            // send "C0644 filesize filename", where filename should not include '/'
            val filesize = _lfile.length()
            command = "C0644 $filesize "
            command += if (lfile.lastIndexOf('/') > 0) {
                lfile.substring(lfile.lastIndexOf('/') + 1)
            } else {
                lfile
            }
            command += "\n"
            output.write(command.toByteArray())
            output.flush()
            if (checkAck(input) != 0) {
                System.exit(0)
            }
            // send a content of lfile
            fis = FileInputStream(lfile)
            val buf = ByteArray(1024)
            while (true) {
                val len = fis.read(buf, 0, buf.size)
                if (len <= 0) break
                output.write(buf, 0, len) //out.flush();
            }
            fis.close()
            fis = null
            // send '\0'
            // send '\0'
            buf[0] = 0
            output.write(buf, 0, 1)
            output.flush()

            if (checkAck(input) != 0) {
                System.exit(0)
            }

            output.close()

            channel.disconnect()
            session.disconnect()
            return@withContext 1
        } catch (e: Exception) {
            Logger.p(e)
            try {
                fis?.close()
            } catch (ee: Exception) { }
        }

        return@withContext -1
    }
}

suspend fun startUpgrade(): Int {
    return withContext(Dispatchers.IO) {
        val jsch = JSch()

        try {
            val session = jsch.getSession("root", "192.168.1.1", 22)
            session.setPassword("aramhuvisqoejrwls")
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session.setConfig(config)
            session.connect()   // 연결

            val channel = session.openChannel("exec")   // 채널접속
            val channelExec = channel as ChannelExec
            channelExec.setPty(true)
            channelExec.setCommand("sysupgrade -v -c /tmp/sysupgrade.bin")  // 내가 실행시킬 명령어를 입력

            // 콜백을 받을 준비.
            val outputBuffer = StringBuilder()
            val input = channel.inputStream
            channel.setErrStream(System.err)

            channel.connect()   // 실행

            val tmp = ByteArray(8192)
            var tmpStr = ""
            var find_end = false

            while (true) {
                while (input.available() > 0) {
                    val i = input.read(tmp, 0, 8192)
                    outputBuffer.append(String(tmp, 0, i))
                    tmpStr = outputBuffer.toString()
                    Logger.d("recv ssh : $tmpStr")

                    if (tmpStr.contains("Writing from <stdin>")) {
                        Logger.d("find finish log : $tmpStr")
                        find_end = true
                    }

                    if(i < 0) break
                }

                if (find_end) {
                    if (channel.isClosed) {
                        channel.disconnect()
                        break
                    }
                }

                if (channel.isClosed) {
                    Logger.d("결과 : ${outputBuffer}")
                    channel.disconnect()
                    break
                }
            }

            return@withContext 2
        } catch (e: Exception) {
            Logger.p(e)
        }

        return@withContext -1
    }
}

@Throws(IOException::class)
private fun checkAck(input: InputStream): Int {
    val b = input.read()
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if (b == 0) return b
    if (b == -1) return b
    if (b == 1 || b == 2) {
        val sb = StringBuffer()
        var c: Int
        do {
            c = input.read()
            sb.append(c.toChar())
        } while (c != '\n'.toInt())
        if (b == 1) { // error
            print(sb.toString())
        }
        if (b == 2) { // fatal error
            print(sb.toString())
        }
    }
    return b
}