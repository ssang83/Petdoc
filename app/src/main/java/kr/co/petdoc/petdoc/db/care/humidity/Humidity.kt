package kr.co.petdoc.petdoc.db.care.humidity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petdoc
 * Class: Humidity
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Entity(tableName = "humidity")
class Humidity(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "petId") var petId: Int,
    @ColumnInfo(name = "tvalue") var tvalue: String,
    @ColumnInfo(name = "hvalue") var hvalue: String,
    @ColumnInfo(name = "regDate") var regDate: String,
    @ColumnInfo(name = "deleteYN") var deleteYN: String,
    @ColumnInfo(name = "year") var year:String,
    @ColumnInfo(name = "month") var month:String,
    @ColumnInfo(name = "day") var day:String
) {
    constructor():this(null, 0, "", "", "", "N", "", "", "")
}