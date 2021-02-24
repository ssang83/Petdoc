package kr.co.petdoc.petdoc.db.care.scanImage.ear

import androidx.room.*

/**
 * Petdoc
 * Class: EarImageDAO
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
@Dao
interface EarImageDAO {
    @Query("SELECT * FROM ear_image WHERE petId = :petId ORDER BY year DESC, month DESC, day DESC")
    suspend fun loadAll(petId:Int): List<EarImage>

    @Query("SELECT * FROM ear_image WHERE petId = :petId AND year = :year AND month = :month AND day = :day")
    suspend fun loadValueByDate(petId:Int, year:String, month:String, day:String): EarImage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(earImage: EarImage)

    @Insert
    suspend fun insertAll(earImages:List<EarImage>)

    @Transaction
    suspend fun updateData(earImages: List<EarImage>) {
        deleteAll()
        insertAll(earImages)
    }

    @Query("UPDATE ear_image " +
            "SET " +
                "leftEarPath = :leftEarPath, " +
                "leftEarImage = :leftEarImage, " +
                "rightEarPath = :rightEarPath, " +
                "rightEarImage = :rightEarImage, " +
                "year = :year, " +
                "month = :month, " +
                "day = :day, " +
                "time = :time  " +
            "WHERE petId = :petId AND year = :year AND month = :month AND day = :day")
    suspend fun updateEarImage(petId: Int, leftEarPath: String, leftEarImage:ByteArray?, rightEarPath:String, rightEarImage:ByteArray?, year: String, month: String, day: String, time:String)

    @Query("DELETE FROM ear_image")
    suspend fun deleteAll()

    @Query("DELETE FROM ear_image WHERE id = :id")
    suspend fun deleteValueById(id:Long)
}