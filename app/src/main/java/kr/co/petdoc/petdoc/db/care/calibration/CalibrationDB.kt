package kr.co.petdoc.petdoc.db.care.calibration

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Petdoc
 * Class: CalibrationDB
 * Created by kimjoonsung on 2020/06/24.
 *
 * Description :
 */
@Database(entities = [Calibration::class], version = 1)
abstract class CalibrationDB : RoomDatabase() {
    abstract fun calibrationDAO() : CalibrationDAO

    companion object {
        @Volatile private var INSTANCE : CalibrationDB? = null

        fun getInstance(context:Context) : CalibrationDB? {
            if (INSTANCE == null) {
                synchronized(CalibrationDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CalibrationDB::class.java,
                        "calibration.db")
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