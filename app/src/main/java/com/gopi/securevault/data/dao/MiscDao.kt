package com.gopi.securevault.data.dao

import androidx.room.*
import com.gopi.securevault.data.entities.MiscEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiscDao {
    @Query("SELECT * FROM misc ORDER BY id DESC")
    fun observeAll(): Flow<List<MiscEntity>>

    @Query("SELECT * FROM misc")
    suspend fun getAll(): List<MiscEntity>

    @Insert
    suspend fun insert(entity: MiscEntity)

    @Update
    suspend fun update(entity: MiscEntity)

    @Delete
    suspend fun delete(entity: MiscEntity)
}
