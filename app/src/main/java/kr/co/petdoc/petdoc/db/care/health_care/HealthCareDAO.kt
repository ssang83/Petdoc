package kr.co.petdoc.petdoc.db.care.health_care

import androidx.room.*

/**
 * Petdoc
 * Class: AuthCodeDAO
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
@Dao
interface HealthCareDAO {
    @Query("SELECT * FROM auth_code WHERE petId = :petId AND deleteYN = :deleteYN")
    suspend fun load(petId: Int, deleteYN: String): HealthCare?

    @Query("SELECT * FROM auth_code WHERE petId = :petId AND deleteYN = :deleteYN")
    suspend fun loadAll(petId:Int, deleteYN:String): List<HealthCare>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(healthCare: HealthCare)

    @Query("UPDATE auth_code SET deleteYN = :deleteYN WHERE petId = :petId")
    suspend fun updateDeleteYN(petId: Int, deleteYN: String)

    @Query("UPDATE auth_code SET authCode = :authCode WHERE petId = :petId")
    suspend fun updateAuthCode(petId: Int, authCode: String)
}