package kr.co.petdoc.petdoc.db.care.body

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Petdoc
 * Class: TemperatureDB
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Database(entities = [Temperature::class], version = 4)
abstract class TemperatureDB : RoomDatabase() {
    abstract fun temperatureDao() : TemperatureDAO

    companion object {
        private var INSTANCE : TemperatureDB? = null

        fun getInstance(context: Context):TemperatureDB? {
            if (INSTANCE == null) {
                synchronized(TemperatureDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        TemperatureDB::class.java, "temperature.db")
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