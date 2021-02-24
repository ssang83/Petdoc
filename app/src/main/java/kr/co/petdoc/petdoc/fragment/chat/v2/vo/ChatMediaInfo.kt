package kr.co.petdoc.petdoc.fragment.chat.v2.vo

import android.graphics.Bitmap

/**
 * Petdoc
 * Class: MediaInfo
 * Created by kimjoonsung on 12/3/20.
 *
 * Description :
 */
data class ChatMediaInfo(
    val id: Long,
    val path: String,
    val mediaType: Int,
    val videoDuration: Long,
    val thumbnail: Bitmap
) {
    override fun toString(): String {
        return "[MediaInfo] id:$id, path:$path, mediaType:$mediaType, videoDuration:$videoDuration, thumbnail:${thumbnail}"
    }
}