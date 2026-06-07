package com.example.milos_achats.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.milos_achats.data.dao.CheckStateDao
import com.example.milos_achats.data.dao.ProductDao
import com.example.milos_achats.data.entity.CheckStateEntity
import com.example.milos_achats.data.entity.ProductEntity

@Database(
    entities = [CheckStateEntity::class, ProductEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkStateDao(): CheckStateDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `products` (
                        `id` TEXT NOT NULL,
                        `supplierId` TEXT NOT NULL,
                        `supplierName` TEXT NOT NULL,
                        `deliveryInfo` TEXT NOT NULL,
                        `nameFr` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `quantity` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0,
                        `sheetGroupIndex` INTEGER NOT NULL DEFAULT -1,
                        `sheetRow` INTEGER NOT NULL DEFAULT -1,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "milos_achats.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
