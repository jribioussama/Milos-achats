package com.example.milos_achats.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.milos_achats.data.entity.CheckStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckStateDao {
    @Query("SELECT * FROM check_states")
    fun observeAll(): Flow<List<CheckStateEntity>>

    @Upsert
    suspend fun upsert(state: CheckStateEntity)

    @Query("DELETE FROM check_states")
    suspend fun deleteAll()

    @Query("DELETE FROM check_states WHERE key NOT LIKE :pattern")
    suspend fun deleteNotMatchingWeek(pattern: String)
}
