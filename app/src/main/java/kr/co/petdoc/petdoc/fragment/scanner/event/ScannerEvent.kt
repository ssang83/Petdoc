package kr.co.petdoc.petdoc.fragment.scanner.event

/**
 * Petdoc
 * Class: ScannerBus
 * Created by kimjoonsung on 2020/05/13.
 *
 * Description : 스캐너 상태 이벤트 버스
 */
class ScannerEvent (
    scannerStatus:Boolean,
    apStatus:Boolean,
    batteryStatus:Int,
    lenseType:String
) {
    var scannerStatus:Boolean
    var apStatus:Boolean
    var batteryStatus:Int
    var lenseType:String

    init {
        this.scannerStatus = scannerStatus
        this.apStatus = apStatus
        this.batteryStatus = batteryStatus
        this.lenseType = lenseType
    }
}