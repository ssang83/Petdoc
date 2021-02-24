package kr.co.petdoc.petdoc.log

/**
 * Petdoc
 * Class: Log
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
object Logger {

    val ALL = 0
    val VERBOSE = 1
    val DEBUG = 2
    val INFO = 3
    val WARN = 4
    val ERROR = 5
    val NONE = 6

    private var sLogLevel = NONE

    private var sLogTag = "Logger"

    private val sLogController = LogController()

    /**
     * 로그 TAG 설정
     */
    fun setTag(tag: String) {
        sLogTag = tag
    }

    /**
     * 로그 등급 결정
     */
    fun setLevel(logLevel: Int) {
        sLogLevel = logLevel
    }

    /**
     * 파일 로그 경로 설정
     */
    fun setFilePath(path: String) {
        e(sLogTag, "setFilePath:$path")
        sLogController.setFilePath(path)
    }

    /**
     * 파일 로그 이름 설정
     *
     *
     * default Name : Log.txt
     */
    fun setFileName(name: String) {
        e(sLogTag, "setFileName:$name")
        sLogController.setFileName(name)
    }

    /**
     * Error
     */
    fun e(vararg message: String?) {
        if (sLogLevel <= ERROR) {
            if (message.size > 1) {
                sLogController.e(message[0]!!, message[1]!!)
            } else {
                sLogController.e(sLogTag, message[0]!!)
            }
        }
    }

    /**
     * Warning
     */
    fun w(vararg message: String?) {
        if (sLogLevel <= WARN) {
            if (message.size > 1) {
                sLogController.w(message[0]!!, message[1]!!)
            } else {
                sLogController.w(sLogTag, message[0]!!)
            }
        }
    }

    /**
     * Information
     */
    fun i(vararg message: String?) {
        if (sLogLevel <= INFO) {
            if (message.size > 1) {
                sLogController.i(message[0]!!, message[1]!!)
            } else {
                sLogController.i(sLogTag, message[0]!!)
            }
        }
    }

    /**
     * Debug
     */
    fun d(vararg message: String?) {
        if (sLogLevel <= DEBUG) {
            if (message.size > 1) {
                sLogController.d(message[0]!!, message[1]!!)
            } else {
                sLogController.d(sLogTag, message[0]!!)
            }
        }
    }


    /**
     * Verbose
     */
    fun v(vararg message: String?) {
        if (sLogLevel <= VERBOSE) {
            if (message.size > 1) {
                sLogController.v(message[0]!!, message[1]!!)
            } else {
                sLogController.v(sLogTag, message[0]!!)
            }
        }
    }

    /**
     * Caller 도 같이 표시 한다.
     */
    fun c(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.c(message[0]!!, message[1]!!)
        } else {
            sLogController.c(sLogTag, message[0]!!)
        }
    }

    /**
     * Large Logger.
     *
     *
     * 로그가 4000자를 넘을 경우에 사용한다.
     */
    fun l(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.l(message[0]!!, message[1]!!)
        } else {
            sLogController.l(sLogTag, message[0]!!)
        }
    }

    /**
     * HighLight.
     *
     *
     * 중요 로그 표시에 사용하자.
     */
    fun h(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.h(message[0]!!, message[1]!!)
        } else {
            sLogController.h(sLogTag, message[0]!!)
        }
    }

    /**
     * Print StackTrace.
     *
     *
     * Exception StackTrace 정보 표시.
     */
    fun p(tag: String?, t: Throwable?) {
        if (sLogLevel == NONE) {
            return
        }
        sLogController.p(tag!!, t!!)
    }

    fun p(t: Throwable?) {
        if (sLogLevel == NONE) {
            return
        }
        sLogController.p(sLogTag, t!!)
    }

    /**
     * JSON Format
     */
    fun json(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.json(message[0]!!, message[1]!!)
        } else {
            sLogController.json(sLogTag, message[0]!!)
        }
    }

    /**
     * JSON Format
     */
    fun json2(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.json2(message[0]!!, message[1]!!)
        } else {
            sLogController.json2(sLogTag, message[0]!!)
        }
    }

    /**
     * 파일에 로그를 저장 한다.  (trace 에 로그도 같이 나온다.)
     */
    fun s(vararg message: String?) {
        if (sLogLevel == NONE) {
            return
        }
        if (message.size > 1) {
            sLogController.s(message[0]!!, message[1]!!)
        } else {
            sLogController.s(sLogTag, message[0]!!)
        }
    }
}