package kr.co.petdoc.petdoc.event

/**
 * Petdoc
 * Class: SoftKeyboradBus
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class SoftKeyboardBus(height: Int, orientation: Int) {
    var height: Int
    var orientation: Int

    init {
        this.height = height
        this.orientation = orientation
    }
}