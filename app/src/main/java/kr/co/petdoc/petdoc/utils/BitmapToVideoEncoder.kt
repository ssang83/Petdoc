package kr.co.petdoc.petdoc.utils

import android.graphics.Bitmap
import android.media.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.petdoc.petdoc.log.Logger
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch


/**
 * Petdoc
 * Class: BitmapToVideoEncoder
 * Created by kimjoonsung on 2020/09/01.
 *
 * Description :
 */
class BitmapToVideoEncoder(callback: IBitmapToVideoEncodeCallback) {

    private val mCallback = callback
    private var mOutputFile:File? = null
    private var mEncodeQueue: Queue<Bitmap> = ConcurrentLinkedQueue()
    private var mediaCodec:MediaCodec? = null
    private var mediaMuxer:MediaMuxer? = null

    private var mFrameSync = Any()
    private var mNewFrameLatch:CountDownLatch? = null

    companion object {
        private const val MIME_TYPE = "video/avc"     // H.264 Advanced Video Coding
        private const val BIT_RATE = 1_600_000
        private const val FRAME_RATE = 30   // Frame per second
        private const val I_FRAME_INTERVAL = 1

        private var mWidth = 0
        private var mHeight = 0
    }

    private var mGenerateIndex = 0L
    private var mTrackIndex = 0
    private var mNoMoreFrames = false
    private var mAbort = false

    interface IBitmapToVideoEncodeCallback {
        fun onEncodingComplete(outputFile: File)
    }

    fun isEncodingStarted() = (mediaCodec != null) && (mediaMuxer != null) && !mNoMoreFrames && !mAbort

    fun getActiveBitmaps() = mEncodeQueue.size

    fun startEncoding(width: Int, height: Int, outputFile: File) {
        mWidth = width
        mHeight = height
        mOutputFile = outputFile

        var ouputFileString = ""
        try {
            ouputFileString = outputFile.canonicalPath
        } catch (e: IOException) {
            Logger.d("Unable to get path for ${outputFile}")
            return
        }

        val codecInfo = selectCodec(MIME_TYPE)
        if (codecInfo == null) {
            Logger.d("Unable to find an appropriate codec for ${MIME_TYPE}")
        }

        Logger.d("found codec : ${codecInfo?.name}")

        var colorFormat = 0
        try {
            colorFormat = selectColorFormat(codecInfo, MIME_TYPE)
        } catch (e: Exception) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        }

        try {
            mediaCodec = MediaCodec.createByCodecName(codecInfo?.name!!)
        } catch (e: IOException) {
            Logger.d("Unable to create MediaCode ${e.message}")
        }

        MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        }.let {
            mediaCodec?.configure(it, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec?.start()
        }

        try {
            mediaMuxer = MediaMuxer(ouputFileString, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: IOException) {
            Logger.d("MediaMuxer creation failed. ${e.message}")
            return
        }

        Logger.d("Initialization Complete. Starting encoder...")

        Completable.fromAction { encode() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun stopEncoding() {
        if (mediaCodec == null || mediaMuxer == null) {
            Logger.d("Failed to stop encoding since it never started")
            return
        }

        Logger.d("Stop Encoding")

        mNoMoreFrames = true

        synchronized(mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch!!.count > 0)) {
                mNewFrameLatch!!.countDown()
            }
        }
    }

    fun abortEncoding() {
        if (mediaCodec == null || mediaMuxer == null) {
            Logger.d("Failed to abort encoding since it never started")
            return
        }

        Logger.d("Abort Encoding")

        mNoMoreFrames = true
        mAbort = true
        mEncodeQueue = ConcurrentLinkedQueue()  // drop all frames

        synchronized(mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch!!.count > 0)) {
                mNewFrameLatch!!.countDown()
            }
        }
    }

    fun queueFrame(bitmap: Bitmap) {
        if (mediaCodec == null || mediaMuxer == null) {
            Logger.d("Failed to queue frame. Encoding not started")
            return
        }

        Logger.d("Queueing frame")

        mEncodeQueue.add(bitmap)

        synchronized(mFrameSync) {
            if ((mNewFrameLatch != null) && (mNewFrameLatch!!.count > 0)) {
                mNewFrameLatch!!.countDown()
            }
        }
    }

    private fun selectCodec(mimeType: String): MediaCodecInfo? {
        for (codec in MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos) {
            if (!codec.isEncoder) {
                continue
            }

            val types = codec.supportedTypes
            for (i in 0 until types.size) {
                if (types[i].equals(mimeType)) {
                    return codec
                }
            }
        }

        return null
    }

    private fun selectColorFormat(codecInfo: MediaCodecInfo?, mimeType: String): Int {
        val capabilities = codecInfo?.getCapabilitiesForType(mimeType)!!
        for (i in 0 until capabilities.colorFormats.size) {
            val colorFormat = capabilities.colorFormats[i]
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat
            }
        }

        return 0
    }

    private fun isRecognizedFormat(colorFormat: Int) =
        when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible -> true
            else -> false
        }

    private fun encode() {
        Logger.d("Encoder started")

        while (true) {
            if(mNoMoreFrames && (mEncodeQueue.size == 0)) break

            var bitmap = mEncodeQueue.poll()
            if (bitmap == null) {
                synchronized(mFrameSync) {
                    mNewFrameLatch = CountDownLatch(1)
                }

                try {
                    mNewFrameLatch?.await()
                } catch (e: InterruptedException) {}

                bitmap = mEncodeQueue.poll()
            }

            if(bitmap == null) continue

            val byteConvertFrame = getNV21(bitmap.width, bitmap.height, bitmap)

            val TIMEOUT_USEC = 500000L
            var inputBufIndex = mediaCodec?.dequeueInputBuffer(TIMEOUT_USEC)!!
            val ptsUsec = computePresentationTime(mGenerateIndex, FRAME_RATE)
            if (inputBufIndex >= 0) {
                val inputBuffer = mediaCodec?.getInputBuffer(inputBufIndex)!!
                inputBuffer.clear()
                inputBuffer.put(byteConvertFrame)
                mediaCodec?.queueInputBuffer(inputBufIndex, 0, byteConvertFrame.size, ptsUsec, 0)
                mGenerateIndex++
            }

            val mBufferInfo = MediaCodec.BufferInfo()
            val encoderStatus = mediaCodec?.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC)!!
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                Logger.d("No output from encoder available")
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // not expeted for an encoder
                val newFormat = mediaCodec?.getOutputFormat()!!
                mTrackIndex = mediaMuxer?.addTrack(newFormat)!!
                mediaMuxer?.start()!!
            } else if (encoderStatus < 0) {
                Logger.d("unexpected result from encoder.dequeueOutBuffer : $encoderStatus")
            } else if (mBufferInfo.size != 0) {
                val encodeData = mediaCodec?.getOutputBuffer(encoderStatus)
                if (encodeData == null) {
                    Logger.d("encoderOutBuffer $encoderStatus was null")
                } else {
                    encodeData.position(mBufferInfo.offset)
                    encodeData.limit(mBufferInfo.offset + mBufferInfo.size)
                    mediaMuxer?.writeSampleData(mTrackIndex, encodeData, mBufferInfo)!!
                    mediaCodec?.releaseOutputBuffer(encoderStatus, false)
                }
            }
        }

        release()

        if (mAbort) {
            mOutputFile?.delete()
        } else {
            if(mOutputFile != null) {
                mCallback.onEncodingComplete(mOutputFile!!)
            }
        }
    }

    private fun release() {
        if (mediaCodec != null) {
            mediaCodec!!.stop()
            mediaCodec!!.release()
            mediaCodec = null

            Logger.d("Releses Codec")
        }

        if (mediaMuxer != null) {
            mediaMuxer!!.stop()
            mediaMuxer!!.release()
            mediaMuxer = null

            Logger.d("Release Muxer")
        }
    }

    private fun getNV21(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {
        val argb = IntArray(inputWidth * inputHeight)

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)

        scaled.recycle()

        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height

        var yIndex = 0
        var uvIndex = frameSize

        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

                yuv420sp[yIndex++] = if (Y < 0) 0 else (if (Y > 255) 255 else Y).toByte()
                if (j.rem(2) == 0 && index.rem(2) == 0) {
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                }

                index++
            }
        }
    }

    private fun computePresentationTime(frameIndex: Long, framerate: Int): Long {
        return 132 + frameIndex * 1000000 / framerate
    }
}