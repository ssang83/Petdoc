package kr.co.petdoc.petdoc.enum

/**
 * Petdoc
 * Class: Hospital
 * Created by kimjoonsung on 2020/06/08.
 *
 * Description :
 */
enum class RunStatus(val status:String) {
    O("진료중"),  // 진료중
    C("진료종료"),  // 진료종료
    B("점심시간"),  // 점심시간
    R("진료종료")   // 진료종료
}

enum class BookingType(val type:String) {
    R("접수"),  // 접수
    B("예약"),  // 예약
    A("전체"),  // 접수/예약 둘 다
    N("안됨")   // 안됨
}

enum class RoomStatus(val text:String, val color: String) {
    RECESS("휴진", "#BCC1C8"),     // 휴진
    REG_CANT("현재 접수 불가", "#BCC1C8"),   // 현재 접수 불가, 시간외 접수 불가
    REG_END("마감", "#BCC1C8"),    // 마감, 접수 마감
    SMOOTH("원활", "#9ae87d"),     // 원활
    NORMAL("보통", "#ffd740"),     // 보통
    BUSY("혼잡", "#ff5a5c")
}

enum class BookingStatus(val status: String) {
    RD("접수완료"),             // 접수완료 register done
    RC("접수취소"),             // register cancel
    RV("진료완료"),             // 접수로 진료 완료 register visit done
    BD("예약완료"),              // 예약 완료
    BR("예약 승인 대기중"),             // 예약 대기 (예약 확정하기전)
    BB("예약반려"),             // 예약 반려
    BC("예약취소"),             // 예약 취소
    BS("입실중"),               // 입실중 staying
    BV("진료완료"),             // 진료완료, 방문완료, 퇴실완료
    NV("미방문")                // 미 방문
}

