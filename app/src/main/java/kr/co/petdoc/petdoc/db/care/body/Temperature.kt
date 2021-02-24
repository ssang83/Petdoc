package kr.co.petdoc.petdoc.db.care.body

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petdoc
 * Class: BodyTemperature
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Entity(tableName = "temperature")
class Temperature(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "petId") var petId: Int,
    @ColumnInfo(name = "value") var value: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "dosage") var dosage: Boolean,     // 투약 여부
    @ColumnInfo(name = "regDate") var regDate: String,
    @ColumnInfo(name = "deleteYN") var deleteYN: String,
    @ColumnInfo(name = "year") var year:String,
    @ColumnInfo(name = "month") var month:String,
    @ColumnInfo(name = "day") var day:String
) {
    constructor():this(null, 0, "", "", false, "", "N", "", "", "")
}