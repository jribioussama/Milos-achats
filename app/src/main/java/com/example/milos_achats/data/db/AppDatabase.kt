package com.example.milos_achats.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.milos_achats.data.dao.CheckStateDao
import com.example.milos_achats.data.entity.CheckStateEntity

@Database(entities = [CheckStateEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkStateDao(): CheckStateDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "milos_achats.db")
                    .build()
                    .also { instance = it }
            }
    }
}
