package kr.co.petdoc.petdoc.utils

/**
 * Petdoc
 * Class: PermissionResult
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description:
 */
interface PermissionResult {
    fun permissionGranted()

    fun permissionDenied()

    fun permissionForeverDenied()
}