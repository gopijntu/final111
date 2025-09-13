package com.gopi.securevault.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "misc")
data class MiscEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val number: String?,
    val amount: String?,
    val notes: String?,
    val documentPath: String?
)
