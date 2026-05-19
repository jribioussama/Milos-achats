package com.example.milos_achats.data.repository

import com.example.milos_achats.data.dao.CheckStateDao
import com.example.milos_achats.data.entity.CheckStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val dao: CheckStateDao) {
    fun observeCheckStates(): Flow<Map<String, Boolean>> =
        dao.observeAll().map { list -> list.associate { it.key to it.isChecked } }

    suspend fun toggle(key: String, currentValue: Boolean) {
        dao.upsert(CheckStateEntity(key = key, isChecked = !currentValue))
    }

    suspend fun setChecked(key: String, value: Boolean) {
        dao.upsert(CheckStateEntity(key = key, isChecked = value))
    }

    suspend fun resetAll() = dao.deleteAll()

    suspend fun cleanupOldWeeks(weekId: String) = dao.deleteNotMatchingWeek("${weekId}_%")
}
