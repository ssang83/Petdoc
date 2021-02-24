package kr.co.petdoc.petdoc.db.care.scanImage.ear

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petdoc
 * Class: ScanImage
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
@Entity(tableName = "ear_image")
class EarImage(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    @ColumnInfo(name = "petId")
    var petId: Int,
    @ColumnInfo(name = "leftEarPath")
    var leftEarPath: String,
    @ColumnInfo(name = "leftEarImage", typeAffinity = ColumnInfo.BLOB)
    var leftEarImage: ByteArray?,
    @ColumnInfo(name = "rightEarPath")
    var rightEarPath: String,
    @ColumnInfo(name = "rightEarImage", typeAffinity = ColumnInfo.BLOB)
    var rightEarImage: ByteArray?,
    @ColumnInfo(name = "year")
    var year:String,
    @ColumnInfo(name = "month")
    var month:String,
    @ColumnInfo(name = "day")
    var day:String,
    @ColumnInfo(name = "time")
    var time: String
) {
    constructor() : this(
        null,
        -1,
        "",
        null,
        "",
        null,
        "",
        "",
        "",
        ""
    )
}