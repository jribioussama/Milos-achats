package com.example.milos_achats.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_states")
data class CheckStateEntity(
    @PrimaryKey val key: String,
    val isChecked: Boolean
)
