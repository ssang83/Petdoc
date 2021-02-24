package kr.co.petdoc.petdoc.db.care.calibration

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petdoc
 * Class: Calibration
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description : 체온 보정 테이블
 */
@Entity(tableName = "calibration")
class Calibration(
    @PrimaryKey(autoGenerate = true) var id:Long?,
    @ColumnInfo(name = "petId") var petId:Int,
    @ColumnInfo(name = "value") var calibration:Double
) {
    constructor() : this(null, 0, 0.0)
}