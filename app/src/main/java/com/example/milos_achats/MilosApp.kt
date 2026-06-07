package com.example.milos_achats

import android.app.Application
import com.example.milos_achats.data.db.AppDatabase
import com.example.milos_achats.data.repository.CatalogRepository
import com.example.milos_achats.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MilosApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database         by lazy { AppDatabase.getInstance(this) }
    val repository       by lazy { ProductRepository(database.checkStateDao()) }
    val catalogRepository by lazy { CatalogRepository(database.productDao(), this) }

    override fun onCreate() {
        super.onCreate()
        // Sync conditionnelle au démarrage : vérifie modifiedTime avant de télécharger
        appScope.launch { catalogRepository.syncCategoryIfNeeded("bar") }
        appScope.launch { catalogRepository.syncCategoryIfNeeded("cuisine") }
        appScope.launch { catalogRepository.syncCategoryIfNeeded("server") }
    }
}
