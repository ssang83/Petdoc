package kr.co.petdoc.petdoc.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import org.greenrobot.eventbus.EventBus
import java.io.IOException

/**
 * Petdoc
 * Class: CreateTempImagesTask
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class CreateTempImagesTask : AsyncTask<Void, Void, Void> {

    companion object {
        private val MIN_IMAGE_PX = 800
    }

    private var appCtx: Context?
    private val uris: List<Uri>
    private val event: CreateTempImagesFinishedEvent
    private val validateImages: Boolean
    private val minImageWidth: Int
    private val minImageHeight: Int

    constructor(ctx: Context, uris:List<Uri>,
                event:CreateTempImagesFinishedEvent,
                validateImages:Boolean, minImageWidth:Int, minImageHeight:Int) {
        this.appCtx = ctx.applicationContext
        this.uris = uris
        this.event = event
        this.validateImages = validateImages
        this.minImageWidth = minImageWidth
        this.minImageHeight = minImageHeight
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val imageHelper = ImageUtil(appCtx!!, minImageWidth, minImageHeight)

        try {
            for (uri in uris) {
                if (!validateImages || imageHelper.isPictureValidForUpload(uri)) {
                    event.bitmapPaths.add(
                        imageHelper
                            .createDownscaledImage(
                                uri,
                                Bitmap.CompressFormat.JPEG,
                                100,
                                true
                            )
                    )
                    event.originalFilePaths.add(
                        imageHelper.createImageWithDimension(
                            uri,
                            Bitmap.CompressFormat.JPEG,
                            100,
                            true,
                            MIN_IMAGE_PX,
                            MIN_IMAGE_PX
                        )
                    )
                }
            }
        } catch (e: IOException) {
            event.exception = e
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        appCtx = null
        EventBus.getDefault().post(event)
    }
}