package kr.co.petdoc.petdoc.utils.image

/**
 * Petdoc
 * Class: CreateTempImagesFinishedEvent
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
class CreateTempImagesFinishedEvent {
    var bitmapPaths: MutableList<String> = mutableListOf()
    var originalFilePaths: MutableList<String> = mutableListOf()
    var exception: Exception? = null
}