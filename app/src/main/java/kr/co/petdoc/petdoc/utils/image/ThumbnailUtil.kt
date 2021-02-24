package kr.co.petdoc.petdoc.utils.image

/**
 * Petdoc
 * Class: ThumbnailUtil
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
object ThumbnailUtil {

    /**
     * @return The url pointing to the thumbnail image with the right size for this screen.
     */
    fun getThumbUrlForScreenSize(url: String, targetWidth: Int): String {
        val tIndex = url.lastIndexOf("t")

        if (tIndex > -1) {
            val shorthand = getThumbnailShorthand(targetWidth)
            val urlBuilder = StringBuilder(url)
            urlBuilder.setCharAt(tIndex, shorthand)
            return urlBuilder.toString()
        }

        return url
    }

    /**
     * @return The shorthand for the thumbnail image with the right width for this screen.
     */
    private fun getThumbnailShorthand(targetWidth: Int): Char {
        val thumbnailWidths = intArrayOf(90, 180, 270, 425)
        val thumbnailShorthands = charArrayOf('t', 'r', 's', 'm')

        for (i in thumbnailWidths.indices) {
            if (thumbnailWidths[i] >= targetWidth) {
                return thumbnailShorthands[i]
            }
        }

        // None of the thumbnails is large enough, so return the largest possible.
        return thumbnailShorthands[thumbnailShorthands.size - 1]
    }
}