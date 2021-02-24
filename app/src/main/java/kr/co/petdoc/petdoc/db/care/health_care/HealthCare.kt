package kr.co.petdoc.petdoc.db.care.health_care

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Petdoc
 * Class: AuthCode
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
@Entity(tableName = "auth_code")
class HealthCare(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "petId") var petId: Int,
    @ColumnInfo(name = "authCode") var authCode: String,
    @ColumnInfo(name = "deleteYN") var deleteYN: String
) {
    constructor():this(null, 0, "", "N")
}