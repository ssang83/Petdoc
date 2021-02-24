package kr.co.petdoc.petdoc.fragment.chat.v2.utils

import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_media_player.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: MediaPlayerActivity
 * Created by kimjoonsung on 11/30/20.
 *
 * Description :
 */
class MediaPlayerActivity : AppCompatActivity(),
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback {

    private var mMediaPlayer: MediaPlayer? = null
    private val mSurfaceHolder: SurfaceHolder? = null
    private var mUrl: String? = null
    private var mName: String? = null

    private var mVideoWidth = 0
    private var mVideoHeight = 0

    private var mIsVideoReadyToBePlayed = false
    private var mIsVideoSizeKnown = false
    private var mIsContainerSizeKnown = false

    private var mIsPaused = false
    private var mCurrentPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_media_player)
        layout_media_player.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        mSurfaceHolder?.addCallback(this)

        val extras = intent.extras
        mUrl = extras!!.getString("url")
        mName = extras.getString("name")

        progress.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.pause()
            mCurrentPosition = mMediaPlayer!!.currentPosition
            mIsPaused = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        doCleanUp()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {}

    override fun onCompletion(mp: MediaPlayer?) {
        finish()
    }

    override fun onPrepared(mediaplayer: MediaPlayer?) {
        mIsVideoReadyToBePlayed = true
        tryToStartVideoPlayback()
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        if (width == 0 || height == 0) {
            return
        }
        mVideoWidth = width
        mVideoHeight = height
        mIsVideoSizeKnown = true
        tryToStartVideoPlayback()
    }

    override fun surfaceChanged(surfaceholder: SurfaceHolder?, i: Int, j: Int, k: Int) {}

    override fun surfaceDestroyed(surfaceholder: SurfaceHolder?) {}

    override fun surfaceCreated(holder: SurfaceHolder?) {
        playVideo()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContainerLayoutListener(true)
    }

    private fun setContainerLayoutListener(screenRotated: Boolean) {
        layout_media_player.getViewTreeObserver().addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    layout_media_player.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                } else {
                    layout_media_player.getViewTreeObserver().removeGlobalOnLayoutListener(this)
                }
                mIsContainerSizeKnown = true
                if (screenRotated) {
                    setVideoSize()
                } else {
                    tryToStartVideoPlayback()
                }
            }
        })
    }

    private fun playVideo() {
        progress.visibility = View.VISIBLE
        doCleanUp()
        try {
            mMediaPlayer = MediaPlayer()
            if (mMediaPlayer != null) {
                mMediaPlayer!!.setDataSource(mUrl)
                mMediaPlayer!!.setDisplay(mSurfaceHolder)
                mMediaPlayer!!.prepareAsync()
                mMediaPlayer!!.setOnBufferingUpdateListener(this)
                mMediaPlayer!!.setOnCompletionListener(this)
                mMediaPlayer!!.setOnPreparedListener(this)
                mMediaPlayer!!.setOnVideoSizeChangedListener(this)
                mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun doCleanUp() {
        mVideoWidth = 0
        mVideoHeight = 0
        mIsVideoReadyToBePlayed = false
        mIsVideoSizeKnown = false
    }

    private fun tryToStartVideoPlayback() {
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown && mIsContainerSizeKnown) {
            startVideoPlayback()
        }
    }

    private fun startVideoPlayback() {
        progress.visibility = View.INVISIBLE
        if (!mMediaPlayer!!.isPlaying) {
            mSurfaceHolder!!.setFixedSize(mVideoWidth, mVideoHeight)
            setVideoSize()
            if (mIsPaused) {
                mMediaPlayer!!.seekTo(mCurrentPosition)
                mIsPaused = false
            }
            mMediaPlayer!!.start()
        }
    }

    private fun setVideoSize() {
        try {
            val videoWidth = mMediaPlayer!!.videoWidth
            val videoHeight = mMediaPlayer!!.videoHeight
            val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
            val videoWidthInContainer: Int = layout_media_player.getWidth()
            val videoHeightInContainer: Int = layout_media_player.getHeight()
            val videoInContainerProportion = videoWidthInContainer.toFloat() / videoHeightInContainer.toFloat()
            val lp: ViewGroup.LayoutParams = surface.getLayoutParams()
            if (videoProportion > videoInContainerProportion) {
                lp.width = videoWidthInContainer
                lp.height = (videoWidthInContainer.toFloat() / videoProportion).toInt()
            } else {
                lp.width = (videoProportion * videoHeightInContainer.toFloat()).toInt()
                lp.height = videoHeightInContainer
            }
            surface.setLayoutParams(lp)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}