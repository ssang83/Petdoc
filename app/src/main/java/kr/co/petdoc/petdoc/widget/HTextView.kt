package kr.co.petdoc.petdoc.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.os.AsyncTask
import android.text.Html
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

/**
 * Petdoc
 * Class: HTextView
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class HTextView(context: Context, attrs: AttributeSet)
    : AppCompatTextView(context, attrs), Html.ImageGetter {

    private var mContext = context
    private var mSpace = 0

    private fun setMargin(space: Int) {
        mSpace = space
    }

    /**
     * @param source HTML 형식의 문자열
     * @param space  이미지 좌우 비는 영역 합계, 값은 px값, layout에 설정한 HTextView의 좌우 마진이나 패딩값이 없을 경우 0
     */
    fun setHtmlText(source: String?, space: Int) {
        setMargin(space)
        val spanned = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, this, null) // Html.ImageGetter 를 여기다 구현해놨다.
        this.text = spanned
    }

    /**
     * Html.ImageGetter 구현.
     *
     * @param source <img></img> 태그의 주소가 넘어온다.
     * @return 일단 LevelListDrawable 을 넘겨줘서 placeholder 처럼 보여주고, AsyncTask 를 이용해서 이미지를 다운로드
     */
    override fun getDrawable(source: String?): Drawable? {
        val d = LevelListDrawable()
        val empty = ContextCompat.getDrawable(getContext(), R.drawable.empty)
        d.addLevel(0, 0, empty)
        d.setBounds(0, 0, empty!!.intrinsicWidth, empty.intrinsicHeight)
        LoadImage().execute(source, d)
        return d
    }


    /**
     * 실제 온라인에서 이미지를 다운로드 받을 AsyncTask
     */
    inner class LoadImage : AsyncTask<Any?, Void?, Bitmap?>() {
        private var mDrawable: LevelListDrawable? = null

        override fun doInBackground(vararg params: Any?): Bitmap? {
            val source = params[0] as String
            mDrawable = params[1] as LevelListDrawable
            try {
                val `is` = URL(source).openStream()
                return BitmapFactory.decodeStream(`is`)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * 이미지 다운로드가 완료되면, 처음에 placeholder 처럼 만들어서 사용하던 Drawable 에, 다운로드 받은 이미지를 넣어준다.
         */
        override fun onPostExecute(bitmap: Bitmap?) {
            if (bitmap != null) {
                var width: Int
                var height: Int
                height = bitmap.height
                val screenWidth: Int = Helper.screenSize(mContext as Activity)[0]
                val baseWidth: Int = screenWidth - mSpace
                if (baseWidth < bitmap.width) {
                    width = baseWidth
                    height = width * bitmap.height / bitmap.width
                } else {
                    width = screenWidth
                }
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
                val d = BitmapDrawable(context.resources, scaledBitmap)
                mDrawable!!.addLevel(1, 1, d)
                mDrawable!!.setBounds(0, 0, scaledBitmap.width, scaledBitmap.height)
                mDrawable!!.level = 1
                val t: CharSequence = getText()
                setText(t)
            }
        }
    }

}