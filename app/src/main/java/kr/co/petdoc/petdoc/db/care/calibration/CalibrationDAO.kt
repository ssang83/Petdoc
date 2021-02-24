package kr.co.petdoc.petdoc.db.care.calibration

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Petdoc
 * Class: CalibrationDAO
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Dao
interface CalibrationDAO {
    @Query("SELECT * FROM calibration WHERE petId = :id")
    suspend fun getValueById(id: Int): Calibration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calibration: Calibration)

    @Query("UPDATE calibration SET value = :calValue WHERE petId = :id")
    suspend fun updateCalibration(id:Int, calValue:Double)

    @Query("DELETE FROM calibration")
    suspend fun deleteAll()
}