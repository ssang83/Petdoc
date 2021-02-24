package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MyAlarmResponse
 * Created by kimjoonsung on 1/25/21.
 *
 * Description :
 */
data class MyAlarmListResponse(
    var code: String,
    var messageKey: String?,
    var resultData: List<MyAlarm>
)

data class MyAlarm(
    var content: String,
    var logId: Int,
    var eventId: Int,
    var requestDate: String,
    var eventType: String,
    var read: Boolean
)