package kr.co.petdoc.petdoc.fragment.chat.v2.utils

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_photo_viewer.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.ImageUtil

/**
 * Petdoc
 * Class: PhotoViewerActivity
 * Created by kimjoonsung on 11/30/20.
 *
 * Description :
 */
class PhotoViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")

        progress.visibility = View.VISIBLE

        if (type != null && type.toLowerCase().contains("gif")) {
            ImageUtil.displayGifImageFromUrl(this, url, photoView, object : RequestListener<GifDrawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                    progress.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progress.visibility = View.GONE
                    return false
                }
            })
        } else {
            ImageUtil.displayImageFromUrl(this, url, photoView, object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    progress.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    progress.visibility = View.GONE
                    return false
                }
            })
        }
    }
}