package kr.co.petdoc.petdoc.db.care.body

import androidx.room.*
import kr.co.petdoc.petdoc.db.care.scanImage.ear.EarImage

/**
 * Petdoc
 * Class: TemperatureDAO
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Dao
interface TemperatureDAO {
    @Query("SELECT * FROM temperature WHERE petId = :petId AND deleteYN = :deleteYN ORDER BY regDate DESC")
    suspend fun loadAll(petId:Int, deleteYN:String): List<Temperature>

    @Query("SELECT * FROM temperature WHERE petId = :petId AND year = :year AND month = :month AND day = :day AND deleteYN = :deleteYN")
    suspend fun loadValueByDate(petId:Int, year:String, month:String, day:String, deleteYN:String): List<Temperature>

    @Query("UPDATE temperature SET dosage = :status WHERE id = :id")
    suspend fun updateDosage(id:Long, status:Boolean)

    @Query("UPDATE temperature SET deleteYN = :deleteYN WHERE id = :id")
    suspend fun updateDeleteYN(id:Long, deleteYN: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(temperature: Temperature)

    @Insert
    suspend fun insertAll(temperatures: List<Temperature>)

    @Update
    suspend fun updateAll(vararg temperature: Temperature)

    @Transaction
    suspend fun updateData(temperatures: List<Temperature>) {
        deleteAll()
        insertAll(temperatures)
    }

    @Query("DELETE FROM temperature")
    suspend fun deleteAll()

    @Query("DELETE FROM temperature WHERE id = :id")
    suspend fun deleteValueById(id:Long)
}