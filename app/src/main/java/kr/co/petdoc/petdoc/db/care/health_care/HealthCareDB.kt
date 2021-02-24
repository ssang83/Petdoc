package kr.co.petdoc.petdoc.db.care.health_care

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Petdoc
 * Class: AuthCodeDB
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
@Database(entities = [HealthCare::class], version = 1)
abstract class HealthCareDB : RoomDatabase() {
    abstract fun healthCareDAO() : HealthCareDAO

    companion object {
        private var INSTANCE : HealthCareDB? = null

        fun getInstance(context: Context): HealthCareDB? {
            if (INSTANCE == null) {
                synchronized(HealthCareDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        HealthCareDB::class.java, "auth_code.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}