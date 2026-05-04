package com.example.milos_achats

import android.app.Application
import com.example.milos_achats.data.db.AppDatabase
import com.example.milos_achats.data.repository.ProductRepository

class MilosApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { ProductRepository(database.checkStateDao()) }
}
