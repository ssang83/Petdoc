package kr.co.petdoc.petdoc.event

import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem

/**
 * Petdoc
 * Class: HospitalEvent
 * Created by kimjoonsung on 2020/07/21.
 *
 * Description :
 */
class HospitalSearchEvent (
    item: HospitalItem,
    mode:Boolean,
    keyword:String
) {
    var item: HospitalItem
    var searchMode:Boolean
    var keyword:String

    init {
        this.item = item
        this.searchMode = mode
        this.keyword = keyword
    }
}