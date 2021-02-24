package kr.co.petdoc.petdoc.db.care.humidity

import androidx.room.*

/**
 * Petdoc
 * Class: HumidityDAO
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Dao
interface HumidityDAO {
    @Query("SELECT * FROM humidity WHERE petId = :petId AND deleteYN = :deleteYN ORDER BY regDate DESC")
    suspend fun loadAll(petId:Int, deleteYN:String): List<Humidity>

    @Query("SELECT * FROM humidity WHERE petId = :petId AND year = :year AND month = :month AND day = :day AND deleteYN = :deleteYN")
    suspend fun loadValueByDate(petId:Int, year:String, month:String, day:String, deleteYN:String): List<Humidity>

    @Query("UPDATE humidity SET deleteYN = :deleteYN WHERE id = :id")
    suspend fun updateDeleteYN(id:Long, deleteYN: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(humidity: Humidity)

    @Insert
    suspend fun insertAll(humiditys: List<Humidity>)

    @Update
    suspend fun updateAll(vararg humidity: Humidity)

    @Transaction
    suspend fun updateData(humiditys: List<Humidity>) {
        deleteAll()
        insertAll(humiditys)
    }

    @Query("DELETE FROM humidity")
    suspend fun deleteAll()

    @Query("DELETE FROM humidity WHERE id = :id")
    suspend fun deleteValueById(id:Long)
}