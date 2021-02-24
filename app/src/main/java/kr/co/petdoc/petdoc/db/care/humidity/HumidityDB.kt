package kr.co.petdoc.petdoc.db.care.humidity

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Petdoc
 * Class: HumidityDB
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Database(entities = [Humidity::class], version = 3)
abstract class HumidityDB : RoomDatabase() {
    abstract fun humidityDao() : HumidityDAO

    companion object {
        private var INSTANCE : HumidityDB? = null

        fun getInstance(context: Context): HumidityDB? {
            if (INSTANCE == null) {
                synchronized(HumidityDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        HumidityDB::class.java, "humidity.db")
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