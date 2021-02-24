package kr.co.petdoc.petdoc.db.care.scanImage.ear

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.co.petdoc.petdoc.db.care.body.Temperature
import kr.co.petdoc.petdoc.db.care.body.TemperatureDAO
import kr.co.petdoc.petdoc.db.care.body.TemperatureDB

/**
 * Petdoc
 * Class: EarImageDB
 * Created by kimjoonsung on 2020/06/25.
 *
 * Description :
 */
@Database(entities = [EarImage::class], version = 1)
abstract class EarImageDB : RoomDatabase() {
    abstract fun earImageDao() : EarImageDAO

    companion object {
        private var INSTANCE : EarImageDB? = null

        fun getInstance(context: Context): EarImageDB? {
            if (INSTANCE == null) {
                synchronized(EarImageDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        EarImageDB::class.java, "ear_image.db")
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